package tokyo.trmotors.native_android_push

import android.app.NotificationChannel
import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import tokyo.trmotors.native_android_push.ui.theme.Native_android_pushTheme
import android.app.AlarmManager
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "Mach"
    private val NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 通知チャネルの作成
        createNotificationChannel(this)

        // 通知権限の確認とリクエスト
        checkNotificationPermission()

        // 通知の送信 : 初期で作成
//        sendNotification(this)

        // Firebase FCM トークンの取得 --- Remote notification対応
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM😂😂😂", "FCM Token: $token")
                // 必要であればトークンをサーバーに送信
                // sendTokenToServer(token)
            } else {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
            }
        }


        setContent {
            Native_android_pushTheme {
                NotificationSchedulerUI(context = this)
            }
        }
    }

    // Android 13以上の場合に通知権限を確認し、必要ならユーザーにリクエストを送る
    private fun checkNotificationPermission() {

        val requiredPermissions = mutableListOf<String>()

        // 通知権限の確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // アラーム権限の確認（Android 12以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager != null) {
                // AlarmManagerが取得できた場合の処理
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }
            } else {
                Log.e("AlarmManager", "Failed to get AlarmManager")
            }
        }

        // 必要な権限をリクエスト
        if (requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                requiredPermissions.toTypedArray(),
                1001 // 任意のリクエストコード
            )
        }
    }



    // Android 8以上の場合に通知チャネルを作成する
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Channel NotificationChannelの第2引き数",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    // 通知を作成し、通知マネージャを通じて表示する
    private fun sendNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification2)
            .setContentTitle("My Notification")
            .setContentText("This is my notification message.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            try {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                Log.e("Notification", "Failed to send notification due to missing permission", e)
            }
        } else {
            Log.w("Notification", "POST_NOTIFICATIONS permission not granted")
        }


    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Native_android_pushTheme {
//        Greeting("Android")
//    }
//}

