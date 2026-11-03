package com.timu.dati

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * 答题悬浮窗控制器：全屏覆盖，答对N道题才关闭。
 *
 * 功能：
 * - 全屏不透明遮罩，吞掉返回键
 * - 随机出题（单选/判断）
 * - 答对累计进度，答错继续下一题
 * - 答对设定数量后自动放行
 * - 提供"密码退出"选项
 */
class QuizOverlayController private constructor(private val ctx: Context) {

    private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var rootView: View? = null

    @Volatile
    var isShowing = false
        private set

    var onComplete: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    private var correctCount = 0
    private lateinit var needCount: 0
    private lateinit var activeSubjects: Set<String>

    // UI引用
    private lateinit var tvProgress: TextView
    private lateinit var tvType: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var optionsContainer: LinearLayout

    fun show() {
        if (isShowing) return

        needCount = Prefs.getQuestionsNeeded(ctx)
        activeSubjects = Prefs.getActiveSubjects(ctx)
        correctCount = 0

        val density = ctx.resources.displayMetrics.density
        val dp = { v: Int -> (v * density).toInt() }

        val container = QuizFrameLayout(ctx)

        // 根布局
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F3F4F6"))
            setPadding(dp(20), dp(40), dp(20), dp(20))
        }

        // 顶部栏：进度 + 密码退出
        val topBar = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(16))
        }

        tvProgress = TextView(ctx).apply {
            text = "已答对 0 / $needCount"
            setTextColor(Color.parseColor("#4F46E5"))
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnExit = TextView(ctx).apply {
            text = "密码退出"
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 13f
            setPadding(dp(12), dp(6), dp(12), dp(6))
            setOnClickListener { showPinDialog() }
        }

        topBar.addView(tvProgress)
        topBar.addView(btnExit)
        root.addView(topBar)

        // 标题卡片
        val headerCard = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(24), dp(20), dp(24))
            gravity = Gravity.CENTER
            elevation = dp(2).toFloat()
        }
        val title = TextView(ctx).apply {
            text = "📚 答题解锁"
            setTextColor(Color.parseColor("#1F2937"))
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
        }
        val subtitle = TextView(ctx).apply {
            text = "答对 $needCount 道题即可继续使用"
            setTextColor(Color.parseColor("#6B7280"))
            textSize = 13f
            setPadding(0, dp(6), 0, 0)
        }
        headerCard.addView(title)
        headerCard.addView(subtitle)
        root.addView(headerCard)

        // 题目卡片
        val qCard = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) }
            elevation = dp(2).toFloat()
        }

        tvType = TextView(ctx).apply {
            setPadding(dp(10), dp(4), dp(10), dp(4))
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = dp(0).let {
                GradientDrawable().apply {
                    setColor(Color.parseColor("#DBEAFE"))
                    cornerRadius = dp(20).toFloat()
                }
            }
            setTextColor(Color.parseColor("#2563EB"))
        }
        qCard.addView(tvType)

        tvQuestion = TextView(ctx).apply {
            setTextColor(Color.parseColor("#1F2937"))
            textSize = 18f
            setPadding(0, dp(16), 0, dp(16))
            setLineSpacing(dp(4).toFloat(), 1.2f)
        }
        qCard.addView(tvQuestion)

        optionsContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
        }
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
        val q = QuizBank.randomQuestion(activeSubjects)
        val density = ctx.resources.displayMetrics.density
        val dp = { v: Int -> (v * density).toInt() }

        // 题型标签
        if (q.type == "judge") {
            tvType.text = "判断题"
            tvType.setTextColor(Color.parseColor("#D97706"))
            (tvType.background as GradientDrawable).setColor(Color.parseColor("#FEF3C7"))
        } else {
            tvType.text = "单选题 · " + QuizBank.getSubjectName(q.subject)
            tvType.setTextColor(Color.parseColor("#2563EB"))
            (tvType.background as GradientDrawable).setColor(Color.parseColor("#DBEAFE"))
        }

        tvQuestion.text = q.question
        tvProgress.text = "已答对 $correctCount / $needCount"

        // 构建选项
        optionsContainer.removeAllViews()

        if (q.type == "judge") {
            // 判断题：对/错两个大按钮
            addOptionButton("✓ 正确", true, q, dp)
            addOptionButton("✗ 错误", false, q, dp)
        } else {
            val labels = listOf("A", "B", "C", "D")
            q.options.forEachIndexed { i, opt ->
                addOptionButton("${labels[i]}. $opt", i, q, dp)
            }
        }
    }

    private fun addOptionButton(text: String, value: Any, q: Question, dp: (Int) -> Int) {
        val btn = Button(ctx).apply {
            this.text = text
            setAllCaps(false)
            textSize = 15f
            setTextColor(Color.parseColor("#1F2937"))
            setBackgroundColor(Color.parseColor("#F9FAFB"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
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
            // 判断对错
            val isCorrect = when (q.type) {
                "judge" -> (value as Boolean) == (q.answer == 1)
                else -> (value as Int) == q.answer
            }

            if (isCorrect) {
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
                // 标出正确答案
                if (q.type == "single") {
                    optionsContainer.getChildAt(q.answer).background = GradientDrawable().apply {
                        setColor(Color.parseColor("#D1FAE5"))
                        cornerRadius = dp(10).toFloat()
                        setStroke(dp(1), Color.parseColor("#10B981"))
                    }
                }
            }

            // 禁用所有按钮
            for (i in 0 until optionsContainer.childCount) {
                optionsContainer.getChildAt(i).isClickable = false
            }

            // 延迟后下一题或完成
            rootView?.postDelayed({
                if (correctCount >= needCount) {
                    hide()
                    onComplete?.invoke()
                } else {
                    loadNextQuestion()
                }
            }, 800)
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

        val title = TextView(ctx).apply {
            text = "输入密码退出答题"
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1F2937"))
        }
        layout.addView(title)

        val input = EditText(ctx).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "4位数字密码"
            setPadding(0, dp(16), 0, dp(16))
        }
        layout.addView(input)

        val btnRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, 0)
        }
        val btnCancel = Button(ctx).apply {
            text = "取消"
            setAllCaps(false)
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#6B7280"))
        }
        val btnOk = Button(ctx).apply {
            text = "确定"
            setAllCaps(false)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#4F46E5"))
                cornerRadius = dp(8).toFloat()
            }
        }
        btnRow.addView(btnCancel)
        btnRow.addView(btnOk)
        layout.addView(btnRow)

        val params = WindowManager.LayoutParams(
            (ctx.resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        try {
            wm.addView(layout, params)
            btnCancel.setOnClickListener { wm.removeView(layout) }
            btnOk.setOnClickListener {
                if (input.text.toString() == Prefs.getPin(ctx)) {
                    wm.removeView(layout)
                    hide()
                    onCancel?.invoke()
                } else {
                    Toast.makeText(ctx, "密码错误", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (_: Exception) {}
    }

    fun hide() {
        rootView?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
        }
        rootView = null
        isShowing = false
    }

    /**
     * 吞掉返回键的FrameLayout
     */
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
