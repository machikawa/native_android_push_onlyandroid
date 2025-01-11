package tokyo.trmotors.native_android_push

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.util.TimeZone

@Composable
fun NotificationSchedulerUI(context: Context) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 背景画像
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "背景画像",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // カスタムSnackbarHost
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth(),
                snackbar = { snackbarData ->
                    Snackbar(
                        containerColor = Color.Black, // 背景色を黒に
                        contentColor = Color.Yellow, // 文字色を黄色に
                    ) {
                        // メッセージの表示
                        Text(
                            text = snackbarData.visuals.message,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Yellow // テキストの色を黄色に指定
                            )
                        )
                        // アクションボタンをカスタマイズ（必要なら追加）
                        snackbarData.visuals.actionLabel?.let { actionLabel ->
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.Cyan // アクションの色を変更
                                ),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            )

            // ヘッダー部分のCard
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "通知スケジューラー",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "ここで通知を設定できます。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 通知タイトル入力
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("タイトル") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 通知内容入力
            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("コンテンツ") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 通知時刻
            Text(
                text = "通知時刻 : ${selectedTime.time}",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    val updatedTime = (selectedTime.clone() as Calendar).apply {
                        add(Calendar.MINUTE, 1)
                    }
                    selectedTime = updatedTime
                }) {
                    Text(text = "+1 Minute")
                }

                Button(onClick = {
                    val now = Calendar.getInstance()
                    if (selectedTime.before(now)) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "未来の日付を設定してください！",
                                withDismissAction = true,
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        scheduleNotification(context, title, content, selectedTime)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "${selectedTime.time}に通知を設定しました！",
                                withDismissAction = true,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }) {
                    Text(text = "Schedule Notification")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showNotificationNow(context, title, content, "Mach") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "今すぐ通知するやで")
            }
        }
    }
}

// 通知の予約処理
fun scheduleNotification(context: Context, title: String, content: String, calendar: Calendar) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    if (alarmManager == null) {
        Log.e("AlarmManager", "Failed to retrieve AlarmManager")
        return
    }


    // ログで確認
    Log.d(
        "AlarmManager",
        "Alarm set for: ${calendar.time} in time zone: ${TimeZone.getDefault().id}"
    )
    try {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("content", content)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE  // FLAG_UPDATE_CURRENT を追加
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("AlarmManager", "Exact alarm permission is not granted. Falling back to non-exact alarm.")
            // 権限がない場合は正確でないアラームをスケジュール
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, // ELAPSED_REALTIME_WAKEUP に変更 (必要に応じて)
                SystemClock.elapsedRealtime() + (calendar.timeInMillis - System.currentTimeMillis()), // 現在時刻からの差分で指定
                pendingIntent
            )
        } else {
            Log.w("AlarmManager22", "きわきわ. Falling back to non-exact alarm.")
            // 権限がある場合は正確なアラームをスケジュール
            alarmManager.setExactAndAllowWhileIdle( // より適切な方法に変更
                AlarmManager.ELAPSED_REALTIME_WAKEUP, // ELAPSED_REALTIME_WAKEUP に変更 (必要に応じて)
                SystemClock.elapsedRealtime() + (calendar.timeInMillis - System.currentTimeMillis()), // 現在時刻からの差分で指定
                pendingIntent
            )
        }

        Log.d("AlarmManager", "Alarm scheduled successfully")
    } catch (e: SecurityException) {
        Log.e("AlarmManager", "Failed to schedule alarm: ${e.message}")
    }
}



/// 正確な通知を送るためにはこれが必要だが、設定画面に富んでしまうので割愛する
fun showPermissionDialog(context: Context) {
    val builder = android.app.AlertDialog.Builder(context)
    builder.setTitle("権限が必要です")
        .setMessage("正確なアラームを設定するには権限が必要です。設定画面を開きますか？")
        .setPositiveButton("設定を開く") { _, _ ->
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
        .setNegativeButton("キャンセル") { dialog, _ ->
            dialog.dismiss()
        }
    builder.create().show()
}
