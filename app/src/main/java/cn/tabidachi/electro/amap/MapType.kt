package cn.tabidachi.electro.amap

import androidx.compose.runtime.Immutable
import com.amap.api.maps.AMap

@Immutable
enum class MapType(val value: Int) {
    /**
     * 白昼地图（即普通地图）
     */
    NORMAL(AMap.MAP_TYPE_NORMAL),

    /**
     * 卫星图
     */
    SATELLITE(AMap.MAP_TYPE_SATELLITE),

    /**
     * 导航地图
     */
    NAVI(AMap.MAP_TYPE_NAVI),

    /**
     * 夜景地图
     */
    NIGHT(AMap.MAP_TYPE_NIGHT)
}