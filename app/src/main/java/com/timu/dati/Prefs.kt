package com.timu.dati

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * 配置存储
 */
object Prefs {
    private const val NAME = "timu_dati_prefs"
    private fun sp(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    // ===== 密码 =====
    fun getPin(ctx: Context): String = sp(ctx).getString("pin", "1234") ?: "1234"
    fun setPin(ctx: Context, pin: String) = sp(ctx).edit().putString("pin", pin).apply()

    // ===== 全局设置 =====
    fun getTimePerQ(ctx: Context): Int = sp(ctx).getInt("time_per_q", 30)
    fun setTimePerQ(ctx: Context, n: Int) = sp(ctx).edit().putInt("time_per_q", n).apply()

    fun getDefaultQNeeded(ctx: Context): Int = sp(ctx).getInt("default_q_needed", 3)
    fun setDefaultQNeeded(ctx: Context, n: Int) = sp(ctx).edit().putInt("default_q_needed", n).apply()

    // 难度：1=简单 2=一般 3=难 4=奥林匹克
    fun getDifficulty(ctx: Context): Int = sp(ctx).getInt("difficulty", 2)
    fun setDifficulty(ctx: Context, n: Int) = sp(ctx).edit().putInt("difficulty", n).apply()

    // 弹出模式：0=打开就弹，1=使用N分钟后弹
    fun getPopupMode(ctx: Context): Int = sp(ctx).getInt("popup_mode", 0)
    fun setPopupMode(ctx: Context, n: Int) = sp(ctx).edit().putInt("popup_mode", n).apply()

    // 定时弹：每N分钟弹一次
    fun getPopupIntervalMin(ctx: Context): Int = sp(ctx).getInt("popup_interval_min", 10)
    fun setPopupIntervalMin(ctx: Context, n: Int) = sp(ctx).edit().putInt("popup_interval_min", n).apply()

    // 答对题后的冷却时间（分钟），冷却期内不再弹
    fun getCooldownMin(ctx: Context): Int = sp(ctx).getInt("cooldown_min", 30)
    fun setCooldownMin(ctx: Context, n: Int) = sp(ctx).edit().putInt("cooldown_min", n).apply()

    // 记录上次答对题放行的时间戳
    fun getLastPassTime(ctx: Context): Long = sp(ctx).getLong("last_pass_time", 0L)
    fun setLastPassTime(ctx: Context, t: Long) = sp(ctx).edit().putLong("last_pass_time", t).apply()

    // ===== 管控APP列表 =====
    fun getMonitoredApps(ctx: Context): MutableSet<String> =
        sp(ctx).getStringSet("monitored", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

    fun setMonitoredApps(ctx: Context, apps: Set<String>) =
        sp(ctx).edit().putStringSet("monitored", apps).apply()

    // ===== 每个APP的单独配置 =====
    fun getAppQCount(ctx: Context, pkg: String): Int =
        sp(ctx).getInt("qcount_$pkg", getDefaultQNeeded(ctx))

    fun setAppQCount(ctx: Context, pkg: String, n: Int) =
        sp(ctx).edit().putInt("qcount_$pkg", n).apply()

    fun getAppDelaySec(ctx: Context, pkg: String): Int =
        sp(ctx).getInt("delay_$pkg", 0)

    fun setAppDelaySec(ctx: Context, pkg: String, sec: Int) =
        sp(ctx).edit().putInt("delay_$pkg", sec).apply()

    fun getAppSubject(ctx: Context, pkg: String): String =
        sp(ctx).getString("subject_$pkg", "all") ?: "all"

    fun setAppSubject(ctx: Context, pkg: String, subj: String) =
        sp(ctx).edit().putString("subject_$pkg", subj).apply()

    // ===== 用户上传的题库 =====
    fun getUserQuestions(ctx: Context): MutableList<JSONObject> {
        val str = sp(ctx).getString("user_questions", "[]") ?: "[]"
        val arr = JSONArray(str)
        val list = mutableListOf<JSONObject>()
        for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
        return list
    }

    fun addUserQuestions(ctx: Context, questions: List<JSONObject>) {
        val list = getUserQuestions(ctx)
        list.addAll(questions)
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        sp(ctx).edit().putString("user_questions", arr.toString()).apply()
    }

    fun clearUserQuestions(ctx: Context) =
        sp(ctx).edit().putString("user_questions", "[]").apply()
}
