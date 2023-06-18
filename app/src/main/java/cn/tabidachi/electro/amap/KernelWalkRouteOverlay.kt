package cn.tabidachi.electro.amap

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.services.route.WalkPath
import com.amap.api.services.route.WalkStep

internal class KernelWalkRouteOverlay(
    aMap: AMap,
    isSelected: Boolean,
    routeWidth: Float,
    polylineColor: Color,
    walkLineSelectedTexture: BitmapDescriptor?,
    walkLineUnSelectedTexture: BitmapDescriptor?,
    walkNodeIcon: BitmapDescriptor?,
    startPoint: LatLng,
    endPoint: LatLng,
    private val walkPath: WalkPath?
): KernelRouteOverlay(
    aMap = aMap,
    isSelected = isSelected,
    routeWidth = routeWidth,
    polylineColor = polylineColor,
    walkLineSelectedTexture = walkLineSelectedTexture,
    walkLineUnSelectedTexture = walkLineUnSelectedTexture,
    busLineSelectedTexture = null,
    busLineUnSelectedTexture = null,
    driveLineSelectedTexture = null,
    driveLineUnSelectedTexture = null,
    busNodeIcon = null,
    walkNodeIcon = walkNodeIcon,
    driveNodeIcon = null,
    startPoint = startPoint,
    endPoint = endPoint
) {
    companion object {
        private const val TAG = "WalkRouteOverlay"
    }

    private var mPolylineOptions: PolylineOptions? = null

    /**
     * 添加步行路线到地图中
     */
    fun addToMap() = asyncLaunch {
        removeFromMap()
        initPolylineOptions()
        val result = kotlin.runCatching {
            val walkPaths: List<WalkStep> = walkPath?.steps ?: emptyList()
            mPolylineOptions?.add(startPoint)
            for (i in walkPaths.indices) {
                val walkStep = walkPaths[i]
                convertToLatLng(walkStep.polyline[0])?.let { addWalkStationMarkers(walkStep, it) }
                addWalkPolyLines(walkStep)
            }
            mPolylineOptions?.add(endPoint)
            showPolyline()
        }
        if (isSelected) {
            zoomToSpan()
        }
        if (result.isFailure) {
            Log.e(TAG, "addToMap", result.exceptionOrNull())
        }
    }

    private fun addWalkPolyLines(walkStep: WalkStep) {
        mPolylineOptions?.addAll(convertArrList(walkStep.polyline))
    }

    private fun addWalkStationMarkers(walkStep: WalkStep, position: LatLng) {
        addStationMarker(
            MarkerOptions()
            .position(position)
            .title("\u65B9\u5411:" + walkStep.action
                    + "\n\u9053\u8DEF:" + walkStep.road
            )
            .snippet(walkStep.instruction).visible(nodeIconVisible)
            .anchor(0.5f, 0.5f).icon(walkNodeIcon))
    }

    private fun initPolylineOptions() {
        mPolylineOptions = null
        mPolylineOptions = PolylineOptions()
        setPolylineSelected(isSelected)
    }

    override fun setPolylineSelected(isSelected: Boolean) {
        mPolylineOptions
            ?.color(polylineColor.copy(alpha = if (isSelected) ROUTE_SELECTED_TRANSPARENCY else ROUTE_UNSELECTED_TRANSPARENCY).toArgb())
            ?.setCustomTexture(if (isSelected) walkLineSelectedTexture else walkLineUnSelectedTexture)
            ?.zIndex(if (isSelected) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
            ?.width(routeWidth)
        removeAllPolyLines()
        showPolyline()
    }


    private fun showPolyline() {
        addPolyLine(mPolylineOptions)
    }
}