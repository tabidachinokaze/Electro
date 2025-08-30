package cn.tabidachi.electro

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import cn.tabidachi.electro.ext.toast
import cn.tabidachi.electro.ui.ElectroNavGraph
import cn.tabidachi.electro.ui.common.ReleaseDialog
import cn.tabidachi.electro.ui.theme.ElectroTheme
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), DistributeListener {
    private val electroViewModel: ElectroViewModel by viewModels()
    private var updateDialogVisible by mutableStateOf(false)
    private var releaseDetails by mutableStateOf<ReleaseDetails?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        Distribute.setListener(this)
        setContentView(
            ElectroComposeView(this).apply {
                setContent {
                    ElectroTheme(
                        darkLight = electroViewModel.darkLight,
                        theme = electroViewModel.theme
                    ) {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            ElectroNavGraph()
                        }
                        ReleaseDialog(
                            visible = updateDialogVisible,
                            onDismissRequest = {
                                updateDialogVisible = false
                            }, releaseDetails = releaseDetails
                        )
                    }
                }
            }
        )
    }

    override fun onReleaseAvailable(activity: Activity, releaseDetails: ReleaseDetails): Boolean {
        this.releaseDetails = releaseDetails
        updateDialogVisible = true
        return true
    }

    override fun onNoReleaseAvailable(activity: Activity) {
        if (AppCenter.allowToast) {
            this.toast(this.resources.getString(R.string.no_update))
        }
    }
}

object AppCenter {
    var allowToast: Boolean = false
        get() {
            return field.also { field = false }
        }
}