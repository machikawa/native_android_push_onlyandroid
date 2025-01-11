package tokyo.trmotors.native_android_push

import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap.Title
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


fun showNotificationNow(context: Context, title: String, content: String, channelID: String){
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, channelID)
        .setSmallIcon(R.drawable.ic_notification2) // アイコンを指定
        .setContentTitle(title)                    // 通知タイトル
        .setContentText(content)                   // 通知内容
        .setPriority(NotificationCompat.PRIORITY_HIGH) // 優先度を設定
        .setContentIntent(pendingIntent)           // 通知タップ時の遷移
        .setAutoCancel(true)                       // 通知タップ後に消す

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(1, builder.build())
}