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
    private var locked = false
    private var currentPkg: String? = null
    private val handler = Handler(Looper.getMainLooper())

    // 使用时长计时（秒）
    private var usageSeconds = 0L
    private var lastTickTime = 0L

    private val tickRunnable = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, 1000L)  // 每秒tick一次
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
        if (locked) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || ignorePkgs.contains(pkg)) return
        val monitored = Prefs.getMonitoredApps(this)
        if (monitored.contains(pkg) && pkg != currentPkg) {
            // 切换了APP，重置计时
            currentPkg = pkg
            usageSeconds = 0L
            // 如果是打开就弹模式，直接触发
            if (Prefs.getPopupMode(this) == 0) {
                handler.post { triggerQuiz() }
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
        if (locked) return

        val now = System.currentTimeMillis()
        val elapsed = (now - lastTickTime) / 1000
        lastTickTime = now

        val fg = getForegroundByWindows() ?: return
        val monitored = Prefs.getMonitoredApps(this)

        if (fg == packageName || ignorePkgs.contains(fg)) return

        if (monitored.contains(fg)) {
            // 是管控APP，累计使用时长
            usageSeconds += elapsed

            val mode = Prefs.getPopupMode(this)
            if (mode == 0) {
                // 打开就弹模式，已经在onAccessibilityEvent里触发了
            } else {
                // 定时弹模式：用了N分钟后弹
                val intervalSec = Prefs.getPopupIntervalMin(this) * 60L
                if (usageSeconds >= intervalSec && currentPkg != null) {
                    triggerQuiz()
                }
            }
        } else {
            // 不在管控APP里，重置计时
            usageSeconds = 0L
            currentPkg = null
        }
    }

    /**
     * 触发答题遮罩
     */
    private fun triggerQuiz() {
        locked = true
        val overlay = QuizOverlayController.get(this)
        if (overlay.isShowing) {
            overlay.hide()
        }
        overlay.onComplete = {
            locked = false
            currentPkg = null  // 重置，退出再打开还能再弹
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
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        instance = null
    }
}
