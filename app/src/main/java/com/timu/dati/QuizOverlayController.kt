package com.timu.dati

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

/**
 * 答题悬浮窗控制器：全屏覆盖，答对N道题才关闭。
 */
class QuizOverlayController private constructor(private val ctx: Context) {

    private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var rootView: View? = null
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    var isShowing = false
        private set

    var onComplete: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    private var correctCount: Int = 0
    private var needCount: Int = 0
    private var subject: String = "all"
    private var timePerQ: Int = 30
    private var timeLeft: Int = 0

    // UI
    private lateinit var tvProgress: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvType: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var optionsContainer: LinearLayout

    fun show(pkg: String) {
        if (isShowing) return

        needCount = Prefs.getAppQCount(ctx, pkg)
        subject = Prefs.getAppSubject(ctx, pkg)
        timePerQ = Prefs.getTimePerQ(ctx)
        correctCount = 0

        val density = ctx.resources.displayMetrics.density
        val dp = { v: Int -> (v * density).toInt() }
        val container = QuizFrameLayout(ctx)

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F3F4F6"))
            setPadding(dp(20), dp(40), dp(20), dp(20))
        }

        // 顶部栏：进度 + 倒计时 + 密码退出
        val topBar = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(12))
        }
        tvProgress = TextView(ctx).apply {
            text = "答对 0/$needCount"
            setTextColor(Color.parseColor("#4F46E5"))
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        tvTimer = TextView(ctx).apply {
            text = "${timePerQ}s"
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
        }
        val btnExit = TextView(ctx).apply {
            text = "退出"
            setTextColor(Color.parseColor("#9CA3AF"))
            textSize = 13f
            setPadding(dp(8), dp(4), 0, dp(4))
            setOnClickListener { showPinDialog() }
        }
        topBar.addView(tvProgress)
        topBar.addView(tvTimer)
        topBar.addView(btnExit)
        root.addView(topBar)

        // 标题卡片
        val headerCard = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            gravity = Gravity.CENTER
        }
        headerCard.addView(TextView(ctx).apply {
            text = "📚 答题解锁"
            textSize = 19f
            setTypeface(typeface, Typeface.BOLD)
        })
        headerCard.addView(TextView(ctx).apply {
            text = "答对 $needCount 道题即可继续"
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 12f
            setPadding(0, dp(4), 0, 0)
        })
        root.addView(headerCard)

        // 题目卡片
        val qCard = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(18), dp(18), dp(18), dp(18))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        }
        tvType = TextView(ctx).apply {
            setPadding(dp(10), dp(4), dp(10), dp(4))
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#DBEAFE"))
                cornerRadius = dp(20).toFloat()
            }
            setTextColor(Color.parseColor("#2563EB"))
        }
        qCard.addView(tvType)
        tvQuestion = TextView(ctx).apply {
            setTextColor(Color.parseColor("#1F2937"))
            textSize = 17f
            setPadding(0, dp(14), 0, dp(14))
            setLineSpacing(dp(3).toFloat(), 1.3f)
        }
        qCard.addView(tvQuestion)
        optionsContainer = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        qCard.addView(optionsContainer)
        root.addView(qCard)

        container.addView(root)
        rootView = container

        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        try {
            wm.addView(container, params)
            isShowing = true
            loadNextQuestion()
        } catch (e: Exception) {
            isShowing = false
            rootView = null
            onCancel?.invoke()
        }
    }

    private fun loadNextQuestion() {
        val q = QuizBank.randomQuestion(ctx, subject)
        val density = ctx.resources.displayMetrics.density
        val dp = { v: Int -> (v * density).toInt() }

        if (q.type == "judge") {
            tvType.text = "判断题"
            tvType.setTextColor(Color.parseColor("#D97706"))
            (tvType.background as GradientDrawable).setColor(Color.parseColor("#FEF3C7"))
        } else {
            tvType.text = "单选题 · ${QuizBank.getSubjectName(q.subject)}"
            tvType.setTextColor(Color.parseColor("#2563EB"))
            (tvType.background as GradientDrawable).setColor(Color.parseColor("#DBEAFE"))
        }
        tvQuestion.text = q.question
        tvProgress.text = "答对 $correctCount/$needCount"
        optionsContainer.removeAllViews()

        if (q.type == "judge") {
            addOptionBtn("✓ 正确", true, q, dp)
            addOptionBtn("✗ 错误", false, q, dp)
        } else {
            val labels = listOf("A", "B", "C", "D")
            q.options.forEachIndexed { i, opt ->
                addOptionBtn("${labels[i]}. $opt", i, q, dp)
            }
        }

        // 启动倒计时
        startTimer()
    }

    private fun startTimer() {
        stopTimer()
        timeLeft = timePerQ
        tvTimer.text = "${timeLeft}s"
        tvTimer.setTextColor(Color.parseColor("#6B7280"))
        val ticker = object : Runnable {
            override fun run() {
                timeLeft--
                tvTimer.text = "${timeLeft}s"
                if (timeLeft <= 5) tvTimer.setTextColor(Color.parseColor("#EF4444"))
                if (timeLeft <= 0) {
                    timeUp()
                } else {
                    handler.postDelayed(this, 1000L)
                }
            }
        }
        handler.postDelayed(ticker, 1000L)
    }

    private fun stopTimer() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun timeUp() {
        // 超时自动判错
        val opts = optionsContainer
        for (i in 0 until opts.childCount) opts.getChildAt(i).isClickable = false
        Toast.makeText(ctx, "时间到！", Toast.LENGTH_SHORT).show()
        rootView?.postDelayed({ loadNextQuestion() }, 600)
    }

    private fun addOptionBtn(text: String, value: Any, q: Question, dp: (Int) -> Int) {
        val btn = Button(ctx).apply {
            this.text = text
            setAllCaps(false)
            textSize = 15f
            setTextColor(Color.parseColor("#1F2937"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(13), dp(16), dp(13))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(10) }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F3F4F6"))
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.parseColor("#E5E7EB"))
            }
        }
        btn.setOnClickListener {
            stopTimer()
            for (i in 0 until optionsContainer.childCount) optionsContainer.getChildAt(i).isClickable = false
            val correct = when (q.type) {
                "judge" -> (value as Boolean) == (q.answer == 1)
                else -> (value as Int) == q.answer
            }
            if (correct) {
                correctCount++
                btn.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#D1FAE5"))
                    cornerRadius = dp(10).toFloat()
                    setStroke(dp(1), Color.parseColor("#10B981"))
                }
                btn.setTextColor(Color.parseColor("#059669"))
            } else {
                btn.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#FEE2E2"))
                    cornerRadius = dp(10).toFloat()
                    setStroke(dp(1), Color.parseColor("#EF4444"))
                }
                btn.setTextColor(Color.parseColor("#DC2626"))
                if (q.type == "single") {
                    optionsContainer.getChildAt(q.answer).background = GradientDrawable().apply {
                        setColor(Color.parseColor("#D1FAE5"))
                        cornerRadius = dp(10).toFloat()
                        setStroke(dp(1), Color.parseColor("#10B981"))
                    }
                }
            }
            rootView?.postDelayed({
                if (correctCount >= needCount) {
                    hide()
                    onComplete?.invoke()
                } else {
                    loadNextQuestion()
                }
            }, 700)
        }
        optionsContainer.addView(btn)
    }

    private fun showPinDialog() {
        val density = ctx.resources.displayMetrics.density
        val dp = { v: Int -> (v * density).toInt() }
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }
        layout.addView(TextView(ctx).apply {
            text = "输入密码退出"
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
        })
        val input = EditText(ctx).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "4位数字密码"
            setPadding(0, dp(16), 0, dp(16))
        }
        layout.addView(input)
        val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(Button(ctx).apply {
            text = "取消"; setAllCaps(false)
            setBackgroundColor(Color.TRANSPARENT); setTextColor(Color.parseColor("#6B7280"))
            setOnClickListener { try { wm.removeView(layout) } catch (_: Exception) {} }
        })
        row.addView(Button(ctx).apply {
            text = "确定"; setAllCaps(false); setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.parseColor("#4F46E5")); cornerRadius = dp(8).toFloat() }
            setOnClickListener {
                if (input.text.toString() == Prefs.getPin(ctx)) {
                    try { wm.removeView(layout) } catch (_: Exception) {}
                    hide(); onCancel?.invoke()
                } else Toast.makeText(ctx, "密码错误", Toast.LENGTH_SHORT).show()
            }
        })
        layout.addView(row)
        val params = WindowManager.LayoutParams(
            (ctx.resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }
        try { wm.addView(layout, params) } catch (_: Exception) {}
    }

    fun hide() {
        stopTimer()
        rootView?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        rootView = null
        isShowing = false
    }

    private class QuizFrameLayout(context: Context) : LinearLayout(context) {
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (event.keyCode == KeyEvent.KEYCODE_BACK) return true
            return super.dispatchKeyEvent(event)
        }
    }

    companion object {
        @Volatile
        private var instance: QuizOverlayController? = null
        fun get(context: Context): QuizOverlayController {
            return instance ?: synchronized(this) {
                instance ?: QuizOverlayController(context.applicationContext).also { instance = it }
            }
        }
    }
}
