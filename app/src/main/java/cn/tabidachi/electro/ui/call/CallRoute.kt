package cn.tabidachi.electro.ui.call

import android.app.Activity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import cn.tabidachi.electro.ext.findActivity
import cn.tabidachi.electro.ui.call.CallContract.Event
import kotlinx.serialization.Serializable
import moe.tabidachi.compose.mvi.observe

@Serializable
data class CallRoute(val offer: Long, val answer: Long, val action: String)

fun NavGraphBuilder.call() = composable<CallRoute> {
    val context = LocalContext.current
    val view = LocalView.current
    val route = it.toRoute<CallRoute>()
    val viewModel: CallViewModel = hiltViewModel()
    val (state, event) = viewModel.observe {
        when (it) {
            CallContract.Effect.OnCallEnd -> {
                (context as? Activity)?.finishAndRemoveTask()
            }
        }
    }
    LaunchedEffect(Unit) {
        event(
            Event.InitCall(
                offer = route.offer,
                answer = route.answer,
                action = route.action
            )
        )
    }
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window ?: return@DisposableEffect onDispose { }
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }
    val localVideoTrack by viewModel.factory.localVideoTrack.collectAsState(null)
    val remoteVideoTrack by viewModel.factory.remoteVideoTrack.collectAsState(null)
    CallScreen(
        state = state.value,
        event = event,
        localVideoTrack = localVideoTrack,
        remoteVideoTrack = remoteVideoTrack,
        eglBaseContext = viewModel.factory.eglBaseContext,
    )
}
