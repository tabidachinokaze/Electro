package moe.tabidachi.electro

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import moe.tabidachi.electro.ext.checkPermission
import moe.tabidachi.electro.ui.call.CallRoute
import moe.tabidachi.electro.ui.call.call
import moe.tabidachi.electro.ui.theme.ElectroTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (!application.checkPermission(Manifest.permission.CAMERA)) {
            finishAndRemoveTask()
            return
        }
        val src = intent?.getStringExtra("src")?.toLong() ?: run {
            finishAndRemoveTask()
            return
        }
        val dst = intent?.getStringExtra("dst")?.toLong() ?: run {
            finishAndRemoveTask()
            return
        }
        intent?.getStringExtra("notification_id")?.let {
            NotificationManagerCompat.from(this).cancel(it.toInt())
        }
        val action = when (intent.action) {
            OFFER_ACTION -> {
                OFFER_ACTION
            }

            ANSWER_ACTION -> {
                ANSWER_ACTION
            }

            else -> run {
                finishAndRemoveTask()
                return
            }
        }

        setContent {
            ElectroTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    NavHost(
                        navController = rememberNavController(),
                        startDestination = CallRoute(
                            offer = src,
                            answer = dst,
                            action = action
                        )
                    ) {
                        call()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("notification_id")?.let {
            NotificationManagerCompat.from(this).cancel(it.toInt())
        }
    }
}
