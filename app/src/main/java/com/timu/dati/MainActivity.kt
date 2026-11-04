package com.timu.dati

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.timu.dati.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var appList = mutableListOf<AppInfo>()
    private val FILE_REQ = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        refreshPermissionStatus()
    }

    private fun setupUI() {
        binding.btnOverlayPerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        binding.btnA11yPerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.btnSetPin.setOnClickListener { showPinDialog() }
        binding.btnSetCount.setOnClickListener {
            val et = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText(Prefs.getDefaultQNeeded(this@MainActivity).toString())
            }
            AlertDialog.Builder(this).setTitle("默认答对几道题？").setView(et)
                .setPositiveButton("确定") { _, _ ->
                    Prefs.setDefaultQNeeded(this, (et.text.toString().toIntOrNull() ?: 3).coerceAtLeast(1))
                    refreshSettings()
                }.show()
        }
        binding.btnSetTime.setOnClickListener {
            val et = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText(Prefs.getTimePerQ(this@MainActivity).toString())
            }
            AlertDialog.Builder(this).setTitle("每题限时（秒）").setView(et)
                .setPositiveButton("确定") { _, _ ->
                    Prefs.setTimePerQ(this, (et.text.toString().toIntOrNull() ?: 30).coerceAtLeast(5))
                    refreshSettings()
                }.show()
        }
        binding.btnUploadQ.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/json" }
            startActivityForResult(intent, FILE_REQ)
        }
        binding.btnDownloadTpl.setOnClickListener {
            val tpl = """{
  "subject": "数学",
  "questions": [
    {
      "type": "single",
      "question": "1+1=?",
      "options": ["1","2","3","4"],
      "answer": 1,
      "explain": "1+1=2"
    },
    {
      "type": "judge",
      "question": "地球是圆的",
      "answer": 1,
      "explain": "地球是椭球体"
    }
  ]
}"""
            AlertDialog.Builder(this).setTitle("题库模板格式")
                .setMessage("按以下格式编辑JSON文件，然后点上传题库导入：\n\n$tpl\n\n题型：single=单选（answer填选项序号从0开始），judge=判断（answer填1=对，0=错）")
                .setPositiveButton("知道了", null).show()
        }
        binding.btnClearQ.setOnClickListener {
            AlertDialog.Builder(this).setTitle("清空用户题库？")
                .setPositiveButton("清空") { _, _ ->
                    Prefs.clearUserQuestions(this)
                    refreshQCount()
                }.setNegativeButton("取消", null).show()
        }
        binding.btnAddApp.setOnClickListener { loadAppList() }
        binding.btnAllMonitored.setOnClickListener {
            AlertDialog.Builder(this).setTitle("全部管控")
                .setMessage("将所有已安装APP都加入管控？打开任何APP都会弹题。")
                .setPositiveButton("全部管控") { _, _ ->
                    val pm = packageManager
                    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                    val allPkgs = pm.queryIntentActivities(intent, 0).map { it.activityInfo.packageName }.toSet()
                    val set = Prefs.getMonitoredApps(this)
                    set.addAll(allPkgs)
                    Prefs.setMonitoredApps(this, set)
                    loadMonitoredApps()
                    Toast.makeText(this, "已管控全部 ${allPkgs.size} 个APP", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("取消", null).show()
        }
        binding.btnTestQuiz.setOnClickListener {
            // 直接弹出答题界面，测试遮罩能不能显示
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请先开启悬浮窗权限", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val overlay = QuizOverlayController.get(this)
            overlay.onComplete = { Toast.makeText(this, "测试通过！答题遮罩正常", Toast.LENGTH_LONG).show() }
            overlay.onCancel = { Toast.makeText(this, "密码退出成功", Toast.LENGTH_SHORT).show() }
            overlay.show("com.test.pkg")
        }
        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
        refreshSettings()
        loadMonitoredApps()
        refreshQCount()
    }

    private fun refreshPermissionStatus() {
        binding.tvOverlayStatus.text = if (Settings.canDrawOverlays(this)) "✅ 已开启" else "❌ 未开启"
        val a11y = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        binding.tvA11yStatus.text = if (a11y?.contains(packageName) == true) "✅ 已开启" else "❌ 未开启"
    }

    private fun refreshSettings() {
        binding.tvPin.text = "密码：" + Prefs.getPin(this)
        binding.tvCount.text = "默认答对：${Prefs.getDefaultQNeeded(this)} 道"
        binding.tvTime.text = "每题限时：${Prefs.getTimePerQ(this)} 秒"
    }

    private fun refreshQCount() {
        binding.tvQCount.text = "用户题库：${Prefs.getUserQuestions(this).size} 题"
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
            showAppConfigDialog(app)
        }
        binding.tvMonitoredCount.text = "已管控 ${appList.size} 个应用（点击可配置）"
    }

    private fun showAppConfigDialog(app: AppInfo) {
        val items = arrayOf("设置答题数量", "指定题库学科", "延迟弹题时间", "移除管控")
        AlertDialog.Builder(this).setTitle(app.name).setItems(items) { _, which ->
            when (which) {
                0 -> {
                    val et = EditText(this).apply {
                        inputType = InputType.TYPE_CLASS_NUMBER
                        setText(Prefs.getAppQCount(this@MainActivity, app.pkg).toString())
                    }
                    AlertDialog.Builder(this).setTitle("答对几道题放行？").setView(et)
                        .setPositiveButton("确定") { _, _ ->
                            Prefs.setAppQCount(this, app.pkg, (et.text.toString().toIntOrNull() ?: 3).coerceAtLeast(1))
                        }.show()
                }
                1 -> {
                    val subs = listOf("全部随机") + QuizBank.subjects.map { it.icon + " " + it.name }
                    val ids = listOf("all") + QuizBank.subjects.map { it.id }
                    AlertDialog.Builder(this).setTitle("指定题库学科")
                        .setItems(subs.toTypedArray()) { _, i ->
                            Prefs.setAppSubject(this, app.pkg, ids[i])
                        }.show()
                }
                2 -> {
                    val et = EditText(this).apply {
                        inputType = InputType.TYPE_CLASS_NUMBER
                        setText(Prefs.getAppDelaySec(this@MainActivity, app.pkg).toString())
                        hint = "打开后几秒弹题，0=立即"
                    }
                    AlertDialog.Builder(this).setTitle("延迟弹题（秒，0=立即）").setView(et)
                        .setPositiveButton("确定") { _, _ ->
                            Prefs.setAppDelaySec(this, app.pkg, (et.text.toString().toIntOrNull() ?: 0).coerceAtLeast(0))
                        }.show()
                }
                3 -> {
                    AlertDialog.Builder(this).setTitle("移除 ${app.name}？")
                        .setPositiveButton("移除") { _, _ ->
                            val set = Prefs.getMonitoredApps(this)
                            set.remove(app.pkg)
                            Prefs.setMonitoredApps(this, set)
                            loadMonitoredApps()
                        }.setNegativeButton("取消", null).show()
                }
            }
        }.show()
    }

    private fun loadAppList() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
            .map {
                val pkg = it.activityInfo.packageName
                AppInfo(pkg, it.loadLabel(pm).toString(), it.loadIcon(pm),
                    Prefs.getMonitoredApps(this).contains(pkg))
            }.distinctBy { it.pkg }.sortedBy { it.name }
        AlertDialog.Builder(this).setTitle("选择管控应用")
            .setMultiChoiceItems(apps.map { it.name }.toTypedArray(),
                BooleanArray(apps.size) { apps[it].monitored }) { _, which, isChecked ->
                apps[which].monitored = isChecked
            }.setPositiveButton("确定") { _, _ ->
                val set = Prefs.getMonitoredApps(this)
                apps.forEach { if (it.monitored) set.add(it.pkg) else set.remove(it.pkg) }
                Prefs.setMonitoredApps(this, set)
                loadMonitoredApps()
            }.show()
    }

    private fun showPinDialog() {
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "输入新4位数字密码"
        }
        AlertDialog.Builder(this).setTitle("修改密码").setView(et)
            .setPositiveButton("确定") { _, _ ->
                if (et.text.length == 4) { Prefs.setPin(this, et.text.toString()); refreshSettings() }
                else Toast.makeText(this, "密码必须4位", Toast.LENGTH_SHORT).show()
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_REQ && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val input = contentResolver.openInputStream(uri)
                    val text = input?.bufferedReader().use { it?.readText() }
                    val json = JSONObject(text ?: "{}")
                    val arr = json.getJSONArray("questions")
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
                    Prefs.addUserQuestions(this, list)
                    refreshQCount()
                    Toast.makeText(this, "成功导入 ${list.size} 道题", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "导入失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
