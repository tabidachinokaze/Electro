package cn.tabidachi.electro.amap

import androidx.compose.ui.graphics.Color
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.services.core.LatLonPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal open class KernelRouteOverlay(
    val aMap: AMap,
    val isSelected: Boolean,
    val routeWidth: Float,
    val polylineColor: Color,
    val walkLineSelectedTexture: BitmapDescriptor?,
    val walkLineUnSelectedTexture: BitmapDescriptor?,
    val busLineSelectedTexture: BitmapDescriptor?,
    val busLineUnSelectedTexture: BitmapDescriptor?,
    val driveLineSelectedTexture: BitmapDescriptor?,
    val driveLineUnSelectedTexture: BitmapDescriptor?,
    val busNodeIcon: BitmapDescriptor?,
    val walkNodeIcon: BitmapDescriptor?,
    val driveNodeIcon: BitmapDescriptor?,
    var startPoint: LatLng,
    var endPoint: LatLng
) {
    companion object {
        const val ROUTE_UNSELECTED_TRANSPARENCY = 0.3F
        const val ROUTE_SELECTED_TRANSPARENCY = 1F
        const val ROUTE_SELECTED_ZINDEX = 0F
        const val ROUTE_UNSELECTED_ZINDEX = -1F
    }

    private val asyncJobs: MutableList<Job> = mutableListOf()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val stationMarkers: MutableList<Marker> = mutableListOf()
    val allPolyLines: MutableList<Polyline> = mutableListOf()
    var nodeIconVisible: Boolean = true

    fun asyncLaunch(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) = coroutineScope.launch(context = context) {
        block.invoke(this)
    }.apply {
        asyncJobs.add(this)
    }

    fun convertToLatLng(latLonPoint: LatLonPoint?): LatLng? {
        if(null == latLonPoint) return null
        return LatLng(latLonPoint.latitude, latLonPoint.longitude)
    }

    open fun setPolylineSelected(isSelected: Boolean){
    }

    fun convertArrList(shapes: List<LatLonPoint>): ArrayList<LatLng> {
        val lineShapes: ArrayList<LatLng> = ArrayList()
        for (point in shapes) {
            convertToLatLng(point)?.let { lineShapes.add(it) }
        }
        return lineShapes
    }

    /**
     * 去掉BusRouteOverlay上所有的Marker。
     */
    open fun removeFromMap() {
        removeAllMarkers()
        removeAllPolyLines()
        asyncJobs.forEach {
            it.cancel()
        }
        asyncJobs.clear()
    }

    private fun removeAllMarkers() {
        for (marker in stationMarkers) {
            marker.remove()
        }
    }

    fun removeAllPolyLines() {
        for (line in allPolyLines) {
            line.remove()
        }
    }

    open fun getLatLngBounds(): LatLngBounds {
        val b: LatLngBounds.Builder = LatLngBounds.builder()
        b.include(LatLng(startPoint.latitude, startPoint.longitude))
        b.include(LatLng(endPoint.latitude, endPoint.longitude))
        for (polyline in allPolyLines) {
            for (point in polyline.points) {
                b.include(point)
            }
        }
        return b.build()
    }

    /**
     * 移动镜头到当前的视角。
     */
    fun zoomToSpan(boundsPadding: Int = 100) {
        kotlin.runCatching {
            val bounds: LatLngBounds = getLatLngBounds()
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, boundsPadding))
        }
    }

    /**
     * 路段节点图标控制显示接口。
     * @param visible true为显示节点图标，false为不显示。
     */
    fun setNodeIconVisibility(visible: Boolean) {
        kotlin.runCatching {
            nodeIconVisible = visible
            if (stationMarkers.size > 0) {
                for (i in stationMarkers.indices) {
                    if(null != stationMarkers[i].icons[0].bitmap) {
                        stationMarkers[i].isVisible = visible
                    }
                }
            }
        }
    }

    fun addStationMarker(options: MarkerOptions?) {
        if (options == null) {
            return
        }
        aMap.addMarker(options)?.let { stationMarkers.add(it) }
    }

    fun addPolyLine(options: PolylineOptions?) {
        if (options == null) {
            return
        }
        aMap.addPolyline(options)?.let {  allPolyLines.add(it) }
    }
}