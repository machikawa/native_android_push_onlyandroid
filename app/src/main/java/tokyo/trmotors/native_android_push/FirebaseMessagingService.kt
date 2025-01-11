package tokyo.trmotors.native_android_push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // サーバーにトークンを送信する処理を追加（必要なら）
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.data}")

        // 通知メッセージが存在する場合
        remoteMessage.notification?.let {
            Log.d("FCM", "Notification Title: ${it.title}")
            Log.d("FCM", "Notification Body: ${it.body}")
            showNotification(it.title ?: "通知", it.body ?: "内容")
        }
    }


    // 通知を表示するメソッド
    private fun showNotification(title: String, body: String) {
        val channelId = "Machi" // 通知チャネルID
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 通知チャネルを作成（Android 8.0以上の場合）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "My Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 通知を作成
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification2) // 適切なアイコンを設定
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // 通知を表示
        notificationManager.notify(0, notification)
    }

}


