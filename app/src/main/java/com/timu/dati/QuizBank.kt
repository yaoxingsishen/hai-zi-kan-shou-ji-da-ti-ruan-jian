package com.timu.dati

import org.json.JSONArray
import org.json.JSONObject

data class Question(
    val type: String,      // single / judge
    val subject: String,   // math / chinese / english / ...
    val question: String,
    val options: List<String>,
    val answer: Int,       // 选项序号，judge时1=对 0=错
    val explain: String = ""
)

object QuizBank {

    val subjects = listOf(
        "math" to "数学",
        "chinese" to "语文",
        "english" to "英语",
        "physics" to "物理",
        "chemistry" to "化学",
        "biology" to "生物",
        "science" to "科学",
        "geography" to "地理",
        "history" to "历史",
        "poetry" to "诗词",
        "xiehouyu" to "歇后语",
        "etiquette" to "礼仪",
        "tangshi" to "唐诗",
        "songci" to "宋词",
        "social" to "社会实践",
        "survival" to "野外求生"
    )

    fun getSubjectName(subject: String): String {
        return subjects.find { it.first == subject }?.second ?: "自定义"
    }

    private val bank = mapOf(
        // ========== 数学 ==========
        "math" to listOf(
            Question("single","math","1 + 1 = ?", listOf("1","2","3","4"), 1, "1+1=2"),
            Question("single","math","2 × 3 = ?", listOf("5","6","7","8"), 1, "二三得六"),
            Question("single","math","10 - 4 = ?", listOf("5","6","7","8"), 1, "10-4=6"),
            Question("judge","math","三角形内角和是180度。", emptyList(), 1, "三角形内角和恒为180°"),
            Question("single","math","正方形有几条边？", listOf("3","4","5","6"), 1, "正方形4条边"),
        ),

        // ========== 语文 ==========
        "chinese" to listOf(
            Question("single","chinese","「床前明月光」的下一句是？", listOf("疑是地上霜","举头望明月","低头思故乡","白日依山尽"), 0, "李白《静夜思》"),
            Question("single","chinese","「春眠不觉晓」的下一句是？", listOf("处处闻啼鸟","夜来风雨声","花落知多少","忙趁东风放纸鸢"), 0, "孟浩然《春晓》"),
            Question("judge","chinese","「大」字有3画。", emptyList(), 1, "横、撇、捺，共3画"),
            Question("single","chinese","「白日依山尽」的下一句是？", listOf("黄河入海流","欲穷千里目","更上一层楼","疑是地上霜"), 0, "王之涣《登鹳雀楼》"),
            Question("single","chinese","「谁知盘中餐」的下一句是？", listOf("粒粒皆辛苦","汗滴禾下土","锄禾日当午","春种一粒粟"), 0, "李绅《悯农》"),
        ),

        // ========== 英语 ==========
        "english" to listOf(
            Question("single","english","苹果的英文是？", listOf("apple","banana","orange","pear"), 0, "apple苹果"),
            Question("single","english","「早上好」的英文是？", listOf("Good morning","Good night","Goodbye","Thank you"), 0, "Good morning早上好"),
            Question("judge","english","「cat」是猫的意思。", emptyList(), 1, "cat猫"),
            Question("single","english","谢谢你的英文是？", listOf("Thank you","Hello","Sorry","Bye"), 0, "Thank you谢谢你"),
            Question("single","english","「书」的英文是？", listOf("book","pen","pencil","bag"), 0, "book书"),
        ),

        // ========== 物理 ==========
        "physics" to listOf(
            Question("single","physics","水在多少度结冰？", listOf("0度","10度","50度","100度"), 0, "标准大气压下水0℃结冰"),
            Question("judge","physics","光在真空中的速度约30万公里/秒。", emptyList(), 1, "光速≈3×10⁸m/s"),
            Question("single","physics","苹果为什么往下掉？", listOf("地球引力","风吹的","苹果自己想掉","不知道"), 0, "万有引力"),
            Question("single","physics","彩虹有几种颜色？", listOf("5种","6种","7种","8种"), 2, "红橙黄绿青蓝紫7色"),
            Question("judge","physics","声音可以在真空中传播。", emptyList(), 0, "真空不能传声"),
        ),

        // ========== 化学 ==========
        "chemistry" to listOf(
            Question("single","chemistry","水的化学式是？", listOf("CO2","H2O","O2","NaCl"), 1, "H₂O水"),
            Question("judge","chemistry","铁放在潮湿的空气中会生锈。", emptyList(), 1, "铁+氧气+水→铁锈"),
            Question("single","chemistry","空气中含量最多的气体是？", listOf("氧气","氮气","二氧化碳","氢气"), 1, "氮气占78%"),
            Question("single","chemistry","火需要什么才能燃烧？", listOf("氧气","二氧化碳","氮气","水"), 0, "燃烧三要素：可燃物+氧气+温度"),
            Question("judge","chemistry","二氧化碳可以灭火。", emptyList(), 1, "CO₂不助燃，可灭火"),
        ),

        // ========== 生物 ==========
        "biology" to listOf(
            Question("single","biology","植物进行光合作用需要什么？", listOf("阳光","月光","灯光","以上都不对"), 0, "光合作用需要阳光、二氧化碳、水"),
            Question("judge","biology","人有206块骨头。", emptyList(), 1, "成人骨骼共206块"),
            Question("single","biology","鸡蛋里蛋清的作用是？", listOf("保护胚胎","提供营养","以上都对","以上都不对"), 2, "保护+提供营养"),
            Question("single","biology","植物的叶子为什么是绿色的？", listOf("含叶绿素","染的","光线原因","不知道"), 0, "叶绿素吸收红光蓝光，反射绿光"),
            Question("judge","biology","鱼用鳃呼吸。", emptyList(), 1, "鱼鳃吸收水中的氧气"),
        ),

        // ========== 科学 ==========
        "science" to listOf(
            Question("single","science","地球绕着什么转？", listOf("月亮","太阳","火星","金星"), 1, "地球绕太阳公转"),
            Question("judge","science","月亮本身会发光。", emptyList(), 0, "月亮反射太阳光"),
            Question("single","science","一年有几个月？", listOf("10个","11个","12个","13个"), 2, "一年12个月"),
            Question("single","science","一周有几天？", listOf("5天","6天","7天","8天"), 2, "一周7天"),
            Question("judge","science","地球是圆的。", emptyList(), 1, "地球是两极稍扁的球体"),
        ),

        // ========== 地理 ==========
        "geography" to listOf(
            Question("single","geography","中国的首都是？", listOf("上海","北京","广州","深圳"), 1, "北京是中国首都"),
            Question("judge","geography","长江是中国最长的河流。", emptyList(), 1, "长江约6300公里"),
            Question("single","geography","世界上最大的海洋是？", listOf("大西洋","太平洋","印度洋","北冰洋"), 1, "太平洋最大"),
            Question("single","geography","中国的最北边在哪个省？", listOf("黑龙江省","吉林省","辽宁省","内蒙古"), 0, "漠河在黑龙江"),
            Question("judge","geography","泰山是五岳之首。", emptyList(), 1, "东岳泰山"),
        ),

        // ========== 历史 ==========
        "history" to listOf(
            Question("single","history","中国第一个朝代是？", listOf("夏朝","商朝","周朝","秦朝"), 0, "夏朝是第一个世袭制朝代"),
            Question("judge","history","秦始皇统一了中国。", emptyList(), 1, "秦始皇灭六国统一天下"),
            Question("single","history","造纸术是谁发明的？", listOf("蔡伦","毕昇","张衡","李时珍"), 0, "蔡伦改进造纸术"),
            Question("single","history","西游记的作者是？", listOf("罗贯中","施耐庵","吴承恩","曹雪芹"), 2, "吴承恩《西游记》"),
            Question("judge","history","唐朝的开国皇帝是李渊。", emptyList(), 1, "唐高祖李渊"),
        ),

        // ========== 诗词 ==========
        "poetry" to listOf(
            Question("single","poetry","「白日依山尽」的下一句是？", listOf("黄河入海流","欲穷千里目","更上一层楼","登鹳雀楼"), 0, "王之涣《登鹳雀楼》"),
            Question("single","poetry","「谁知盘中餐」的下一句是？", listOf("粒粒皆辛苦","汗滴禾下土","锄禾日当午","春种一粒粟"), 0, "李绅《悯农》"),
            Question("single","poetry","「野火烧不尽」的下一句是？", listOf("春风吹又生","远芳侵古道","晴翠接荒城","一岁一枯荣"), 0, "白居易《赋得古原草送别》"),
            Question("judge","poetry","「举头望明月，低头思故乡」表达思乡之情。", emptyList(), 1, "李白思乡名作"),
            Question("single","poetry","「春眠不觉晓」的下一句是？", listOf("处处闻啼鸟","夜来风雨声","花落知多少","夜来风雨声"), 0, "孟浩然《春晓》"),
            Question("single","poetry","「海内存知己」的下一句是？", listOf("天涯若比邻","儿女共沾巾","无为在歧路","海内存知己"), 0, "王勃《送杜少府之任蜀州》"),
            Question("judge","poetry","「少壮不努力，老大徒伤悲」劝人珍惜时间。", emptyList(), 1, "《长歌行》名句"),
            Question("single","poetry","「会当凌绝顶」的下一句是？", listOf("一览众山小","齐鲁青未了","造化钟神秀","阴阳割昏晓"), 0, "杜甫《望岳》"),
            Question("single","poetry","「但愿人长久」是谁写的？", listOf("苏轼","李白","杜甫","白居易"), 0, "苏轼《水调歌头》"),
            Question("judge","poetry","「桃花潭水深千尺，不及汪伦送我情」是送别诗。", emptyList(), 1, "李白送别汪伦"),
        ),

        // ========== 歇后语 ==========
        "xiehouyu" to listOf(
            Question("single","xiehouyu","「芝麻开花」的下一句是？", listOf("节节高","一场空","七上八下","节节高"), 0, "芝麻开花节节高，形容越来越好"),
            Question("single","xiehouyu","「十五个吊桶打水」的下一句是？", listOf("七上八下","节节高","一场空","七上八下"), 0, "心神不定的样子"),
            Question("single","xiehouyu","「竹篮打水」的下一句是？", listOf("一场空","节节高","七上八下","一场空"), 0, "比喻白费力气"),
            Question("judge","xiehouyu","「八仙过海」的下一句是「各显神通」。", emptyList(), 1, "各自拿出本领"),
            Question("single","xiehouyu","「老鼠过街」的下一句是？", listOf("人人喊打","各显神通","节节高","人人喊打"), 0, "比喻人人痛恨"),
            Question("judge","xiehouyu","「老虎屁股」摸不得。", emptyList(), 1, "比喻惹不起"),
            Question("single","xiehouyu","「画蛇添足」比喻？", listOf("多此一举","恰到好处","画得好","多此一举"), 0, "做多余的事反而不好"),
        ),

        // ========== 礼仪 ==========
        "etiquette" to listOf(
            Question("single","etiquette","见长辈应该怎么称呼？", listOf("直呼其名","叫「叔叔/阿姨/爷爷/奶奶」","哎","那个人"), 1, "长辈要有尊称"),
            Question("single","etiquette","吃饭时筷子应该怎么拿？", listOf("插在饭里","拿在手里夹菜","挥来挥去","指着别人"), 1, "插筷子像上香，不礼貌"),
            Question("judge","etiquette","别人说话的时候应该认真听，不插嘴。", emptyList(), 1, "倾听是基本礼貌"),
            Question("single","etiquette","收到别人帮助应该说什么？", listOf("谢谢","哦","知道了","没事"), 0, "礼貌说谢谢"),
            Question("single","etiquette","去别人家做客应该？", listOf("随便翻东西","提前打招呼、带小礼物","不请自来","乱走动"), 1, "做客要有礼"),
            Question("judge","etiquette","打喷嚏时应该对着别人打。", emptyList(), 0, "应该捂住嘴侧过头"),
            Question("single","etiquette","排队的时候应该？", listOf("插队","按顺序排","挤来挤去","推别人"), 1, "先来后到"),
            Question("single","etiquette","接电话第一句应该说？", listOf("喂你好","喂，谁啊","没事挂了","喂，快说"), 0, "礼貌用语"),
        ),

        // ========== 唐诗 ==========
        "tangshi" to listOf(
            Question("single","tangshi","李白被称为？", listOf("诗圣","诗仙","诗鬼","诗佛"), 1, "李白号青莲居士，诗仙"),
            Question("single","tangshi","杜甫被称为？", listOf("诗仙","诗圣","诗魔","诗豪"), 1, "杜甫诗圣，诗作诗史"),
            Question("single","tangshi","「床前明月光」是谁写的？", listOf("杜甫","李白","白居易","王维"), 1, "李白《静夜思》"),
            Question("judge","tangshi","唐诗是唐朝的诗歌。", emptyList(), 1, "唐诗是中国文学瑰宝"),
            Question("single","tangshi","「春眠不觉晓」的作者是？", listOf("孟浩然","李白","杜甫","王昌龄"), 0, "孟浩然《春晓》"),
            Question("single","tangshi","「白日依山尽」的作者是？", listOf("王之涣","李白","杜甫","王维"), 0, "王之涣《登鹳雀楼》"),
            Question("judge","tangshi","「谁知盘中餐，粒粒皆辛苦」作者是李绅。", emptyList(), 1, "李绅《悯农》"),
            Question("single","tangshi","「桃花潭水深千尺」下一句是？", listOf("不及汪伦送我情","唯见长江天际流","一片冰心在玉壶","不及汪伦送我情"), 0, "李白赠汪伦"),
        ),

        // ========== 宋词 ==========
        "songci" to listOf(
            Question("single","songci","「大江东去，浪淘尽」是谁写的？", listOf("苏轼","李清照","辛弃疾","柳永"), 0, "苏轼《念奴娇·赤壁怀古》"),
            Question("single","songci","「寻寻觅觅，冷冷清清」是谁写的？", listOf("李清照","苏轼","陆游","欧阳修"), 0, "李清照《声声慢》"),
            Question("judge","songci","宋词是宋朝流行的文学体裁。", emptyList(), 1, "宋词与唐诗并称双绝"),
            Question("single","songci","「但愿人长久，千里共婵娟」的词牌名是？", listOf("水调歌头","念奴娇","如梦令","水调歌头"), 0, "苏轼《水调歌头·明月几时有》"),
            Question("single","songci","辛弃疾是哪个朝代的？", listOf("唐朝","北宋","南宋","元朝"), 2, "南宋豪放派词人"),
            Question("judge","songci","「醉里挑灯看剑」是辛弃疾写的。", emptyList(), 1, "辛弃疾《破阵子》"),
            Question("single","songci","宋词兴盛于哪个朝代？", listOf("唐朝","宋朝","明朝","清朝"), 1, "宋朝是词的时代"),
            Question("single","songci","「问君能有几多愁」下一句是？", listOf("恰似一江春水向东流","春花秋月何时了","故国不堪回首","恰似一江春水向东流"), 0, "李煜《虞美人》"),
        ),

        // ========== 社会实践 ==========
        "social" to listOf(
            Question("single","social","过马路应该走什么？", listOf("斑马线","随便穿","翻栏杆","车少直接跑"), 0, "走斑马线人行道"),
            Question("single","social","红绿灯红灯亮时应该？", listOf("赶紧跑过去","停下等待","慢慢走","看心情"), 1, "红灯停绿灯行"),
            Question("judge","social","过马路可以看手机。", emptyList(), 0, "过马路要专心"),
            Question("single","social","发生火灾应该拨打什么电话？", listOf("110","119","120","114"), 1, "119火警"),
            Question("single","social","有人晕倒应该拨打什么电话？", listOf("119","120","110","122"), 1, "120急救"),
            Question("judge","social","垃圾分类中，废电池属于有害垃圾。", emptyList(), 1, "电池有毒有害"),
            Question("single","social","节约用水应该？", listOf("开着水龙头洗东西","随手关紧水龙头","多放水","浪费没关系"), 1, "节约用水人人有责"),
            Question("judge","social","小朋友独自在家有人敲门应该马上开门。", emptyList(), 0, "从猫眼看，不认识就不开"),
        ),

        // ========== 野外求生 ==========
        "survival" to listOf(
            Question("single","survival","野外迷路了应该？", listOf("乱走","留在原地等救援","随便选个方向跑","爬上树睡觉"), 1, "迷路不乱跑，原地等待最安全"),
            Question("single","survival","野外没有水应该喝什么？", listOf("河水直接喝","找水源净化后喝","尿","露水也不行"), 1, "生水有细菌，要烧开或净化"),
            Question("judge","survival","打雷下雨天可以在大树下躲雨。", emptyList(), 0, "大树易遭雷击，不能躲树下"),
            Question("single","survival","野外遇到蛇应该？", listOf("跑过去","绕开慢慢走","踩它","抓它"), 1, "不惊动蛇，慢慢绕开"),
            Question("single","survival","太阳在天上，北半球怎么分辨方向？", listOf("太阳升起方向是东","太阳方向是西","随便猜","看草往哪边倒"), 0, "太阳东升西落"),
            Question("judge","survival","野外生火后一定要彻底熄灭再离开。", emptyList(), 1, "防止森林火灾"),
            Question("single","survival","冬天野外失温应该？", listOf("剧烈运动产热","找避风处、干燥处保暖","喝冰水","脱衣服"), 1, "保暖防风防湿最重要"),
        ),
    )

    /**
     * 程序化生成数学题（无限题库，按难度）
     * difficulty: 1=简单 2=一般 3=难 4=奥林匹克
     */
    private fun genMathQuestion(difficulty: Int = 2): Question {
        val (min, max) = when (difficulty) {
            1 -> 1 to 20
            2 -> 10 to 99
            3 -> 100 to 999
            else -> 10 to 999
        }
        val type = if (difficulty >= 4) (0..5).random() else (0..3).random()
        return when (type) {
            0 -> {
                val a = (min..max).random()
                val b = (min..max).random()
                val ans = a + b
                val opts = mutableListOf(ans.toString())
                while (opts.size < 4) {
                    val w = ans + (-20..20).random()
                    if (w != ans && w > 0 && !opts.contains(w.toString())) opts.add(w.toString())
                }
                Question("single", "math", "$a + $b = ?", opts.shuffled(), opts.indexOf(ans.toString()), "$a+$b=$ans")
            }
            1 -> {
                val a = if (difficulty == 1) (2..9).random() else (2..20).random()
                val b = if (difficulty == 1) (2..9).random() else (2..15).random()
                val ans = a * b
                val opts = mutableListOf(ans.toString())
                while (opts.size < 4) {
                    val w = ans + (-15..15).random()
                    if (w != ans && w > 0 && !opts.contains(w.toString())) opts.add(w.toString())
                }
                Question("single", "math", "$a × $b = ?", opts.shuffled(), opts.indexOf(ans.toString()), "$a×$b=$ans")
            }
            2 -> {
                val b = (2..12).random()
                val ans = (2..if(difficulty>=3)50 else 20).random()
                val a = b * ans
                val opts = mutableListOf(ans.toString())
                while (opts.size < 4) {
                    val w = ans + (-5..5).random()
                    if (w != ans && w > 0 && !opts.contains(w.toString())) opts.add(w.toString())
                }
                Question("single", "math", "$a ÷ $b = ?", opts.shuffled(), opts.indexOf(ans.toString()), "$a÷$b=$ans")
            }
            3 -> {
                val a = (min..max*10).random()
                val b = (min..max*10).random()
                val correct = if (a > b) ">" else if (a < b) "<" else "="
                Question("single", "math", "$a ○ $b，应填？", listOf(">", "<", "=", "≠"), listOf(">", "<", "=", "≠").indexOf(correct), "$a${if(a>b)">" else if(a<b)"<" else "="}$b")
            }
            else -> {
                val n = if (difficulty >= 4) (10..200).random() else (10..100).random()
                var isPrime = n > 1
                for (i in 2 until n) { if (n % i == 0) { isPrime = false; break } }
                Question("judge", "math", "${n} 是质数。", emptyList(), if (isPrime) 1 else 0, if (isPrime) "${n}是质数" else "${n}不是质数")
            }
        }
    }

    /**
     * 随机取一道题（合并内置题库+用户题库，按指定学科筛选）
     */
    fun randomQuestion(ctx: android.content.Context, subject: String = "all"): Question {
        val diff = Prefs.getDifficulty(ctx)
        if (subject == "math" && (0..1).random() == 0) {
            return genMathQuestion(diff)
        }
        if (subject == "all" && (0..5).random() == 0) {
            return genMathQuestion(diff)
        }
        val pool = mutableListOf<Question>()
        if (subject == "all") {
            bank.values.forEach { pool.addAll(it) }
        } else {
            bank[subject]?.let { pool.addAll(it) }
        }
        Prefs.getUserQuestions(ctx).forEach { json ->
            try {
                val type = json.getString("type")
                val q = json.getString("question")
                val answer = json.getInt("answer")
                val explain = if (json.has("explain")) json.getString("explain") else ""
                val options = mutableListOf<String>()
                if (json.has("options")) {
                    val arr = json.getJSONArray("options")
                    for (i in 0 until arr.length()) options.add(arr.getString(i))
                }
                val s = if (json.has("subject")) json.getString("subject") else "custom"
                pool.add(Question(type, s, q, options, answer, explain))
            } catch (_: Exception) {}
        }
        if (pool.isEmpty()) {
            return Question("judge", "math", "1+1=2", emptyList(), 1, "默认题")
        }
        return pool.random()
    }
}
