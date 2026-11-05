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
    private var lastPkg: String? = null
    private var usageSeconds = 0L
    private var lastTickTime = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val tickRunnable = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, 1000L)  // 每1秒检测一次
        }
    }

    // 守护线程：答题界面显示期间，每500毫秒检查一次，如果被关掉就重新显示
    private val watchdogRunnable = object : Runnable {
        override fun run() {
            if (!locked) return
            val overlay = QuizOverlayController.get(this@ForegroundMonitorService)
            if (!overlay.isShowing) {
                // 答题界面被关掉了，重新显示
                overlay.show(currentPkg ?: "unknown")
            }
            handler.postDelayed(this, 500L)
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

        val mode = Prefs.getPopupMode(this)
        val intervalSec = Prefs.getPopupIntervalMin(this) * 60L

        // 定时弹模式：不管前台是什么APP，每隔N分钟弹一次
        if (mode == 1) {
            usageSeconds += elapsed
            if (usageSeconds >= intervalSec) {
                val overlay = QuizOverlayController.get(this)
                overlay.onComplete = { unlock() }
                overlay.onCancel = { unlock() }
                overlay.show("timer")
                locked = true
                usageSeconds = 0L
                handler.post(watchdogRunnable)  // 启动守护
            }
            return
        }

        // 打开就弹模式：只在管控APP里才计时
        val monitored = Prefs.getMonitoredApps(this)
        if (monitored.isEmpty()) return

        // 用UsageStatsManager检测前台APP
        val fg = getForegroundByUsageStats()
        var inMonitoredApp = false
        if (fg != null) {
            currentPkg = fg
            // 忽略系统包
            if (!fg.startsWith("com.android.") && fg != "com.miui.home") {
                inMonitoredApp = monitored.contains(fg)
            }
        }

        if (mode == 0) {
            // 打开就弹模式：刚进入管控APP就立即弹一次，然后每隔N分钟弹一次
            if (inMonitoredApp) {
                // 刚从非管控APP切换到管控APP，立即弹一次
                if (lastPkg != fg) {
                    val overlay = QuizOverlayController.get(this)
                    overlay.onComplete = { unlock() }
                    overlay.onCancel = { unlock() }
                    overlay.show(fg ?: "unknown")
                    locked = true
                    usageSeconds = 0L
                    lastPkg = fg
                    handler.post(watchdogRunnable)  // 启动守护
                    return
                }
                usageSeconds += elapsed
                if (usageSeconds >= intervalSec) {
                    val overlay = QuizOverlayController.get(this)
                    overlay.onComplete = { unlock() }
                    overlay.onCancel = { unlock() }
                    overlay.show(fg ?: "unknown")
                    locked = true
                    usageSeconds = 0L
                    handler.post(watchdogRunnable)  // 启动守护
                }
            } else {
                // 不在管控APP里，重置计时
                usageSeconds = 0L
                lastPkg = fg
            }
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
        handler.removeCallbacks(watchdogRunnable)  // 停止守护
    }

    fun resetState() {
        locked = false
        currentPkg = null
        usageSeconds = 0L
    }
}
