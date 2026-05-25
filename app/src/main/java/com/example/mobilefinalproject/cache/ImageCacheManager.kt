package com.example.mobilefinalproject.cache

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.db.AppDatabase
import com.example.mobilefinalproject.db.entity.CachedImageEntity
import com.example.mobilefinalproject.session.TokenManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Manages a persistent local image cache on disk, tracked by Room.
 *
 * ## How it works
 * 1. `loadInto` loads an image into an ImageView. It first checks Room/disk for a
 *    cached copy and loads that immediately (no network, no placeholder flash).
 *    In the background it re-downloads the remote URL so the disk copy stays current.
 * 2. When no cached copy exists, Picasso fetches from network. A placeholder is shown
 *    only if the ImageView is currently empty; otherwise the current image is kept
 *    while the new one loads (prevents the "green icon flash" on teal background).
 * 3. `invalidate` removes a URL from Room + disk + Picasso memory cache — call this
 *    before loading a URL whose content has changed (e.g. after a profile photo update).
 * 4. `evictStale` prunes entries older than a given age (default 7 days).
 *
 * Auth note: `saveToCache` uses an OkHttpClient with the app's Bearer token so that
 * images served from authenticated endpoints are downloaded correctly.
 * Picasso itself is also configured with auth in AppApplication.
 */
class ImageCacheManager(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).cachedImageDao()
    private val cacheDir: File
        get() = File(context.filesDir, "img_cache").also { it.mkdirs() }

    /** Authenticated OkHttpClient used for disk-cache downloads. */
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = TokenManager.getAccessToken(context)
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else chain.request()
                chain.proceed(request)
            }
            .build()
    }

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Loads [url] into [imageView].
     *
     * - If a local cached file exists → display it instantly (no placeholder).
     *   Refreshes the file in the background so it stays current.
     * - If no cached file → load from network via Picasso (which has auth via
     *   AppApplication). A placeholder is shown only when the ImageView is currently
     *   empty, to avoid the green-background flash when re-navigating to the screen.
     */
    suspend fun loadInto(
        url: String?,
        imageView: ImageView,
        placeholderRes: Int = R.drawable.ic_person
    ) {
        if (url.isNullOrBlank()) {
            withContext(Dispatchers.Main) { imageView.setImageResource(placeholderRes) }
            return
        }

        // ── Serve from disk cache (zero network, zero placeholder flash) ────
        val cachedFile = getCachedFile(url)
        if (cachedFile != null) {
            withContext(Dispatchers.Main) {
                Picasso.get()
                    .load(cachedFile)
                    .noPlaceholder()          // file is local – loads instantly, no flash
                    .error(placeholderRes)
                    .into(imageView)
            }
            // Keep the disk copy fresh in the background
            refreshInBackground(url)
            return
        }

        // ── No cached file: load from network ───────────────────────────────
        withContext(Dispatchers.Main) {
            val builder = Picasso.get().load(url).error(placeholderRes)
            // Show placeholder only when the view is currently empty. If the view
            // already shows something (previous image / app restoring state), keep
            // that content while the new image loads – avoids the teal-background flash.
            val isEmpty = imageView.drawable == null || imageView.drawable is ColorDrawable
            if (isEmpty) builder.placeholder(placeholderRes) else builder.noPlaceholder()
            builder.into(imageView)
        }
        // Persist for the next cold start / offline session
        saveToCache(url)
    }

    /**
     * Removes [url] from Room, deletes the local file, and invalidates Picasso's
     * in-memory cache entry. Call this after the remote content at [url] has changed
     * (e.g. after the user replaces their profile photo).
     */
    suspend fun invalidate(url: String) = withContext(Dispatchers.IO) {
        dao.get(url)?.let { entity -> File(entity.localFilePath).delete() }
        dao.delete(url)
        withContext(Dispatchers.Main) {
            runCatching { Picasso.get().invalidate(url) }
        }
    }

    /**
     * Returns the locally cached File for [url] if it exists on disk *and* in Room,
     * otherwise returns null.
     */
    suspend fun getCachedFile(url: String): File? = withContext(Dispatchers.IO) {
        val entity = dao.get(url) ?: return@withContext null
        val file = File(entity.localFilePath)
        if (file.exists()) file else {
            // Stale Room entry (file deleted externally) — remove the DB row
            dao.delete(url)
            null
        }
    }

    /**
     * Downloads [url] using an authenticated OkHttpClient and stores it on disk,
     * then records the path in Room. Returns the saved File on success, null on error.
     */
    suspend fun saveToCache(url: String): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileName = url.hashCode().toString().replace("-", "n") + ".jpg"
            val destFile = File(cacheDir, fileName)

            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            response.body?.byteStream()?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }

            dao.upsert(CachedImageEntity(remoteUrl = url, localFilePath = destFile.absolutePath))
            destFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Evicts cache entries (and their local files) older than [maxAgeMs] milliseconds.
     * Safe to call periodically (e.g. on app start) — defaults to 7 days.
     */
    suspend fun evictStale(maxAgeMs: Long = TimeUnit.DAYS.toMillis(7)) =
        withContext(Dispatchers.IO) {
            val cutoff = System.currentTimeMillis() - maxAgeMs
            val stale = dao.getOlderThan(cutoff)
            stale.forEach { File(it.localFilePath).delete() }
            dao.evictOlderThan(cutoff)
        }

    // ── Private helpers ─────────────────────────────────────────────────────

    /** Silently re-download [url] to disk so the cached copy stays up-to-date. */
    private suspend fun refreshInBackground(url: String) {
        withContext(Dispatchers.IO) {
            try { saveToCache(url) } catch (_: Exception) { /* best-effort */ }
        }
    }
}
