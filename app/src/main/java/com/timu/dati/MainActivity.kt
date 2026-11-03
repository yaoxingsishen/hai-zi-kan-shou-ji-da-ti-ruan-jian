package com.timu.dati

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.timu.dati.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var appList = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        refreshPermissionStatus()
    }

    private fun setupUI() {
        // 悬浮窗权限
        binding.btnOverlayPerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        // 无障碍权限
        binding.btnA11yPerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // 设置密码
        binding.btnSetPin.setOnClickListener { showPinDialog() }

        // 设置答题数量
        binding.btnSetCount.setOnClickListener {
            val et = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText(Prefs.getQuestionsNeeded(this@MainActivity).toString())
            }
            AlertDialog.Builder(this)
                .setTitle("需要答对几道题？")
                .setView(et)
                .setPositiveButton("确定") { _, _ ->
                    val n = et.text.toString().toIntOrNull() ?: 3
                    Prefs.setQuestionsNeeded(this, n.coerceAtLeast(1))
                    refreshSettings()
                }
                .show()
        }

        // 添加管控APP
        binding.btnAddApp.setOnClickListener { loadAppList() }

        // 列表
        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
        refreshSettings()
        loadMonitoredApps()
    }

    private fun refreshPermissionStatus() {
        val overlay = Settings.canDrawOverlays(this)
        binding.tvOverlayStatus.text = if (overlay) "✅ 已开启" else "❌ 未开启"

        val a11y = isAccessibilityEnabled()
        binding.tvA11yStatus.text = if (a11y) "✅ 已开启" else "❌ 未开启"
    }

    private fun isAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabled?.contains(packageName) == true
    }

    private fun refreshSettings() {
        binding.tvPin.text = "当前密码：" + Prefs.getPin(this)
        binding.tvCount.text = "需答对：${Prefs.getQuestionsNeeded(this)} 道题"
    }

    private fun loadMonitoredApps() {
        val monitored = Prefs.getMonitoredApps(this)
        val pm = packageManager
        appList = monitored.mapNotNull { pkg ->
            try {
                val info = pm.getApplicationInfo(pkg, 0)
                AppInfo(pkg, pm.getApplicationLabel(info).toString(), pm.getApplicationIcon(pkg), true)
            } catch (_: Exception) { null }
        }.toMutableList()

        binding.recyclerApps.adapter = AppListAdapter(appList) { app ->
            // 点击移除
            AlertDialog.Builder(this)
                .setTitle("移除管控")
                .setMessage("确定移除 ${app.name} 的管控？")
                .setPositiveButton("移除") { _, _ ->
                    Prefs.removeMonitoredApp(this, app.pkg)
                    loadMonitoredApps()
                }
                .setNegativeButton("取消", null)
                .show()
        }
        binding.tvMonitoredCount.text = "已管控 ${appList.size} 个应用"
    }

    private fun loadAppList() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
            .map {
                val pkg = it.activityInfo.packageName
                AppInfo(pkg, it.loadLabel(pm).toString(), it.loadIcon(pm),
                    Prefs.getMonitoredApps(this).contains(pkg))
            }
            .distinctBy { it.pkg }
            .sortedBy { it.name }

        val names = apps.map { it.name }.toTypedArray()
        val checked = BooleanArray(apps.size) { apps[it].monitored }

        AlertDialog.Builder(this)
            .setTitle("选择要管控的应用")
            .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                apps[which].monitored = isChecked
            }
            .setPositiveButton("确定") { _, _ ->
                val set = Prefs.getMonitoredApps(this)
                apps.forEach {
                    if (it.monitored) set.add(it.pkg) else set.remove(it.pkg)
                }
                Prefs.setMonitoredApps(this, set)
                loadMonitoredApps()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showPinDialog() {
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "输入新4位数字密码"
        }
        AlertDialog.Builder(this)
            .setTitle("修改密码")
            .setView(et)
            .setPositiveButton("确定") { _, _ ->
                val pin = et.text.toString()
                if (pin.length == 4 && pin.all { it.isDigit() }) {
                    Prefs.setPin(this, pin)
                    refreshSettings()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
