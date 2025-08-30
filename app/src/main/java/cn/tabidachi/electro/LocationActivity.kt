package cn.tabidachi.electro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import cn.tabidachi.electro.model.attachment.LocationAttachment
import cn.tabidachi.electro.ui.map.DragDropSelectPointScreen
import cn.tabidachi.electro.ui.theme.ElectroTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationActivity : ComponentActivity() {
    private val electroViewModel: ElectroViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ElectroTheme(
                darkLight = electroViewModel.darkLight,
                theme = electroViewModel.theme
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DragDropSelectPointScreen(
                        onBack = {
                            finishAndRemoveTask()
                        }, onSelectLocationClick = {
                            val attachment = LocationAttachment {
                                latitude = it.latitude
                                longitude = it.longitude
                            }
                            val intent = Intent().apply {
                                putExtra("attachment", attachment)
                            }
                            setResult(114514, intent)
                            finishAndRemoveTask()
                        }, onPoiClick = {
                            val attachment = LocationAttachment {
                                latitude = it.latLonPoint.latitude
                                longitude = it.latLonPoint.longitude
                                title = it.title
                                city = it.cityName
                                address = it.adName
                                snippet = it.snippet
                            }
                            val intent = Intent().apply {
                                putExtra("attachment", attachment)
                            }
                            setResult(114514, intent)
                            finishAndRemoveTask()
                        }
                    )
                }
            }
        }
    }
}