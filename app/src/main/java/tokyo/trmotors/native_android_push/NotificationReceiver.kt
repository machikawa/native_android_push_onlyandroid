package tokyo.trmotors.native_android_push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.w("NotificationReceiver", "context or intent is null")
            return
        }

        val title = intent.getStringExtra("title") ?: "タイトルはありません"
        val content = intent.getStringExtra("content") ?: "コンテンツがありません"

        Log.d("NotificationReceiver", "Received Notification: title=$title, content=$content")

        showNotificationNow(context, title, content, "Mach")
    }
}
