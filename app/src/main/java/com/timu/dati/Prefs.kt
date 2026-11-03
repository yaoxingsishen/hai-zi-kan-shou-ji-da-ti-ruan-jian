package com.timu.dati

import android.content.Context
import android.content.SharedPreferences

/**
 * 配置存储：密码、管控APP列表、答题数量等
 */
object Prefs {
    private const val NAME = "timu_dati_prefs"
    private const val KEY_PIN = "pin"
    private const val KEY_MONITORED = "monitored_apps"
    private const val KEY_QUESTIONS_NEEDED = "questions_needed"
    private const val KEY_SUBJECTS = "active_subjects"

    private fun sp(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun getPin(ctx: Context): String = sp(ctx).getString(KEY_PIN, "1234") ?: "1234"
    fun setPin(ctx: Context, pin: String) = sp(ctx).edit().putString(KEY_PIN, pin).apply()

    fun getMonitoredApps(ctx: Context): MutableSet<String> =
        sp(ctx).getStringSet(KEY_MONITORED, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

    fun setMonitoredApps(ctx: Context, apps: Set<String>) =
        sp(ctx).edit().putStringSet(KEY_MONITORED, apps).apply()

    fun addMonitoredApp(ctx: Context, pkg: String) {
        val set = getMonitoredApps(ctx)
        set.add(pkg)
        setMonitoredApps(ctx, set)
    }

    fun removeMonitoredApp(ctx: Context, pkg: String) {
        val set = getMonitoredApps(ctx)
        set.remove(pkg)
        setMonitoredApps(ctx, set)
    }

    fun getQuestionsNeeded(ctx: Context): Int = sp(ctx).getInt(KEY_QUESTIONS_NEEDED, 3)
    fun setQuestionsNeeded(ctx: Context, n: Int) = sp(ctx).edit().putInt(KEY_QUESTIONS_NEEDED, n).apply()

    fun getActiveSubjects(ctx: Context): MutableSet<String> =
        sp(ctx).getStringSet(KEY_SUBJECTS, mutableSetOf("math","chinese5000"))?.toMutableSet() ?: mutableSetOf("math","chinese5000")
}
