package cn.tabidachi.electro.amap

import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MyLocationStyle
import java.util.Objects

val DefaultMapProperties = MapProperties()

/**
 * @param language 设置地图底图语言，目前支持中文底图和英文底图，【语言mapType仅支持：MAP_TYPE_NORMAL】
 * @param isShowBuildings  是否显示3D楼块效果
 * @param isShowMapLabels  是否显示底图标注
 * @param isIndoorEnabled  是否显示室内地图
 * @param isMyLocationEnabled 设置是否打开定位图层（myLocationOverlay）。
 * @param isTrafficEnabled 是否打开交通路况图层
 * @param myLocationStyle 设置定位图层（myLocationOverlay）的样式。
 * @param maxZoomPreference 设置地图最大缩放级别 缩放级别范围为[3, 20],超出范围将按最大级别计算
 * @param minZoomPreference 设置最小缩放级别 缩放级别范围为[3, 20],超出范围将按最小级别计算
 * @param mapShowLatLngBounds 设置地图显示范围，无论如何操作地图，显示区域都不能超过该矩形区域
 * @param mapType 设置地图模式，默认为：MAP_TYPE_NORMAL
 */
class MapProperties(
    val language: String = AMap.CHINESE,
    val isShowBuildings: Boolean = false,
    val isShowMapLabels: Boolean = true,
    val isIndoorEnabled: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val myLocationStyle: MyLocationStyle? = null,
    val maxZoomPreference: Float = 21.0F,
    val minZoomPreference: Float = 0F,
    val mapShowLatLngBounds: LatLngBounds? = null,
    val mapType: MapType = MapType.NORMAL,
) {

    override fun equals(other: Any?): Boolean = other is MapProperties &&
            language == other.language &&
            isShowBuildings == other.isShowBuildings &&
            isShowMapLabels == other.isShowMapLabels &&
            isIndoorEnabled == other.isIndoorEnabled &&
            isMyLocationEnabled == other.isMyLocationEnabled &&
            isTrafficEnabled == other.isTrafficEnabled &&
            myLocationStyle == other.myLocationStyle &&
            maxZoomPreference == other.maxZoomPreference &&
            minZoomPreference == other.minZoomPreference &&
            mapShowLatLngBounds == other.mapShowLatLngBounds &&
            mapType == other.mapType

    override fun hashCode(): Int = Objects.hash(
        language,
        isShowBuildings,
        isShowMapLabels,
        isIndoorEnabled,
        isMyLocationEnabled,
        isTrafficEnabled,
        myLocationStyle,
        maxZoomPreference,
        minZoomPreference,
        mapShowLatLngBounds,
        mapType
    )

    fun copy(
        language: String = this.language,
        isShowBuildings: Boolean = this.isShowBuildings,
        isShowMapLabels: Boolean = this.isShowMapLabels,
        isIndoorEnabled: Boolean = this.isIndoorEnabled,
        isMyLocationEnabled: Boolean = this.isMyLocationEnabled,
        isTrafficEnabled: Boolean = this.isTrafficEnabled,
        myLocationStyle: MyLocationStyle? = this.myLocationStyle,
        maxZoomPreference: Float = this.maxZoomPreference,
        minZoomPreference: Float = this.minZoomPreference,
        mapShowLatLngBounds: LatLngBounds? = this.mapShowLatLngBounds,
        mapType: MapType = this.mapType,
    ): MapProperties = MapProperties(
        language = language,
        isShowBuildings = isShowBuildings,
        isShowMapLabels = isShowMapLabels,
        isIndoorEnabled = isIndoorEnabled,
        isMyLocationEnabled = isMyLocationEnabled,
        isTrafficEnabled = isTrafficEnabled,
        myLocationStyle = myLocationStyle,
        maxZoomPreference = maxZoomPreference,
        minZoomPreference = minZoomPreference,
        mapShowLatLngBounds = mapShowLatLngBounds,
        mapType = mapType,
    )

    override fun toString(): String {
        return "MapProperties(language=$language, " +
                "isShowBuildings=$isShowBuildings, " +
                "isShowMapLabels=$isShowMapLabels, " +
                "isIndoorEnabled=$isIndoorEnabled, " +
                "isMyLocationEnabled=$isMyLocationEnabled, " +
                "isTrafficEnabled=$isTrafficEnabled, " +
                "myLocationStyle=$myLocationStyle, " +
                "maxZoomPreference=$maxZoomPreference, " +
                "minZoomPreference=$minZoomPreference, " +
                "mapShowLatLngBounds=$mapShowLatLngBounds, " +
                "mapType=$mapType)"
    }
}