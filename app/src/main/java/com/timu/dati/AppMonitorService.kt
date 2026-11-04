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
    private var locked = false  // 是否正在答题锁定中
    private var currentPkg: String? = null
    private val handler = Handler(Looper.getMainLooper())

    private val tickRunnable = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, 800L)
        }
    }

    private val ignorePkgs = mutableSetOf<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        buildIgnorePkgs()
        handler.post(tickRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 事件不做关键判断，全部交给tick统一处理
    }

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        handler.removeCallbacksAndMessages(null)
        QuizOverlayController.get(this).hide()
        return super.onUnbind(intent)
    }

    private fun tick() {
        // 已经在答题锁定中，不重复触发
        if (locked) return

        val fg = getForegroundByWindows() ?: return
        val monitored = Prefs.getMonitoredApps(this)

        // 自己的APP或忽略包，跳过
        if (fg == packageName || ignorePkgs.contains(fg)) return

        if (monitored.contains(fg)) {
            currentPkg = fg
            triggerQuiz()
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
            // 答完题后不主动goHome，让用户继续用刚打开的APP
        }
        overlay.onCancel = {
            locked = false
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
