package si.uckoquiz.app.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import si.uckoquiz.app.MainActivity
import si.uckoquiz.app.R
import si.uckoquiz.app.ui.onboarding.LocalProfileStore
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/** UQ-17: dnevni opomnik (WorkManager). Ura je približna, Android lahko opomnik zamakne za nekaj minut. */
object Reminders {
    private const val WORK_NAME = "daily_reminder"
    private const val CHANNEL_ID = "daily_reminder"

    /** Vklopi ali izklopi opomnik glede na nastavitve. */
    fun apply(context: Context, settings: AppSettings) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (!settings.reminders) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val time = runCatching { LocalTime.parse(settings.reminderTime) }.getOrDefault(LocalTime.of(18, 0))
        val request = PeriodicWorkRequest.Builder(ReminderWorker::class.java, 24, TimeUnit.HOURS)
            .setInitialDelay(delayUntil(time).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    /** Koliko časa je do naslednje ure [time] (danes, če še ni minila, sicer jutri). */
    internal fun delayUntil(time: LocalTime, now: LocalDateTime = LocalDateTime.now()): Duration {
        var next = now.toLocalDate().atTime(time)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun show(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Dnevni opomnik", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Učko te čaka")
            .setContentText("Danes še nisi vadil. Ena lekcija traja le nekaj minut.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(1, notification)
    }
}

/** Opomni samo, če učenec danes še ni opravil lekcije. */
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        val studiedToday = LocalProfileStore(context).load()?.lastActiveDate == LocalDate.now().toString()
        if (!studiedToday && Reminders.canNotify(context)) {
            runCatching { Reminders.show(context) }
        }
        return Result.success()
    }
}

/** Zažene opomnik po uvodu in ob vsaki spremembi nastavitev. Na Androidu 13+ vpraša za dovoljenje. */
@Composable
fun ReminderEffect(profileReady: Boolean, settings: AppSettings) {
    val context = LocalContext.current
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(profileReady, settings.reminders, settings.reminderTime) {
        if (!profileReady) return@LaunchedEffect
        runCatching { Reminders.apply(context, settings) }
        if (settings.reminders && Build.VERSION.SDK_INT >= 33 && !Reminders.canNotify(context)) {
            askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}