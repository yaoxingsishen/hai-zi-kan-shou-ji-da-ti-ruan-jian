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
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val debugRunnable = object : Runnable {
        override fun run() {
            val fg = AppMonitorService.getCurrentForeground()
            binding.tvDebugFg.text = "当前前台：${fg ?: "未检测到"}"
            // 显示内部状态
            val service = AppMonitorService.instance
            val locked = service?.locked ?: false
            val cooldownMin = Prefs.getCooldownMin(this@MainActivity)
            val lastPass = Prefs.getLastPassTime(this@MainActivity)
            val elapsedSec = ((System.currentTimeMillis() - lastPass) / 1000).toInt()
            val remainSec = (cooldownMin * 60 - elapsedSec).coerceAtLeast(0)
            val monitoredCount = Prefs.getMonitoredApps(this@MainActivity).size
            binding.tvDebugStatus.text = "locked: $locked | 冷却剩余: ${remainSec}秒 | 管控APP数: $monitoredCount"
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showPasswordGate()
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
        refreshSettings()
        loadMonitoredApps()
        refreshQCount()
        handler.post(debugRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(debugRunnable)
    }

    private fun showPasswordGate() {
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "输入密码进入"
        }
        AlertDialog.Builder(this)
            .setTitle("🔒 定时答题锁")
            .setView(et)
            .setCancelable(false)
            .setPositiveButton("进入") { _, _ ->
                if (et.text.toString() == Prefs.getPin(this)) {
                    setupUI()
                    refreshPermissionStatus()
                } else {
                    Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show()
                    showPasswordGate()
                }
            }
            .setNegativeButton("退出") { _, _ -> finish() }
            .show()
    }

    private fun setupUI() {
        binding.btnOverlayPerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        binding.btnA11yPerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.btnUsagePerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        binding.btnDebugReset.setOnClickListener {
            // 重置所有状态
            Prefs.setLastPassTime(this, 0L)
            AppMonitorService.instance?.resetState()
            Toast.makeText(this, "已重置！现在打开抖音测试", Toast.LENGTH_LONG).show()
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
        binding.btnSetDifficulty.setOnClickListener {
            val items = arrayOf("🟢 简单", "🟡 一般", "🟠 难", "🔴 奥林匹克")
            AlertDialog.Builder(this).setTitle("选择题目难度")
                .setItems(items) { _, which ->
                    Prefs.setDifficulty(this, which + 1)
                    refreshSettings()
                }.show()
        }
        binding.btnSetPopupMode.setOnClickListener {
            val items = arrayOf("打开就弹答题", "使用N分钟后弹答题")
            AlertDialog.Builder(this).setTitle("弹出模式")
                .setItems(items) { _, which ->
                    Prefs.setPopupMode(this, which)
                    if (which == 1) {
                        val et = EditText(this).apply {
                            inputType = InputType.TYPE_CLASS_NUMBER
                            setText(Prefs.getPopupIntervalMin(this@MainActivity).toString())
                            hint = "每多少分钟弹一次？"
                        }
                        AlertDialog.Builder(this).setTitle("间隔（分钟）").setView(et)
                            .setPositiveButton("确定") { _, _ ->
                                Prefs.setPopupIntervalMin(this, (et.text.toString().toIntOrNull() ?: 10).coerceAtLeast(1))
                                refreshSettings()
                            }.show()
                    }
                    refreshSettings()
                }.show()
        }
        binding.btnSetCooldown.setOnClickListener {
            val et = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText(Prefs.getCooldownMin(this@MainActivity).toString())
                hint = "答对题后多少分钟内不再弹？"
            }
            AlertDialog.Builder(this).setTitle("冷却时间（分钟）").setView(et)
                .setPositiveButton("确定") { _, _ ->
                    Prefs.setCooldownMin(this, (et.text.toString().toIntOrNull() ?: 30).coerceAtLeast(1))
                    refreshSettings()
                }.show()
        }
        binding.btnUploadQ.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
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

    private fun refreshPermissionStatus() {
        binding.tvOverlayStatus.text = if (Settings.canDrawOverlays(this)) "✅ 已开启" else "❌ 未开启"
        val a11y = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        binding.tvA11yStatus.text = if (a11y?.contains(packageName) == true) "✅ 已开启" else "❌ 未开启"
        // 检查使用情况访问权限
        val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )
        binding.tvUsageStatus.text = if (mode == android.app.AppOpsManager.MODE_ALLOWED) "✅ 已开启" else "❌ 未开启"
    }

    private fun refreshSettings() {
        binding.tvPin.text = "密码：" + Prefs.getPin(this)
        binding.tvCount.text = "默认答对：${Prefs.getDefaultQNeeded(this)} 道"
        binding.tvTime.text = "每题限时：${Prefs.getTimePerQ(this)} 秒"
        val diffNames = listOf("简单", "一般", "难", "奥林匹克")
        binding.tvDifficulty.text = "题目难度：${diffNames[Prefs.getDifficulty(this) - 1]}"
        val mode = Prefs.getPopupMode(this)
        binding.tvPopupMode.text = if (mode == 0) "弹出模式：打开就弹" else "弹出模式：每${Prefs.getPopupIntervalMin(this)}分钟弹一次"
        binding.tvCooldown.text = "冷却时间：${Prefs.getCooldownMin(this)}分钟"
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
                    val subs = listOf("全部随机") + QuizBank.subjects.map { it.second }
                    val ids = listOf("all") + QuizBank.subjects.map { it.first }
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
        val allApps = pm.queryIntentActivities(intent, 0)
            .map {
                val pkg = it.activityInfo.packageName
                AppInfo(pkg, it.loadLabel(pm).toString(), it.loadIcon(pm),
                    Prefs.getMonitoredApps(this).contains(pkg))
            }.distinctBy { it.pkg }.sortedBy { it.name }

        // 搜索框 + 列表容器
        val layout = android.widget.LinearLayout(this).apply { orientation = android.widget.LinearLayout.VERTICAL }
        val search = EditText(this@MainActivity).apply {
            hint = "输入应用名搜索..."
            setSingleLine(true)
        }
        layout.addView(search)
        val listView = android.widget.ListView(this)
        layout.addView(listView)

        fun updateList(filter: String) {
            val filtered = if (filter.isBlank()) allApps else allApps.filter {
                it.name.contains(filter, ignoreCase = true) || it.pkg.contains(filter, ignoreCase = true)
            }
            val adapter = object : android.widget.ArrayAdapter<String>(
                this@MainActivity, android.R.layout.simple_list_item_multiple_choice,
                filtered.map { it.name }.toTypedArray()
            ) {}
            listView.adapter = adapter
            listView.choiceMode = android.widget.ListView.CHOICE_MODE_MULTIPLE
            filtered.forEachIndexed { i, app ->
                listView.setItemChecked(i, app.monitored)
            }
            listView.onItemClickListener = android.widget.AdapterView.OnItemClickListener { _, _, pos, _ ->
                filtered[pos].monitored = listView.isItemChecked(pos)
            }
        }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateList(s.toString())
            }
        })
        updateList("")

        AlertDialog.Builder(this).setTitle("选择管控应用（可搜索）")
            .setView(layout)
            .setPositiveButton("确定") { _, _ ->
                val set = Prefs.getMonitoredApps(this)
                allApps.forEach { if (it.monitored) set.add(it.pkg) else set.remove(it.pkg) }
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
                    val text = input?.bufferedReader().use { it?.readText() } ?: ""
                    val list = mutableListOf<JSONObject>()
                    if (text.trimStart().startsWith("{")) {
                        // JSON格式
                        val json = JSONObject(text)
                        val arr = json.getJSONArray("questions")
                        for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
                    } else {
                        // CSV格式：题型,题目,选项1,选项2,选项3,选项4,答案序号,解析
                        // Excel编辑后另存为CSV即可导入
                        text.lines().forEach { line ->
                            if (line.isBlank() || line.startsWith("#")) return@forEach
                            if (line.startsWith("type,") || line.startsWith("题型,")) return@forEach // 跳过表头
                            val parts = line.split(",")
                            if (parts.size < 5) return@forEach
                            val q = JSONObject()
                            // 中文类型转英文
                            val typeRaw = parts[0].trim()
                            val type = when (typeRaw) {
                                "单选", "single" -> "single"
                                "判断", "judge" -> "judge"
                                else -> typeRaw
                            }
                            q.put("type", type)
                            q.put("question", parts[1].trim())
                            val opts = JSONArray()
                            for (i in 2..5) if (parts.size > i && parts[i].isNotBlank()) opts.put(parts[i].trim())
                            q.put("options", opts)
                            q.put("answer", parts[6].trim().toIntOrNull() ?: 0)
                            if (parts.size > 7) q.put("explain", parts[7].trim())
                            list.add(q)
                        }
                    }
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
