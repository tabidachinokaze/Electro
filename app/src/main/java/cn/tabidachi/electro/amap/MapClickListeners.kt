package cn.tabidachi.electro.amap

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.amap.api.maps.model.IndoorBuildingInfo
import com.amap.api.maps.model.Poi

internal class MapClickListeners {
    var onMapLoaded: () -> Unit by mutableStateOf({})
    var onPOIClick: (Poi) -> Unit by mutableStateOf({})
    var indoorBuildingActive: (IndoorBuildingInfo) -> Unit by mutableStateOf({})
}