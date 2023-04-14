package cn.tabidachi.electro

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import cn.tabidachi.electro.data.network.Ktor
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class ElectroMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    @Inject lateinit var ktor: Ktor
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
        ElectroNotification(this).createNotificationChannel()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived: ${message.data}")
        if (message.data["type"] == "call") {
            val nid = Random.nextInt()
            val data = message.data.apply {
                put("notification_id", nid.toString())
            }
            val image = data["image"]?.let { ktor.convert(it).buildString() }
            val bitmap = runBlocking {
                image?.let {
                    val channel = ktor.upload.get(it).bodyAsChannel()
                    BitmapFactory.decodeStream(channel.toInputStream())
                }
            }
            val notification = ElectroNotification(this).call(bitmap, data)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this).notify(nid, notification)
            }
        }
    }

    companion object {
        val TAG = ElectroMessagingService::class.simpleName
    }
}

class ElectroNotification(
    private val context: Context
) {
    private val icon = context.getDrawable(R.drawable.transparent_akkarin)?.toBitmap()
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANEL_ID,
            NOTIFICATION_CHANEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = NOTIFICATION_CHANEL_DESCRIPTION
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000, 1000, 1000)
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).run {
            createNotificationChannel(channel)
        }
    }

    fun call(image: Bitmap?, data: Map<String, String>): Notification {
        val declineIntent = Intent(context, ElectroReceiver::class.java).apply {
            action = DECLINE_ACTION
            data.forEach {
                putExtra(it.key, it.value)
            }
        }
        val answerIntent = Intent(context, CallActivity::class.java).apply {
            action = ANSWER_ACTION
            data.forEach {
                putExtra(it.key, it.value)
            }
        }
        val declinePendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                declineIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
        val answerPendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                answerIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            action = CALL_ACTION
            data.forEach {
                putExtra(it.key, it.value)
            }
        }
        val contentPendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
        val declineAction = NotificationCompat.Action(
            R.drawable.round_call_end_24,
            getActionText(R.string.decline, R.color.decline),
            declinePendingIntent
        )
        val answerAction = NotificationCompat.Action(
            R.drawable.round_call_24,
            getActionText(R.string.answer, R.color.answer),
            answerPendingIntent
        )
        return NotificationCompat.Builder(context, NOTIFICATION_CHANEL_ID)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setContentTitle(data["username"])
            .setLargeIcon(image ?: icon)
            .addAction(declineAction)
            .addAction(answerAction)
            .setAutoCancel(true)
            .setFullScreenIntent(answerPendingIntent, true)
            .setTimeoutAfter(15000)
            .build()
    }

    private fun getActionText(@StringRes stringRes: Int, @ColorRes colorRes: Int): SpannableString {
        val spannable = SpannableString(context.getText(stringRes))
        spannable.setSpan(
            ForegroundColorSpan(context.getColor(colorRes)), 0, spannable.length, 0
        )
        return spannable
    }

    companion object {
        const val NOTIFICATION_CHANEL_ID = "notification_channel_call"
        const val NOTIFICATION_CHANEL_NAME = "呼叫"
        const val NOTIFICATION_CHANEL_DESCRIPTION = "呼叫"
    }
}

const val DECLINE_ACTION = "cn.tabidachi.electro.ACTION_CALL_DECLINE"
const val ANSWER_ACTION = "cn.tabidachi.electro.ACTION_CALL_ANSWER"
const val OFFER_ACTION = "cn.tabidachi.electro.ACTION_CALL_OFFER"
const val CALL_ACTION = "cn.tabidachi.electro.ACTION_CALL"