package com.example.mobilefinalproject

import android.app.Application
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

/**
 * Custom Application class.
 * Configures Picasso with a persistent OkHttp disk cache (50 MB) so remote images
 * survive network interruptions and app restarts without any extra code in UI layer.
 */
class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        configurePicasso()
    }

    private fun configurePicasso() {
        val cacheDir = File(cacheDir, "picasso-cache")
        val diskCacheSizeBytes = 50L * 1024 * 1024 // 50 MB

        val okHttpClient = OkHttpClient.Builder()
            .cache(Cache(cacheDir, diskCacheSizeBytes))
            .build()

        val picasso = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(okHttpClient))
            .build()

        // setSingletonInstance throws if called more than once (e.g. some test setups).
        runCatching { Picasso.setSingletonInstance(picasso) }
    }
}


