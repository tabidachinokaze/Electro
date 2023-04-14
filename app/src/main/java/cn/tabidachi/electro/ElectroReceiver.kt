package cn.tabidachi.electro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectroReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra("notification_id")?.let {
            NotificationManagerCompat.from(context).cancel(it.toInt())
        }
    }
}