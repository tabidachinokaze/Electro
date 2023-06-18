package cn.tabidachi.electro.ui.common.attachment

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cn.tabidachi.electro.R
import cn.tabidachi.electro.amap.AMapLifecycle
import cn.tabidachi.electro.amap.rememberCameraPositionState
import cn.tabidachi.electro.model.attachment.LocationAttachment
import cn.tabidachi.electro.ui.map.UIMarkerInScreenCenter
import cn.tabidachi.electro.ui.common.SimpleListItem
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng

@Composable
fun LocationAttachment(attachment: LocationAttachment) {
    val context = LocalContext.current

    val function = {
        val RideNavi =
            "amapuri://openFeature?featureName=OnRideNavi&rideType=elebike&sourceApplication=Electro&lat=${attachment.latitude}&lon=${attachment.longitude}&dev=0"
        val nav =
            "androidamap://navi?sourceApplication=Electro&poiname=${attachment.title}&lat=${attachment.latitude}&lon=${attachment.longitude}&dev=0&style=2"
        val uri =
            "androidamap://viewReGeo?sourceApplication=Electro&lat=${attachment.latitude}&lon=${attachment.longitude}&dev=0"
        val mark =
            "androidamap://viewMap?sourceApplication=appname&poiname=${attachment.title ?: "目标位置"}&lat=${attachment.latitude}&lon=${attachment.longitude}&dev=0"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mark))
        intent.setPackage("com.autonavi.minimap")
        context.startActivity(intent)
    }
    Column(
        modifier = Modifier.clickable(onClick = function)
    ) {
        Box(
            modifier = Modifier
                .requiredHeight(200.dp)
                .widthIn(200.dp)
        ) {
            val mapView = remember {
                TextureMapView(context, AMapOptions())
            }
            AndroidView(factory = { mapView })
            AMapLifecycle(mapView)
            val cameraPositionState = rememberCameraPositionState() {
                setMap(mapView.map)
            }
            var isMapLoaded by rememberSaveable { mutableStateOf(true) }
            LaunchedEffect(key1 = Unit, block = {
                mapView.map.setOnMapClickListener {
                    function()
                }
                mapView.map.uiSettings.isZoomControlsEnabled = false
                mapView.map.uiSettings.isZoomGesturesEnabled = false
                mapView.map.uiSettings.isScaleControlsEnabled = false
                mapView.map.uiSettings.isScrollGesturesEnabled = false
                mapView.map.uiSettings.setLogoLeftMargin(Int.MAX_VALUE / 2)
                val update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        LatLng(
                            attachment.latitude,
                            attachment.longitude
                        ), 23f, 0f, 0f
                    )
                )
                cameraPositionState.animate(update)
            })
            if (isMapLoaded) {
                UIMarkerInScreenCenter(resID = R.drawable.purple_pin) {
                    Size(25F, 11F)
                }
            }
        }
        attachment.title?.let {
            SimpleListItem(
                headlineContent = {
                    Text(text = attachment.title)
                }, supportingContent = {
                    with(attachment) {
                        Text(text = "${city ?: ""}${address ?: ""}${snippet ?: ""}")
                    }
                }, modifier = Modifier.padding(8.dp)
            )
        }
    }
}