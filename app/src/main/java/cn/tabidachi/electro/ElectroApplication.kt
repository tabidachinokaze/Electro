package cn.tabidachi.electro

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.VideoFrameDecoder
import coil.request.CachePolicy
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ElectroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .components {
                    add(VideoFrameDecoder.Factory())
                    add(GifDecoder.Factory())
                }.build()
        )
        AppCenter.start(
            this,
            BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java,
            Crashes::class.java,
            Distribute::class.java
        )
    }
}