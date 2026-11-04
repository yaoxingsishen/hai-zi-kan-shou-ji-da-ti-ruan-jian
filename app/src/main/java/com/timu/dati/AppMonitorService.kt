package com.timu.dati

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo

/**
 * 无障碍服务：监测前台APP，打开被管控APP时立即弹出答题遮罩。
 *
 * 改造点（相比原"戒手机"项目）：
 * - 原逻辑：连续使用X分钟后弹倒计时遮罩
 * - 新逻辑：一进入被管控APP立即弹答题遮罩，答对N道题才放行
 */
class AppMonitorService : AccessibilityService() {

    @Volatile
    var locked = false
        private set
    var currentPkg: String? = null
        private set
    // 调试用：最近无障碍事件的包名
    @Volatile
    var lastEventPkg: String? = null
        private set
    private val handler = Handler(Looper.getMainLooper())

    // 使用时长计时（秒）
    var usageSeconds = 0L
        private set
    private var lastTickTime = 0L

    // 重置所有状态（调试用）
    fun resetState() {
        locked = false
        currentPkg = null
        usageSeconds = 0L
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, 500L)  // 每0.5秒tick一次
        }
    }

    private val ignorePkgs = mutableSetOf<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        buildIgnorePkgs()
        lastTickTime = System.currentTimeMillis()
        handler.post(tickRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        lastEventPkg = pkg  // 记录最近事件的包名，调试用
        if (locked) return
        if (pkg == packageName || ignorePkgs.contains(pkg)) return

        // 不管是不是管控APP，都更新currentPkg，作为兜底
        if (pkg != currentPkg) {
            currentPkg = pkg
            val monitored = Prefs.getMonitoredApps(this)
            if (monitored.contains(pkg)) {
                usageSeconds = 0L
                // 如果是打开就弹模式，直接触发
                if (Prefs.getPopupMode(this) == 0) {
                    handler.post { triggerQuiz() }
                }
            } else {
                // 切到非管控APP，重置计时
                usageSeconds = 0L
            }
        }
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        handler.removeCallbacksAndMessages(null)
        QuizOverlayController.get(this).hide()
        return super.onUnbind(intent)
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        val elapsed = (now - lastTickTime) / 1000
        lastTickTime = now

        if (locked) return

        val monitored = Prefs.getMonitoredApps(this)

        // 三重兜底检测前台包名
        var fg: String? = currentPkg  // 第一重：无障碍事件记录的
        if (fg == null) {
            fg = getForegroundByWindows()  // 第二重：getWindows轮询
        }
        if (fg == null) {
            fg = getForegroundByUsageStats()  // 第三重：UsageStatsManager
        }
        if (fg == null) return

        if (fg == packageName || ignorePkgs.contains(fg)) return

        if (monitored.contains(fg)) {
            // 是管控APP
            if (currentPkg != fg) {
                // 刚切换到这个APP
                currentPkg = fg
                usageSeconds = 0L
                // 打开就弹模式：检测到刚打开就弹
                if (Prefs.getPopupMode(this) == 0) {
                    handler.post { triggerQuiz() }
                }
            }
            usageSeconds += elapsed

            val mode = Prefs.getPopupMode(this)
            if (mode == 1) {
                // 定时弹模式：用了N分钟后弹
                val intervalSec = Prefs.getPopupIntervalMin(this) * 60L
                if (usageSeconds >= intervalSec) {
                    triggerQuiz()
                }
            }
        } else {
            // 不在管控APP里，重置计时
            usageSeconds = 0L
            // 注意：不重置currentPkg，因为getWindows可能拿不到，要保留无障碍事件的值
        }
    }

    /**
     * 第三重兜底：用UsageStatsManager获取前台APP
     */
    @Suppress("DEPRECATION")
    private fun getForegroundByUsageStats(): String? {
        return try {
            val usm = getSystemService("usagestats") as android.app.usage.UsageStatsManager
            val end = System.currentTimeMillis()
            val start = end - 1000 * 10
            val stats = usm.queryUsageStats(android.app.usage.UsageStatsManager.INTERVAL_BEST, start, end)
            if (stats.isNullOrEmpty()) return null
            var best: android.app.usage.UsageStats? = null
            for (s in stats) {
                if (best == null || s.lastTimeUsed > best.lastTimeUsed) {
                    best = s
                }
            }
            best?.packageName
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 触发答题遮罩
     */
    private fun triggerQuiz() {
        // 检查冷却时间：答对题后N分钟内不再弹
        val cooldownSec = Prefs.getCooldownMin(this) * 60L
        val lastPass = Prefs.getLastPassTime(this)
        val now = System.currentTimeMillis()
        if (now - lastPass < cooldownSec * 1000) {
            // 还在冷却期，不弹
            locked = false
            return
        }

        locked = true
        val overlay = QuizOverlayController.get(this)
        if (overlay.isShowing) {
            overlay.hide()
        }
        overlay.onComplete = {
            locked = false
            currentPkg = null
            // 记录放行时间，开始冷却
            Prefs.setLastPassTime(this, System.currentTimeMillis())
        }
        overlay.onCancel = {
            locked = false
            currentPkg = null
            goHome()
        }
        overlay.show(currentPkg ?: "")
    }

    /** 来电时撤遮罩 */
    fun onIncomingCall() {
        QuizOverlayController.get(this).hide()
        locked = false
    }

    private fun goHome() {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    /**
     * 用getWindows()取最上层应用窗口的包名作为前台APP
     */
    private fun getForegroundByWindows(): String? {
        val windows = windows ?: return null
        var best: AccessibilityWindowInfo? = null
        var bestLayer = Int.MIN_VALUE
        for (w in windows) {
            if (w.type != AccessibilityWindowInfo.TYPE_APPLICATION) continue
            if (w.layer > bestLayer) {
                bestLayer = w.layer
                best = w
            }
        }
        return best?.root?.packageName?.toString()
    }

    private fun buildIgnorePkgs() {
        ignorePkgs.add("com.android.systemui")
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.enabledInputMethodList.forEach { ignorePkgs.add(it.packageName) }
    }

    companion object {
        @Volatile
        var instance: AppMonitorService? = null
            private set

        // 调试用：暴露当前检测到的前台包名（三重检测）
        fun getCurrentForeground(): String? {
            val inst = instance ?: return null
            // 三重检测
            var fg = inst.currentPkg
            if (fg == null) fg = inst.getForegroundByWindows()
            if (fg == null) fg = inst.getForegroundByUsageStats()
            return fg
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // 启动前台服务，加常驻通知，防止后台被杀死
        val channelId = "quiz_monitor"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId, "答题锁服务", android.app.NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = android.app.Notification.Builder(this, channelId)
            .setContentTitle("答题锁运行中")
            .setContentText("正在后台监控管控应用")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        instance = null
    }
}
