package cn.tabidachi.electro.amap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.geometry.Offset
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.MultiPointItem
import com.amap.api.maps.model.MultiPointOverlay
import com.amap.api.maps.model.MultiPointOverlayOptions

internal class MultiPointOverlayNode(
    val multiPointOverlay: MultiPointOverlay,
    var onPointItemClick: (MultiPointItem) -> Unit
) : MapNode {
    override fun onRemoved() {
        multiPointOverlay.remove()
    }
}

/**
 * MultiPointOverlay
 * @param enable 是否可用
 * @param anchor 锚点
 * @param icon 图标
 * @param multiPointItems 海量点中某个点的位置及其他信息
 */
@Composable
@AMapComposable
fun MultiPointOverlay(
    enable: Boolean,
    anchor: Offset = Offset(0.5F, 0.5F),
    icon: BitmapDescriptor,
    multiPointItems: List<MultiPointItem>,
    onClick: (MultiPointItem) -> Unit
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<MultiPointOverlayNode, MapApplier>(
        factory = {
            val multiPointOverlay =
                mapApplier?.map?.addMultiPointOverlay(MultiPointOverlayOptions().apply {
                    this.anchor(anchor.x, anchor.y)
                    this.setEnable(enable)
                    this.icon(icon)
                }) ?: error("Error adding MultiPointOverlay")
            multiPointOverlay.items = multiPointItems
            MultiPointOverlayNode(multiPointOverlay, onClick)
        },
        update = {
            update(onClick) { this.onPointItemClick = it }

            set(anchor) { this.multiPointOverlay.setAnchor(it.x, it.y) }
            set(enable) { this.multiPointOverlay.setEnable(it) }
            set(multiPointItems) { this.multiPointOverlay.items = it }
        }
    )
}