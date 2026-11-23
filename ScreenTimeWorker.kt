package com.family.guardian.service

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.family.guardian.network.ApiClient
import java.util.Calendar

/**
 * Runs every ~15 min (scheduled by WorkManager in MainActivity / BootReceiver).
 * Reads today's per-app foreground time from UsageStatsManager — this requires the
 * PACKAGE_USAGE_STATS permission, which the child grants once via a system settings
 * screen (Settings > Apps > Special access > Usage access), a deliberate, visible step.
 */
class ScreenTimeWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now)
            .filter { it.totalTimeInForeground > 0 }
            .associate { it.packageName to it.totalTimeInForeground / 60000 } // minutes

        val totalMinutes = stats.values.sum()

        return try {
            ApiClient.service.reportUsage(
                childId = ApiClient.childId(applicationContext),
                totalMinutesToday = totalMinutes,
                perAppMinutes = stats
            )
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
