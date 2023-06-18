package cn.tabidachi.electro.ui.map

import android.Manifest
import android.app.Application
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.tabidachi.electro.R
import cn.tabidachi.electro.amap.AMap
import cn.tabidachi.electro.amap.MapUiSettings
import cn.tabidachi.electro.amap.Marker
import cn.tabidachi.electro.amap.rememberCameraPositionState
import cn.tabidachi.electro.amap.rememberMarkerState
import cn.tabidachi.electro.ext.applicationSettings
import cn.tabidachi.electro.ext.toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.amap.api.services.poisearch.PoiResultV2
import com.amap.api.services.poisearch.PoiSearchV2
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DragDropSelectPointScreen(
    onBack: (LatLng?) -> Unit = {},
    onPoiClick: (PoiItemV2) -> Unit = {},
    onSelectLocationClick: (LatLng) -> Unit = {}
) {
    var isMapLoaded by rememberSaveable { mutableStateOf(false) }
    val dragDropAnimatable = remember { Animatable(Size.Zero, Size.VectorConverter) }
    val cameraPositionState = rememberCameraPositionState()
    val locationState = rememberMarkerState()
    val viewModel: DragDropSelectPointViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()
    val openGpsLauncher = handlerGPSLauncher(viewModel::checkGpsStatus)
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), onPermissionsResult = {
            val notGrant = it.filter { !it.value }
            if (notGrant.isNotEmpty()) {
                viewModel.showDialog()
            } else {
                viewModel.checkGpsStatus()
            }
        }
    )
    val mapView = MapView(LocalContext.current)
    val cameraPosition = CameraPosition(
        LatLng(1.0, 1.0), 0f, 0f, 0f
    )
    mapView.map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    LaunchedEffect(key1 = Unit, block = {
        snapshotFlow { permissionsState.allPermissionsGranted }.collect {
            viewModel.checkGpsStatus()
        }
    })
    LaunchedEffect(viewState.isOpenGps, permissionsState.allPermissionsGranted) {
        if (viewState.isOpenGps == true) {
            if (!permissionsState.allPermissionsGranted) {
                permissionsState.launchMultiplePermissionRequest()
            } else {
                viewModel.startMapLocation()
            }
        }
    }
    // 地图移动，中心的Marker需要动画跳动
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            dragDropAnimatable.animateTo(Size(45F, 20F))
        } else {
            dragDropAnimatable.animateTo(Size(25F, 11F))
            // 查询附近1000米地址数据
            viewModel.doSearchQueryPoi(cameraPositionState.position.target)
        }
    }
    LaunchedEffect(viewState.isClickForceStartLocation, viewState.currentLocation) {
        val curLocation = viewState.currentLocation
        if (null == curLocation || cameraPositionState.position.target == curLocation) return@LaunchedEffect
        locationState.position = curLocation
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(viewState.currentLocation, 17F))
    }

    if (viewState.isShowOpenGPSDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDialog, confirmButton = {
                TextButton(onClick = {
                    viewModel.openGPSPermission(openGpsLauncher)
                    viewModel.hideDialog()
                }) {
                    Text(text = "开启定位")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    viewModel.hideDialog()
                }) {
                    Text(text = "取消")
                }
            }, text = {
                Text(text = "定位失败，打开定位服务来获取位置信息")
            }
        )
    }
    Column {
        TopAppBar(
            title = {
                Text(text = "位置")
            }, navigationIcon = {
                IconButton(onClick = {
                    onBack(viewState.currentLocation)
                }) {
                    Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                }
            }
        )
        Column(modifier = Modifier.weight(0.5f)) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                AMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        showMapLogo = false,
                        isZoomGesturesEnabled = true,
                        isScrollGesturesEnabled = true
                    ), onMapLoaded = {
                        isMapLoaded = true
                    }
                ) {
                    Marker(
                        state = locationState,
                        anchor = Offset(0.5F, 0.5F),
                        rotation = viewState.currentRotation,
                        icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location_self),
                        onClick = {
                            true
                        }
                    )
                }
                if (isMapLoaded) {
                    UIMarkerInScreenCenter(resID = R.drawable.purple_pin) {
                        dragDropAnimatable.value
                    }
                }
                FilledIconButton(
                    onClick = viewModel::startMapLocation,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(imageVector = Icons.Rounded.MyLocation, contentDescription = null)
                }
            }
            ListItem(
                headlineContent = {
                    Text(text = "发送当前选中信息")
                }, supportingContent = {
                    Text(text = cameraPositionState.position.target.toString())
                }, modifier = Modifier.clickable {
                    onSelectLocationClick(cameraPositionState.position.target)
                }
            )
            Divider()
        }
        LazyColumn(modifier = Modifier.weight(0.5f)) {
            if (viewState.poiItems.isNullOrEmpty()) {
                item {
                    ListItem(
                        headlineContent = {
                            Text(text = "没有搜索结果")
                        }
                    )
                }
            }
            items(viewState.poiItems ?: emptyList(), key = { it.poiId }) {
                val title = it.title
                val cityName = it.cityName
                val addressName = it.adName
                val snippet = it.snippet
                ListItem(
                    headlineContent = {
                        Text(
                            text = title ?: "-",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    }, supportingContent = {
                        Text(
                            text = (cityName ?: "").plus(addressName ?: "").plus(snippet ?: ""),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }, modifier = Modifier.clickable {
                        onPoiClick(it)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
@Composable
fun BoxScope.UIMarkerInScreenCenter(@DrawableRes resID: Int, dragDropAnimValueProvider: () -> Size) {
    Image(
        modifier = Modifier
            .align(Alignment.Center)
            .drawBehind {
                drawOval(
                    color = Color.Gray.copy(alpha = 0.7F),
                    topLeft = Offset(
                        size.width / 2 - dragDropAnimValueProvider().width / 2,
                        size.height / 2 - 18F
                    ),
                    size = dragDropAnimValueProvider()
                )
            }
            .graphicsLayer {
                translationY = -(dragDropAnimValueProvider().width.coerceAtLeast(5F) / 2)
            },
        painter = painterResource(id = resID),
        contentDescription = null
    )
}
@Composable
fun handlerGPSLauncher(block: () -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        block.invoke()
    }
}

data class DragDropSelectPointViewState(
    // 是否点击了强制定位
    val isClickForceStartLocation: Boolean = false,
    // 是否打开了系统GPS权限
    val isOpenGps: Boolean? = null,
    // 是否显示打开GPS的确认弹框
    val isShowOpenGPSDialog: Boolean = false,
    // 当前用户自身定位所在的位置
    val currentLocation: LatLng? = null,
    // 当前手持设备的方向
    val currentRotation: Float = 0F,
    // poi列表
    val poiItems: List<PoiItemV2>? = null,
)

@HiltViewModel
class DragDropSelectPointViewModel @Inject constructor(
    private val application: Application
) : ViewModel(), AMapLocationListener, PoiSearchV2.OnPoiSearchListener {
    private val _viewState = MutableStateFlow(DragDropSelectPointViewState())
    val viewState = _viewState.asStateFlow()
    private var locationClient: AMapLocationClient? = null
    private var poiItemQuery: PoiSearchV2.Query? = null
    private var poiItemSearch: PoiSearchV2? = null
    private val jobs: MutableList<Job> = mutableListOf()
    fun checkGpsStatus() = viewModelScope.launch(Dispatchers.IO) {
        val isOpenGps = isGPSOpened()
        _viewState.update { it.copy(isOpenGps = isOpenGps, isShowOpenGPSDialog = !isOpenGps) }
    }.also {
        jobs.add(it)
    }

    fun isGPSOpened(): Boolean {
        val locationManager = application.getSystemService<LocationManager>()
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    fun showDialog() {
        _viewState.update { it.copy(isShowOpenGPSDialog = true) }
    }

    fun hideDialog() {
        _viewState.update { it.copy(isShowOpenGPSDialog = false) }
    }

    fun startMapLocation() = viewModelScope.launch(Dispatchers.IO) {
        if (_viewState.value.isClickForceStartLocation) return@launch
        _viewState.update { it.copy(isClickForceStartLocation = true) }
        restartLocation(
            locationClient = locationClient,
            listener = this@DragDropSelectPointViewModel
        ) {
            locationClient = it
        }
    }.also {
        jobs.add(it)
    }

    fun restartLocation(
        locationClient: AMapLocationClient?,
        listener: AMapLocationListener,
        block: (AMapLocationClient) -> Unit
    ) {
        locationClient?.setLocationListener(null)
        locationClient?.stopLocation()
        val client = AMapLocationClient(application)
        client.setLocationListener(listener)
        client.setLocationOption(
            AMapLocationClientOption().apply {
                isOnceLocation = true
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            }
        )
        block.invoke(
            client.apply {
                startLocation()
            }
        )
    }

    override fun onLocationChanged(location: AMapLocation?) {
        _viewState.update { it.copy(isClickForceStartLocation = false) }
        if (null == location) {
            application.toast("定位失败，请检查定位权限与网络")
            return
        }
        val latitude = location.latitude
        val longitude = location.longitude
        _viewState.update {
            it.copy(currentLocation = LatLng(latitude, longitude))
        }

    }

    fun doSearchQueryPoi(latLng: LatLng) = viewModelScope.launch(Dispatchers.IO) {
        doSearchQueryPoi(
            searchV2 = poiItemSearch,
            moveLatLonPoint = LatLonPoint(latLng.latitude, latLng.longitude),
            listener = this@DragDropSelectPointViewModel
        ) { a, b ->
            poiItemQuery = a
            poiItemSearch = b
        }
    }.also {
        jobs.add(it)
    }

    fun doSearchQueryPoi(
        searchV2: PoiSearchV2?,
        moveLatLonPoint: LatLonPoint?,
        listener: PoiSearchV2.OnPoiSearchListener,
        block: (PoiSearchV2.Query, PoiSearchV2) -> Unit
    ) {
        if (moveLatLonPoint != null) {
            // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
            val poiItemQuery = PoiSearchV2.Query("", "", "").apply {
                cityLimit = true
                pageSize = 20
                pageNum = 0
            }
            val newSearch2: PoiSearchV2
            if (null == searchV2) {
                newSearch2 = PoiSearchV2(application, poiItemQuery)
                newSearch2.setOnPoiSearchListener(listener)
            } else {
                newSearch2 = searchV2
            }
            newSearch2.bound = PoiSearchV2.SearchBound(moveLatLonPoint, 1000, true)
            newSearch2.searchPOIAsyn()

            block.invoke(poiItemQuery, newSearch2)
        }
    }

    override fun onPoiSearched(poiResult: PoiResultV2?, resultCode: Int) {
        handlePoiSearched(poiItemQuery, poiResult, resultCode) { poiList ->
            _viewState.update { it.copy(poiItems = poiList) }
        }
    }

    override fun onPoiItemSearched(p0: PoiItemV2?, p1: Int) {
    }

    fun handlePoiSearched(
        query: PoiSearchV2.Query?,
        poiResult: PoiResultV2?,
        resultCode: Int,
        block: (List<PoiItemV2>) -> Unit
    ) {
        if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
            if (poiResult?.query != null) {
                if (poiResult.query == query) {
                    val poiItems = poiResult.pois
                    if (poiItems != null && poiItems.size > 0) {
                        block.invoke(poiItems)
                        return
                    }
                }
            }
            //无搜索结果
            block.invoke(emptyList())
        }
    }

    fun openGPSPermission(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if (isGPSOpened()) {
            application.applicationSettings()
        } else {
            launcher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    override fun onCleared() {
        jobs.forEach {
            if (!it.isCancelled) {
                it.cancel()
            }
        }
        jobs.clear()
        locationClient?.setLocationListener(null)
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
        super.onCleared()
    }
}
