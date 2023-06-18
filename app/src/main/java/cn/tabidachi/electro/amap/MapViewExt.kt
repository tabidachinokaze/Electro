package cn.tabidachi.electro.amap

import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.TextureMapView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal suspend inline fun MapView.awaitMap(): AMap =
    suspendCoroutine { continuation ->
        continuation.resume(map)
    }

internal suspend inline fun TextureMapView.awaitMap(): AMap =
    suspendCoroutine { continuation ->
        continuation.resume(map)
    }