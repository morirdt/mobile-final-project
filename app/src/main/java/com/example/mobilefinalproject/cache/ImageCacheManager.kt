package com.example.mobilefinalproject.cache

import android.content.Context
import android.widget.ImageView
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.db.AppDatabase
import com.example.mobilefinalproject.db.entity.CachedImageEntity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Manages a persistent local image cache on disk, tracked by Room.
 *
 * ## How it works
 * 1. [loadInto] loads an image into an [ImageView].  It first checks Room / disk for a
 *    cached copy and loads that immediately.  In the background it fetches the remote URL,
 *    saves the bytes to `filesDir/img_cache/`, and updates the Room record so the next call
 *    is served entirely from disk.
 * 2. [getCachedFile] returns the local [File] for a URL if it is already cached, or null.
 * 3. [evictStale] deletes cache entries (and their files) older than [maxAgeMs].
 *
 * Note: Picasso is also configured (in [AppApplication]) with a 50 MB HTTP disk cache via
 * OkHttp, so TLS-enabled remote images are automatically cached at the HTTP layer too.
 * [ImageCacheManager] adds a *permanent* local copy that survives cache eviction and works
 * fully offline.
 */
class ImageCacheManager(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).cachedImageDao()
    private val cacheDir: File
        get() = File(context.filesDir, "img_cache").also { it.mkdirs() }

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Loads [url] into [imageView] using a placeholder drawable while loading.
     * Serves the locally cached file when available; falls back to the network otherwise.
     * After a successful network load the image is saved to disk for future offline access.
     */
    suspend fun loadInto(
        url: String?,
        imageView: ImageView,
        placeholderRes: Int = R.drawable.ic_person
    ) {
        if (url.isNullOrBlank()) {
            imageView.setImageResource(placeholderRes)
            return
        }

        // Try to serve from local cache first (instant, no network required)
        val cachedFile = getCachedFile(url)
        if (cachedFile != null) {
            withContext(Dispatchers.Main) {
                Picasso.get()
                    .load(cachedFile)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .into(imageView)
            }
            // Refresh the file in the background so it stays up-to-date
            refreshInBackground(url)
            return
        }

        // No local copy — load from network and save to disk simultaneously
        withContext(Dispatchers.Main) {
            Picasso.get()
                .load(url)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .into(imageView)
        }
        // Persist to disk for next offline access
        saveToCache(url)
    }

    /**
     * Returns the locally cached [File] for [url] if it exists on disk *and* in Room,
     * otherwise returns null.
     */
    suspend fun getCachedFile(url: String): File? = withContext(Dispatchers.IO) {
        val entity = dao.get(url) ?: return@withContext null
        val file = File(entity.localFilePath)
        if (file.exists()) file else {
            // Stale Room entry — the file was deleted externally; clean up the DB row
            dao.delete(url)
            null
        }
    }

    /**
     * Downloads [url] to the local cache dir and records the path in Room.
     * Returns the saved [File] on success, or null on any error.
     */
    suspend fun saveToCache(url: String): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileName = url.hashCode().toString().replace("-", "n") + ".jpg"
            val destFile = File(cacheDir, fileName)

            URL(url).openStream().use { input ->
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

    // ── Private helpers ────────────────────────────────────────────────────

    /** Fire-and-forget background refresh of the locally cached copy. */
    private suspend fun refreshInBackground(url: String) {
        withContext(Dispatchers.IO) {
            try { saveToCache(url) } catch (_: Exception) { /* best-effort */ }
        }
    }
}

