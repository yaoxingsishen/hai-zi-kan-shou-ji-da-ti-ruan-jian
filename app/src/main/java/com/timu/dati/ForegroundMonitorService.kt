package com.timu.dati

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.app.usage.UsageStatsManager

/**
 * 普通前台服务：用UsageStatsManager检测前台APP，不依赖无障碍服务。
 * 小米上无障碍服务经常被杀死，改用UsageStatsManager更可靠。
 */
class ForegroundMonitorService : Service() {

    companion object {
        var instance: ForegroundMonitorService? = null
            private set
    }

    @Volatile
    var locked = false
        private set
    @Volatile
    var currentPkg: String? = null
    private var usageSeconds = 0L
    private var lastTickTime = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val tickRunnable = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, 1000L)  // 每1秒检测一次
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startForegroundNotification()
        lastTickTime = System.currentTimeMillis()
        handler.post(tickRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        handler.removeCallbacks(tickRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundNotification() {
        val channelId = "foreground_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "前台监测服务",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("定时答题锁")
            .setContentText("正在监测前台APP...")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
        startForeground(1, notification)
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        val elapsed = (now - lastTickTime) / 1000
        lastTickTime = now

        if (locked) return

        val monitored = Prefs.getMonitoredApps(this)
        if (monitored.isEmpty()) return

        // 用UsageStatsManager检测前台APP
        val fg = getForegroundByUsageStats() ?: return
        currentPkg = fg

        // 忽略系统包，但不忽略自己（方便调试）
        if (fg.startsWith("com.android.") || fg == "com.miui.home") return

        if (monitored.contains(fg)) {
            // 是管控APP
            usageSeconds += elapsed
            val mode = Prefs.getPopupMode(this)
            if (mode == 0) {
                // 打开就弹模式：检测到刚打开就弹
                QuizOverlayController.get(this).show(fg)
                locked = true
                usageSeconds = 0L
            } else {
                // 定时弹模式：用了N分钟后弹
                val intervalSec = Prefs.getPopupIntervalMin(this) * 60L
                if (usageSeconds >= intervalSec) {
                    QuizOverlayController.get(this).show(fg)
                    locked = true
                    usageSeconds = 0L
                }
            }
        } else {
            // 不在管控APP里，重置计时
            usageSeconds = 0L
        }
    }

    private fun getForegroundByUsageStats(): String? {
        return try {
            val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val start = end - 1000 * 30  // 最近30秒
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
            if (stats.isNullOrEmpty()) return null
            var best: android.app.usage.UsageStats? = null
            for (s in stats) {
                if (best == null || s.lastTimeUsed > best.lastTimeUsed) best = s
            }
            best?.packageName
        } catch (e: Exception) {
            null
        }
    }

    fun unlock() {
        locked = false
    }

    fun resetState() {
        locked = false
        currentPkg = null
        usageSeconds = 0L
    }
}
