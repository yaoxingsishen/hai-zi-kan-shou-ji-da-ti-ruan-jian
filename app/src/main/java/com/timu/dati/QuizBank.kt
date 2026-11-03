package com.timu.dati

/**
 * 题目数据模型
 */
data class Question(
    val type: String,        // "single" or "judge"
    val subject: String,     // 学科id
    val question: String,
    val options: List<String> = emptyList(),  // 单选题才有
    val answer: Int,         // 单选题:选项索引;判断题:1=对,0=错
    val explain: String = ""
)

/**
 * 内置题库：数理化英地史政 + 中华上下五千年
 */
object QuizBank {

    val subjects = listOf(
        Subject("math", "数学", "🔢"),
        Subject("physics", "物理", "⚡"),
        Subject("chemistry", "化学", "🧪"),
        Subject("english", "英语", "🔤"),
        Subject("geography", "地理", "🌍"),
        Subject("history", "历史", "🏛️"),
        Subject("politics", "政治", "⚖️"),
        Subject("china5000", "中华五千年", "🐉"),
    )

    data class Subject(val id: String, val name: String, val icon: String)

    private val bank = mapOf(
        "math" to listOf(
            Question("single","math","12+28=?", listOf("38","40","42","44"), 1, "12+28=40"),
            Question("single","math","三角形内角和是多少度？", listOf("90°","180°","270°","360°"), 1, "三角形内角和恒为180°"),
            Question("single","math","下列哪个是质数？", listOf("9","15","17","21"), 2, "17只能被1和自身整除"),
            Question("judge","math","0是最小的自然数。", emptyList(), 1, "现行教材规定0是自然数"),
            Question("judge","math","所有偶数都是合数。", emptyList(), 0, "2是偶数但也是质数"),
            Question("single","math","圆的周长公式是？", listOf("πr","2πr","πr²","2πr²"), 1, "周长=2πr，面积=πr²"),
            Question("single","math","1小时等于多少秒？", listOf("360","3600","600","6000"), 1, "1小时=60分=3600秒"),
            Question("judge","math","直角三角形两锐角和等于90度。", emptyList(), 1, "180°-90°=90°"),
            Question("single","math","一个三角形三个内角度数比1:2:3，这个三角形是？", listOf("锐角三角形","直角三角形","钝角三角形","等边三角形"), 1, "30°、60°、90°，是直角三角形"),
            Question("judge","math","假分数的倒数都小于1。", emptyList(), 0, "分子等于分母的假分数倒数等于1"),
        ),
        "physics" to listOf(
            Question("single","physics","光在真空中的速度约为？", listOf("3×10⁶ m/s","3×10⁷ m/s","3×10⁸ m/s","3×10⁹ m/s"), 2, "光速约30万千米/秒"),
            Question("single","physics","水中筷子变弯属于什么现象？", listOf("光的反射","光的折射","光的直线传播","光的色散"), 1, "光从水斜射入空气时发生折射"),
            Question("single","physics","力的单位是？", listOf("焦耳","瓦特","牛顿","帕斯卡"), 2, "力的单位是牛顿(N)"),
            Question("judge","physics","物体不受力时一定静止。", emptyList(), 0, "牛顿第一定律：不受力时静止或匀速直线运动"),
            Question("judge","physics","声音不能在真空中传播。", emptyList(), 1, "声音传播需要介质"),
            Question("single","physics","标准大气压约为？", listOf("1013 Pa","10130 Pa","101300 Pa","1013000 Pa"), 2, "1标准大气压≈1.013×10⁵ Pa"),
            Question("judge","physics","并联电路各支路电压相等。", emptyList(), 1, "并联电路各支路两端电压相等"),
        ),
        "chemistry" to listOf(
            Question("single","chemistry","水的化学式是？", listOf("CO₂","H₂O","O₂","NaCl"), 1, "水由氢氧组成H₂O"),
            Question("single","chemistry","空气中含量最多的气体是？", listOf("氧气","二氧化碳","氮气","稀有气体"), 2, "氮气约占78%"),
            Question("single","chemistry","下列哪个是化学变化？", listOf("冰融化","铁生锈","玻璃破碎","水蒸发"), 1, "铁生锈产生新物质铁锈"),
            Question("judge","chemistry","氧气具有可燃性。", emptyList(), 0, "氧气有助燃性，但本身不可燃"),
            Question("judge","chemistry","pH<7的溶液呈酸性。", emptyList(), 1, "pH<7酸性，=7中性，>7碱性"),
            Question("single","chemistry","食盐的主要成分是？", listOf("NaOH","Na₂CO₃","NaCl","NaHCO₃"), 2, "食盐主要成分氯化钠NaCl"),
        ),
        "english" to listOf(
            Question("single","english","What ___ your name?", listOf("am","is","are","be"), 1, "your name是第三人称单数用is"),
            Question("single","english","\"苹果\"的英文是？", listOf("Banana","Apple","Orange","Grape"), 1, "apple=苹果"),
            Question("single","english","She ___ to school every day.", listOf("go","goes","going","went"), 1, "she第三人称单数一般现在时用goes"),
            Question("judge","english","\"Book\"的复数是\"books\"。", emptyList(), 1, "一般名词直接加s"),
            Question("judge","english","\"I am a student.\" 这句话是正确的。", emptyList(), 1, "I搭配am"),
            Question("single","english","下列哪个是元音字母？", listOf("B","C","E","F"), 2, "元音字母：a,e,i,o,u"),
        ),
        "geography" to listOf(
            Question("single","geography","中国的首都是？", listOf("上海","北京","广州","深圳"), 1, "中国首都是北京"),
            Question("single","geography","世界上最长的河流是？", listOf("长江","亚马逊河","尼罗河","密西西比河"), 2, "尼罗河是世界第一长河"),
            Question("single","geography","中国最大的岛屿是？", listOf("海南岛","台湾岛","崇明岛","舟山岛"), 1, "台湾岛是中国第一大岛"),
            Question("judge","geography","太平洋是世界上最大的洋。", emptyList(), 1, "太平洋面积约1.8亿平方千米"),
            Question("judge","geography","黄河是中国第一大河。", emptyList(), 0, "长江是第一大河，黄河第二"),
            Question("single","geography","五岳中的东岳是？", listOf("华山","泰山","衡山","嵩山"), 1, "东岳泰山，西岳华山"),
        ),
        "history" to listOf(
            Question("single","history","秦朝统一六国的皇帝是？", listOf("秦二世","秦始皇","汉高祖","周武王"), 1, "公元前221年秦始皇统一六国"),
            Question("single","history","唐朝开国皇帝是？", listOf("李世民","李渊","李隆基","李治"), 1, "618年李渊建立唐朝"),
            Question("single","history","鸦片战争爆发于哪一年？", listOf("1839","1840","1842","1856"), 1, "1840年鸦片战争爆发"),
            Question("judge","history","丝绸之路开通于西汉。", emptyList(), 1, "张骞出使西域开通丝绸之路"),
            Question("judge","history","辛亥革命推翻了清朝统治。", emptyList(), 1, "1911年辛亥革命结束封建帝制"),
            Question("single","history","中国第一个统一的封建王朝是？", listOf("夏朝","商朝","秦朝","周朝"), 2, "公元前221年秦朝建立"),
        ),
        "politics" to listOf(
            Question("single","politics","我国的根本制度是？", listOf("人民代表大会制度","社会主义制度","民族区域自治","多党合作"), 1, "社会主义制度是我国根本制度"),
            Question("judge","politics","宪法是我国的根本大法。", emptyList(), 1, "宪法具有最高法律效力"),
            Question("judge","politics","未成年人不需要遵守法律。", emptyList(), 0, "法律对全体社会成员具有普遍约束力"),
            Question("single","politics","我国一切权力属于？", listOf("公民","人民","人民代表","国家主席"), 1, "一切权力属于人民"),
        ),
        "china5000" to listOf(
            Question("single","china5000","被尊称为\"人文初祖\"的是？", listOf("黄帝","炎帝","尧","舜"), 0, "黄帝被尊为人文初祖"),
            Question("single","china5000","\"三过家门而不入\"说的是谁？", listOf("大禹","黄帝","周武王","孔子"), 0, "大禹治水三过家门而不入"),
            Question("single","china5000","中国历史上第一位皇帝是？", listOf("刘邦","嬴政","李世民","赵匡胤"), 1, "秦始皇嬴政自称始皇帝"),
            Question("judge","china5000","武则天是中国唯一的女皇帝。", emptyList(), 1, "武则天建立武周"),
            Question("judge","china5000","四大发明包括造纸术、印刷术、火药、指南针。", emptyList(), 1, "中国古代四大发明"),
            Question("single","china5000","郑和下西洋发生在哪个朝代？", listOf("宋朝","元朝","明朝","清朝"), 2, "明朝永乐年间郑和七下西洋"),
            Question("single","china5000","\"鞠躬尽瘁，死而后已\"是谁的名言？", listOf("刘备","诸葛亮","曹操","关羽"), 1, "出自诸葛亮《后出师表》"),
            Question("judge","china5000","科举制度创立于隋朝。", emptyList(), 1, "隋炀帝设进士科，科举正式形成"),
            Question("single","china5000","夏朝的建立者是？", listOf("启","禹","桀","汤"), 1, "约公元前2070年禹建立夏朝"),
            Question("single","china5000","\"人生自古谁无死，留取丹心照汗青\"是谁的诗句？", listOf("岳飞","文天祥","陆秀夫","辛弃疾"), 1, "出自文天祥《过零丁洋》"),
        ),
    )

    /**
     * 从指定学科中随机取一道题
     */
    fun randomQuestion(activeSubjects: Set<String>): Question {
        val pools = activeSubjects.mapNotNull { bank[it] }
        val all = pools.flatten()
        return all.random()
    }

    fun getSubjectName(id: String): String = subjects.find { it.id == id }?.name ?: id
}
