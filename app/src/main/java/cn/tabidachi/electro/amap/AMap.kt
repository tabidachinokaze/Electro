package cn.tabidachi.electro.amap

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.IndoorBuildingInfo
import com.amap.api.maps.model.Poi
import kotlinx.coroutines.awaitCancellation

@Composable
fun AMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    aMapOptionsFactory: () -> AMapOptions = { AMapOptions() },
    properties: MapProperties = DefaultMapProperties,
    uiSettings: MapUiSettings = DefaultMapUiSettings,
    locationSource: LocationSource? = null,
    onMapLoaded: () -> Unit = {},
    onPOIClick: (Poi) -> Unit = {},
    indoorBuildingActive: (IndoorBuildingInfo) -> Unit = {},
    content: (@Composable @AMapComposable () -> Unit)? = null
) {
    if (LocalInspectionMode.current) {
        return
    }
    val context = LocalContext.current
    val mapView = remember {
        MapView(context, aMapOptionsFactory())
//        TextureMapView(context, aMapOptionsFactory())
    }
    AndroidView(modifier = modifier, factory = { mapView })
    AMapLifecycle(mapView)
    val mapClickListeners = remember { MapClickListeners() }.also {
        it.onMapLoaded = onMapLoaded
        it.onPOIClick = onPOIClick
        it.indoorBuildingActive = indoorBuildingActive
    }

    val currentLocationSource by rememberUpdatedState(locationSource)
    val currentMapProperties by rememberUpdatedState(properties)
    val currentUiSettings by rememberUpdatedState(uiSettings)
    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)

    LaunchedEffect(Unit) {
        disposingComposition {
            mapView.newComposition(parentComposition) {
                MapUpdater(
                    mapUiSettings = currentUiSettings,
                    clickListeners = mapClickListeners,
                    locationSource = currentLocationSource,
                    mapProperties = currentMapProperties,
                    cameraPositionState = cameraPositionState
                )
                currentContent?.invoke()
            }
        }
    }
}

private suspend inline fun disposingComposition(factory: () -> Composition) {
    val composition = factory()
    try {
        awaitCancellation()
    } finally {
        composition.dispose()
    }
}

private suspend inline fun MapView.newComposition(
    parent: CompositionContext,
    noinline content: @Composable () -> Unit
): Composition {
    val map = awaitMap()
    return Composition(
        MapApplier(map, this), parent
    ).apply {
        setContent(content)
    }
}

@Composable
fun AMapLifecycle(mapView: MapView) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val previousState = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    DisposableEffect(context, lifecycle, mapView) {
        val mapLifecycleObserver = mapView.lifecycleObserver(previousState)
        val callbacks = mapView.componentCallbacks()

        lifecycle.addObserver(mapLifecycleObserver)
        context.registerComponentCallbacks(callbacks)
        println("MapView DisposableEffect")
        onDispose {
            lifecycle.removeObserver(mapLifecycleObserver)
            context.unregisterComponentCallbacks(callbacks)
            // fix memory leak
            mapView.onDestroy()
            mapView.removeAllViews()
            println("MapView onDispose")
        }
    }
}

@Composable
fun AMapLifecycle(mapView: TextureMapView) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val previousState = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    DisposableEffect(context, lifecycle, mapView) {
        val mapLifecycleObserver = mapView.lifecycleObserver(previousState)
        val callbacks = mapView.componentCallbacks()

        lifecycle.addObserver(mapLifecycleObserver)
        context.registerComponentCallbacks(callbacks)
        println("TextureMapView DisposableEffect")
        onDispose {
            lifecycle.removeObserver(mapLifecycleObserver)
            context.unregisterComponentCallbacks(callbacks)
            // fix memory leak
            mapView.onDestroy()
            mapView.removeAllViews()
            println("TextureMapView onDispose")
        }
    }
}

private fun MapView.lifecycleObserver(previousState: MutableState<Lifecycle.Event>): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        event.targetState
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                // Skip calling mapView.onCreate if the lifecycle did not go through onDestroy - in
                // this case the GDMap composable also doesn't leave the composition. So,
                // recreating the map does not restore state properly which must be avoided.
                if (previousState.value != Lifecycle.Event.ON_STOP) {
                    this.onCreate(Bundle())
                }
                println("lifecycleObserver ON_CREATE")
            }

            Lifecycle.Event.ON_RESUME -> {
                this.onResume()
                println("lifecycleObserver ON_RESUME")
            }

            Lifecycle.Event.ON_PAUSE -> {
                this.onPause()
                println("lifecycleObserver ON_PAUSE")
            }

            Lifecycle.Event.ON_DESTROY -> {
                // handled in onDispose
                println("lifecycleObserver ON_DESTROY")
            }
//            else -> { /* ignore */ }
            Lifecycle.Event.ON_START -> {
                println("lifecycleObserver ON_START")
            }

            Lifecycle.Event.ON_STOP -> {
                println("lifecycleObserver ON_STOP")
            }

            Lifecycle.Event.ON_ANY -> {
                println("lifecycleObserver ON_ANY")
            }
        }
        previousState.value = event
    }

private fun MapView.componentCallbacks(): ComponentCallbacks =
    object : ComponentCallbacks {
        override fun onConfigurationChanged(config: Configuration) {}

        override fun onLowMemory() {
            this@componentCallbacks.onLowMemory()
        }
    }


private fun TextureMapView.lifecycleObserver(previousState: MutableState<Lifecycle.Event>): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                // Skip calling mapView.onCreate if the lifecycle did not go through onDestroy - in
                // this case the GDMap composable also doesn't leave the composition. So,
                // recreating the map does not restore state properly which must be avoided.
                if (previousState.value != Lifecycle.Event.ON_STOP) {
                    this.onCreate(Bundle())
                }
            }

            Lifecycle.Event.ON_RESUME -> this.onResume()
            Lifecycle.Event.ON_PAUSE -> this.onPause()
            Lifecycle.Event.ON_DESTROY -> {
                // handled in onDispose
            }

            else -> { /* ignore */
            }
        }
        previousState.value = event
    }

private fun TextureMapView.componentCallbacks(): ComponentCallbacks =
    object : ComponentCallbacks {
        override fun onConfigurationChanged(config: Configuration) {}

        override fun onLowMemory() {
            this@componentCallbacks.onLowMemory()
        }
    }