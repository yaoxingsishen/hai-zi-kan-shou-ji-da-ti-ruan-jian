package com.timu.dati

/**
 * 题目数据模型
 */
data class Question(
    val type: String,
    val subject: String,
    val question: String,
    val options: List<String> = emptyList(),
    val answer: Int,
    val explain: String = ""
)

/**
 * 内置题库：16学科，幼儿园到大学难度
 */
object QuizBank {

    val subjects = listOf(
        Subject("math", "数学", "🔢"),
        Subject("chinese", "语文", "📖"),
        Subject("english", "英语", "🔤"),
        Subject("physics", "物理", "⚡"),
        Subject("chemistry", "化学", "🧪"),
        Subject("biology", "生物", "🧬"),
        Subject("science", "科学", "🔬"),
        Subject("geography", "地理", "🌍"),
        Subject("history", "历史", "🏛️"),
        Subject("poetry", "诗词", "📜"),
        Subject("xiehouyu", "歇后语", "🗣️"),
        Subject("etiquette", "礼仪", "🤝"),
        Subject("tangshi", "唐诗", "🌙"),
        Subject("songci", "宋词", "🌸"),
        Subject("social", "社会实践", "👥"),
        Subject("survival", "野外求生", "🏕️"),
    )

    data class Subject(val id: String, val name: String, val icon: String)

    private val bank = mapOf(
        // ========== 数学 ==========
        "math" to listOf(
            Question("single","math","12+28=?", listOf("38","40","42","44"), 1, "12+28=40"),
            Question("single","math","5×6=?", listOf("25","30","35","36"), 1, "五六三十"),
            Question("single","math","一个三角形内角和是多少度？", listOf("90°","180°","270°","360°"), 1, "三角形内角和180度"),
            Question("single","math","100以内最大的质数是？", listOf("97","98","99","96"), 0, "97是质数，98=2×49，99=9×11"),
            Question("single","math","圆的周长公式是？", listOf("πr²","2πr","πd²","2πd"), 1, "周长=2πr=πd"),
            Question("judge","math","0是最小的自然数。", emptyList(), 1, "0是自然数"),
            Question("judge","math","所有的偶数都是合数。", emptyList(), 0, "2是偶数但它是质数"),
            Question("single","math","如果x+5=12，x=?", listOf("5","6","7","8"), 2, "x=12-5=7"),
            Question("single","math","1米等于多少厘米？", listOf("10","100","1000","50"), 1, "1米=100厘米"),
            Question("judge","math","直角三角形两个锐角和是90度。", emptyList(), 1, "180-90=90"),
            Question("single","math","2的10次方是多少？", listOf("100","512","1024","2048"), 2, "2^10=1024"),
        ),

        // ========== 语文 ==========
        "chinese" to listOf(
            Question("single","chinese","「床前明月光」的下一句是？", listOf("疑是地上霜","举头望明月","低头思故乡","白首不见君"), 0, "李白《静夜思》"),
            Question("single","chinese","「春眠不觉晓」的作者是？", listOf("李白","杜甫","孟浩然","王维"), 2, "孟浩然《春晓》"),
            Question("single","chinese","「但愿人长久」的下一句是？", listOf("千里共婵娟","何事长向别时圆","明月几时有","把酒问青天"), 0, "苏轼《水调歌头》"),
            Question("judge","chinese","「的、地、得」三个字用法完全一样。", emptyList(), 0, "定语用「的」，状语用「地」，补语用「得」"),
            Question("single","chinese","「画龙点睛」这个成语故事里，被点睛的龙怎么了？", listOf("死了","飞走了","变大了","消失了"), 1, "张僧繇画龙点睛后龙破壁飞去"),
            Question("single","chinese","「负荆请罪」讲的是谁的故事？", listOf("廉颇和蔺相如","刘备和张飞","关羽和曹操","周瑜和诸葛亮"), 0, "廉颇背着荆条向蔺相如请罪"),
            Question("judge","chinese","「亡羊补牢」比喻出了问题再补救还不算晚。", emptyList(), 1, "丢了羊再修羊圈还来得及"),
            Question("single","chinese","「守株待兔」讽刺的是哪种人？", listOf("勤劳的人","侥幸不劳而获的人","聪明的人","勇敢的人"), 1, "指望靠运气而不努力"),
            Question("single","chinese","《西游记》的作者是？", listOf("罗贯中","施耐庵","吴承恩","曹雪芹"), 2, "吴承恩写《西游记》"),
            Question("judge","chinese","「三顾茅庐」说的是刘备三次拜访诸葛亮。", emptyList(), 1, "刘备三顾茅庐请诸葛亮出山"),
        ),

        // ========== 英语 ==========
        "english" to listOf(
            Question("single","english","「苹果」的英文是？", listOf("banana","apple","orange","grape"), 1, "apple苹果"),
            Question("single","english","「谢谢」的英文是？", listOf("sorry","thank you","hello","goodbye"), 1, "thank you谢谢"),
            Question("single","english","「书」的英文是？", listOf("book","pen","desk","chair"), 0, "book书"),
            Question("judge","english","「Good morning」是早上好的意思。", emptyList(), 1, "Good morning早上好"),
            Question("single","english","「I」的意思是？", listOf("你","我","他","它"), 1, "I=我"),
            Question("single","english","「cat」是什么动物？", listOf("狗","猫","鸟","鱼"), 1, "cat猫"),
            Question("judge","english","「blue」是红色的意思。", emptyList(), 0, "blue蓝色，red才是红色"),
            Question("single","english","「How are you?」的回答通常是？", listOf("I'm fine, thank you.","Goodbye.","Yes, I am.","No."), 0, "I'm fine, thank you.我很好谢谢"),
            Question("single","english","「dog」是什么动物？", listOf("猫","狗","猪","羊"), 1, "dog狗"),
            Question("judge","english","「red」是红色。", emptyList(), 1, "red红色"),
        ),

        // ========== 物理 ==========
        "physics" to listOf(
            Question("single","physics","水的标准沸点是多少度？", listOf("90℃","100℃","110℃","80℃"), 1, "标准大气压下水沸点100℃"),
            Question("single","physics","光在真空中的速度约为？", listOf("30万公里/秒","3万公里/秒","300公里/秒","340米/秒"), 0, "光速约3×10^8米/秒=30万公里/秒"),
            Question("judge","physics","声音不能在真空中传播。", emptyList(), 1, "声音需要介质传播，真空不传声"),
            Question("single","physics","苹果落地是因为什么力？", listOf("磁力","重力","拉力","浮力"), 1, "地球引力即重力"),
            Question("single","physics","电的单位是？", listOf("伏特","牛顿","焦耳","帕斯卡"), 0, "电压单位伏特V"),
            Question("judge","physics","地球引力会让所有物体下落速度一样（忽略空气阻力）。", emptyList(), 1, "伽利略比萨斜塔实验"),
            Question("single","physics","彩虹一般有几种颜色？", listOf("5种","6种","7种","8种"), 2, "红橙黄绿蓝靛紫7色"),
            Question("single","physics","冰化成水是什么现象？", listOf("凝固","熔化","汽化","升华"), 1, "固态变液态叫熔化"),
            Question("judge","physics","镜子能反光是因为光的反射。", emptyList(), 1, "平面镜成像原理是光的反射"),
            Question("single","physics","人体安全电压不高于多少？", listOf("12V","36V","220V","110V"), 1, "安全电压不高于36V"),
        ),

        // ========== 化学 ==========
        "chemistry" to listOf(
            Question("single","chemistry","水的化学式是？", listOf("CO2","H2O","O2","NaCl"), 1, "H2O水"),
            Question("single","chemistry","空气中含量最多的气体是？", listOf("氧气","氮气","二氧化碳","氢气"), 1, "氮气约占空气78%"),
            Question("single","chemistry","火需要什么才能燃烧？", listOf("氧气","二氧化碳","氮气","水"), 0, "燃烧三要素：可燃物、氧气、温度"),
            Question("judge","chemistry","铁生锈是化学变化。", emptyList(), 1, "铁和氧气水反应生成氧化铁，新物质生成"),
            Question("single","chemistry","食盐的主要成分是？", listOf("氢氧化钠","氯化钠","碳酸钠","碳酸钙"), 1, "NaCl氯化钠是食盐主要成分"),
            Question("single","chemistry","二氧化碳可以用来做什么？", listOf("灭火","助燃","呼吸","炼钢"), 0, "CO2不燃烧不助燃，可灭火"),
            Question("judge","chemistry","水变成水蒸气是化学变化。", emptyList(), 0, "只是状态变化，没有新物质生成，是物理变化"),
            Question("single","chemistry","煤气中毒主要是什么气体？", listOf("二氧化碳","一氧化碳","氮气","甲烷"), 1, "CO一氧化碳无色无味易中毒"),
            Question("single","chemistry","pH值小于7的溶液是？", listOf("碱性","酸性","中性","不知道"), 1, "pH<7酸性，=7中性，>7碱性"),
            Question("judge","chemistry","胃酸的主要成分是盐酸。", emptyList(), 1, "胃液里有盐酸帮助消化"),
        ),

        // ========== 生物 ==========
        "biology" to listOf(
            Question("single","biology","植物进行光合作用需要什么？", listOf("阳光","黑暗","低温","盐水"), 0, "光合作用需要阳光、二氧化碳、水"),
            Question("single","biology","人体最大的器官是？", listOf("心脏","肝脏","皮肤","大脑"), 2, "皮肤是人体最大器官"),
            Question("judge","biology","鸡蛋里蛋黄是小鸡的胚胎。", emptyList(), 0, "蛋黄是营养物质，胚盘才是胚胎"),
            Question("single","biology","植物叶子秋天变黄是因为什么分解了？", listOf("叶绿素","叶黄素","花青素","糖分"), 0, "天冷叶绿素分解，显出黄色"),
            Question("single","biology","人体有多少颗牙齿（成人）？", listOf("28颗","30颗","32颗","36颗"), 2, "成人恒牙28-32颗（含智齿）"),
            Question("judge","biology","蜘蛛是昆虫。", emptyList(), 0, "蜘蛛有8条腿，是蛛形纲；昆虫6条腿"),
            Question("single","biology","人体呼吸时吸入的主要有用气体是？", listOf("氮气","氧气","二氧化碳","氢气"), 1, "呼吸需要氧气"),
            Question("single","biology","植物的根主要作用是？", listOf("光合作用","吸收水分和养分","开花","结果"), 1, "根吸收水和矿物质"),
            Question("judge","biology","鱼是用鳃呼吸的。", emptyList(), 1, "鱼用鳃滤取水中氧气"),
            Question("single","biology","蜜蜂中负责采蜜的是？", listOf("雄蜂","工蜂","蜂后","蜂王"), 1, "工蜂负责采蜜、筑巢、喂养"),
        ),

        // ========== 科学 ==========
        "science" to listOf(
            Question("single","science","地球绕太阳转一圈叫什么？", listOf("一天","一个月","一年","一个星期"), 2, "地球公转周期是一年"),
            Question("single","science","月亮本身会发光吗？", listOf("会","不会，反射太阳光","有时会","只在十五会"), 1, "月球反射太阳光"),
            Question("judge","science","地球是太阳系的中心。", emptyList(), 0, "太阳是太阳系中心，地球绕太阳转"),
            Question("single","science","恐龙生活在什么时代？", listOf("侏罗纪/中生代","唐朝","冰河世纪","远古人类时代"), 0, "恐龙繁盛于中生代侏罗纪"),
            Question("single","science","为什么先看到闪电后听到雷声？", listOf("光比声音快","声音比光快","眼睛比耳朵灵","不知道"), 0, "光速远快于声速"),
            Question("judge","science","星星之所以闪烁是因为大气扰动。", emptyList(), 1, "星光穿过大气层被折射晃动"),
            Question("single","science","水往低处流是因为？", listOf("风","地球引力","热胀冷缩","植物吸水"), 1, "重力作用"),
            Question("single","science","冬天摸铁块比木头冷是因为？", listOf("铁温度低","铁导热快","木头发热","铁吸收热量多"), 1, "铁导热快，把手上热量导走"),
            Question("judge","science","彩虹通常出现在雨后出太阳时。", emptyList(), 1, "雨后空气中有小水滴折射阳光形成彩虹"),
            Question("single","science","昆虫有几条腿？", listOf("4条","6条","8条","10条"), 1, "昆虫特征：6条腿、身体分头胸腹"),
        ),

        // ========== 地理 ==========
        "geography" to listOf(
            Question("single","geography","中国的首都是？", listOf("上海","北京","广州","深圳"), 1, "首都北京"),
            Question("single","geography","世界上最高的山是？", listOf("珠穆朗玛峰","泰山","富士山","乞力马扎罗"), 0, "珠峰高8848.86米"),
            Question("single","geography","长江有多长？", listOf("约6300公里","约5000公里","约8000公里","约3000公里"), 0, "长江约6300公里，中国第一长河"),
            Question("judge","geography","黄河是中国第一长河。", emptyList(), 0, "长江第一，黄河第二"),
            Question("single","geography","地球有几大洲？", listOf("5大洲","6大洲","7大洲","8大洲"), 2, "亚洲、欧洲、非洲、北美洲、南美洲、大洋洲、南极洲"),
            Question("single","geography","太平洋是世界上最大的洋吗？", listOf("是","不是，大西洋最大","不是，印度洋最大","不知道"), 0, "太平洋最大最深"),
            Question("judge","geography","中国在亚洲东部。", emptyList(), 1, "中国位于东亚"),
            Question("single","geography","冬天我国哪个方向最冷？", listOf("东北","东南","西南","华南"), 0, "东北冬天最冷，漠河极端低温"),
            Question("single","geography","太阳从哪个方向升起？", listOf("东方","西方","南方","北方"), 0, "太阳东升西落"),
            Question("judge","geography","海南是中国的一个省。", emptyList(), 1, "海南省，海口是省会"),
        ),

        // ========== 历史 ==========
        "history" to listOf(
            Question("single","history","秦朝统一六国的皇帝是？", listOf("秦始皇","汉武帝","唐太宗","宋太祖"), 0, "秦始皇嬴政灭六国统一天下"),
            Question("single","history","造纸术是谁发明改进的？", listOf("毕昇","蔡伦","张衡","华佗"), 1, "东汉蔡伦改进造纸术"),
            Question("single","history","唐朝的开国皇帝是？", listOf("李世民","李渊","李隆基","武则天"), 1, "李渊建立唐朝，李世民是唐太宗"),
            Question("judge","history","郑和下西洋是明朝的事。", emptyList(), 1, "明成祖派郑和七下西洋"),
            Question("single","history","「丝绸之路」最早开辟于哪个朝代？", listOf("秦朝","西汉","唐朝","宋朝"), 1, "西汉张骞出使西域开辟丝绸之路"),
            Question("single","history","万里长城最早是为了抵御谁？", listOf("匈奴","倭寇","蒙古","越南"), 0, "秦始皇连长城抵御匈奴"),
            Question("judge","history","孔子是春秋时期鲁国人。", emptyList(), 1, "孔子名丘，鲁国人，儒家创始人"),
            Question("single","history","鸦片战争发生在哪一年？", listOf("1840年","1849年","1894年","1900年"), 0, "1840年第一次鸦片战争"),
            Question("single","history","辛亥革命推翻了哪个朝代？", listOf("明朝","元朝","清朝","宋朝"), 2, "1911年辛亥革命推翻清朝"),
            Question("judge","history","「三顾茅庐」发生在东汉末年。", emptyList(), 1, "刘备三请诸葛亮，东汉末年三国时期"),
        ),

        // ========== 诗词 ==========
        "poetry" to listOf(
            Question("single","poetry","「白日依山尽」的下一句是？", listOf("黄河入海流","欲穷千里目","更上一层楼","登鹳雀楼"), 0, "王之涣《登鹳雀楼》"),
            Question("single","poetry","「谁知盘中餐」的下一句是？", listOf("粒粒皆辛苦","汗滴禾下土","锄禾日当午","春种一粒粟"), 0, "李绅《悯农》"),
            Question("single","poetry","「野火烧不尽」的下一句是？", listOf春风吹又生.name, "远芳侵古道","晴翠接荒城","一岁一枯荣"), 0, "白居易《赋得古原草送别》"),
            Question("judge","poetry","「举头望明月，低头思故乡」表达思乡之情。", emptyList(), 1, "李白思乡名作"),
            Question("single","poetry","「春眠不觉晓」的下一句是？", listOf处处闻啼鸟.name, "夜来风雨声","花落知多少","处处闻啼鸟"), 3, "孟浩然《春晓》"),
            Question("single","poetry","「海内存知己」的下一句是？", listOf天涯若比邻.name, "儿女共沾巾","无为在歧路","天涯若比邻"), 3, "王勃《送杜少府之任蜀州》"),
            Question("judge","poetry","「少壮不努力，老大徒伤悲」劝人珍惜时间。", emptyList(), 1, "《长歌行》名句"),
            Question("single","poetry","「会当凌绝顶」的下一句是？", listOf一览众山小.name, "齐鲁青未了","造化钟神秀","一览众山小"), 3, "杜甫《望岳》"),
            Question("single","poetry","「但愿人长久」是谁写的？", listOf("苏轼","李白","杜甫","白居易"), 0, "苏轼《水调歌头》"),
            Question("judge","poetry","「桃花潭水深千尺，不及汪伦送我情」是送别诗。", emptyList(), 1, "李白送别汪伦"),
        ),

        // ========== 歇后语 ==========
        "xiehouyu" to listOf(
            Question("single","xiehouyu","「芝麻开花」的下一句是？", listOf节节高.name, "一场空","七上八下","节节高"), 3, "芝麻开花节节高，形容越来越好"),
            Question("single","xiehouyu","「十五个吊桶打水」的下一句是？", listOf七上八下.name, "节节高","一场空","七上八下"), 3, "心神不定的样子"),
            Question("single","xiehouyu","「竹篮打水」的下一句是？", listOf一场空.name, "节节高","七上八下","一场空"), 3, "比喻白费力气"),
            Question("judge","xiehouyu","「黄鼠狼给鸡拜年」是没安好心。", emptyList(), 1, "伪装善良，实则害人"),
            Question("single","xiehouyu","「八仙过海」的下一句是？", listOf各显神通.name, " woo 一场空","七上八下","各显神通"), 3, "各自拿出本领"),
            Question("single","xiehouyu","「老鼠过街」的下一句是？", listOf人人喊打.name, "各显神通","节节高","人人喊打"), 3, "比喻人人痛恨"),
            Question("judge","xiehouyu","「王婆卖瓜」是自卖自夸。", emptyList(), 1, "自己夸耀自己的东西好"),
            Question("single","xiehouyu","「老虎屁股」的下一句是？", listOf摸不得.name, "人人喊打","各显神通","摸不得"), 3, "比喻惹不起"),
            Question("single","xiehouyu","「画蛇添足」比喻？", listOf多此一举.name, "恰到好处","画得好","多此一举"), 0, "做多余的事反而不好"),
            Question("judge","xiehouyu","「亡羊补牢」为时已晚。", emptyList(), 0, "亡羊补牢为时未晚，补救还来得及"),
        ),

        // ========== 礼仪 ==========
        "etiquette" to listOf(
            Question("single","etiquette","见长辈应该怎么称呼？", listOf直呼其名","叫「叔叔/阿姨/爷爷/奶奶」","哎","那个人"), 1, "长辈要有尊称"),
            Question("single","etiquette","吃饭时筷子应该怎么拿？", listOf插在饭里","拿在手里夹菜","挥来挥去","指着别人"), 1, "插筷子像上香，不礼貌"),
            Question("judge","etiquette","别人说话时随便打断是不礼貌的。", emptyList(), 1, "要等别人说完再开口"),
            Question("single","etiquette","收到别人帮助应该说什么？", listOf谢谢","哦","知道了","没事"), 0, "礼貌说谢谢"),
            Question("single","etiquette","去别人家做客应该？", listOf随便翻东西","提前打招呼、带小礼物","不请自来","乱走动"), 1, "做客要有礼"),
            Question("judge","etiquette","公共场所可以大声喧哗。", emptyList(), 0, "公共场所要保持安静"),
            Question("single","etiquette","打喷嚏时应该？", listOf对着别人","用手捂住嘴侧过头","无所谓","憋着"), 1, "卫生礼貌"),
            Question("single","etiquette","排队的时候应该？", listOf插队","按顺序排","挤来挤去","推别人"), 1, "先来后到"),
            Question("judge","etiquette","借了别人东西要按时还。", emptyList(), 1, "有借有还再借不难"),
            Question("single","etiquette","接电话第一句应该说？", listOf喂你好","喂，谁啊","没事挂了","喂，快说"), 0, "礼貌用语"),
        ),

        // ========== 唐诗 ==========
        "tangshi" to listOf(
            Question("single","tangshi","李白被称为？", listOf诗圣","诗仙","诗鬼","诗佛"), 1, "李白号青莲居士，诗仙"),
            Question("single","tangshi","杜甫被称为？", listOf诗仙","诗圣","诗魔","诗豪"), 1, "杜甫诗圣，诗作诗史"),
            Question("single","tangshi","「床前明月光」是谁写的？", listOf杜甫","李白","白居易","王维"), 1, "李白《静夜思》"),
            Question("judge","tangshi","唐诗是唐朝的诗歌。", emptyList(), 1, "唐诗是中国诗歌巅峰"),
            Question("single","tangshi","「春眠不觉晓」的作者是？", listOf孟浩然","李白","杜甫","王昌龄"), 0, "孟浩然《春晓》"),
            Question("single","tangshi","「白日依山尽」的作者是？", listOf王之涣","李白","杜甫","王维"), 0, "王之涣《登鹳雀楼》"),
            Question("judge","tangshi","王维的诗被称为「诗中有画」。", emptyList(), 1, "王维诗画双绝"),
            Question("single","tangshi","「谁知盘中餐，粒粒皆辛苦」作者是？", listOf李绅","李白","杜甫","白居易"), 0, "李绅《悯农》"),
            Question("single","tangshi","「桃花潭水深千尺」下一句是？", listOf不及汪伦送我情.name, "唯见长江天际流","一片冰心在玉壶","不及汪伦送我情"), 3, "李白赠汪伦"),
            Question("judge","tangshi","《长恨歌》是白居易写的。", emptyList(), 1, "白居易《长恨歌》写唐明皇杨贵妃"),
        ),

        // ========== 宋词 ==========
        "songci" to listOf(
            Question("single","songci","「大江东去，浪淘尽」是谁写的？", listOf苏轼","李清照","辛弃疾","柳永"), 0, "苏轼《念奴娇·赤壁怀古》"),
            Question("single","songci","「寻寻觅觅，冷冷清清」是谁写的？", listOf李清照","苏轼","陆游","欧阳修"), 0, "李清照《声声慢》"),
            Question("judge","songci","宋词分豪放派和婉约派。", emptyList(), 1, "苏轼辛弃疾豪放，李清照柳永婉约"),
            Question("single","songci","「但愿人长久，千里共婵娟」的词牌名是？", listOf水调歌头.name, "念奴娇","如梦令","水调歌头"), 3, "苏轼《水调歌头·明月几时有》"),
            Question("single","songci","辛弃疾是哪个朝代的？", listOf唐朝","北宋","南宋","元朝"), 2, "南宋豪放派词人"),
            Question("judge","songci","「莫等闲，白了少年头，空悲切」是岳飞写的。", emptyList(), 1, "岳飞《满江红》"),
            Question("single","songci","「醉里挑灯看剑」是谁写的？", listOf辛弃疾","苏轼","陆游","范仲淹"), 0, "辛弃疾《破阵子》"),
            Question("single","songci","宋词兴盛于哪个朝代？", listOf唐朝","宋朝","明朝","清朝"), 1, "宋朝是词的时代"),
            Question("judge","songci","李清照是婉约派代表。", emptyList(), 1, "李清照号易安居士，婉约宗主"),
            Question("single","songci","「问君能有几多愁」下一句是？", listOf恰似一江春水向东流.name, "春花秋月何时了","故国不堪回首","恰似一江春水向东流"), 3, "李煜《虞美人》"),
        ),

        // ========== 社会实践 ==========
        "social" to listOf(
            Question("single","social","过马路应该走什么？", listOf斑马线","随便穿","翻栏杆","车少直接跑"), 0, "走斑马线人行道"),
            Question("single","social","红绿灯红灯亮时应该？", listOf赶紧跑过去","停下等待","慢慢走","看心情"), 1, "红灯停绿灯行"),
            Question("judge","social","捡到钱应该交给警察或失主。", emptyList(), 1, "拾金不昧是美德"),
            Question("single","social","发生火灾应该拨打什么电话？", listOf110","119","120","114"), 1, "119火警"),
            Question("single","social","有人晕倒应该拨打什么电话？", listOf119","120","110","122"), 1, "120急救"),
            Question("judge","social","可以随便跟陌生人走。", emptyList(), 0, "安全第一，不跟陌生人走"),
            Question("single","social","垃圾分类中，废电池属于？", listOf可回收","有害垃圾","厨余垃圾","其他垃圾"), 1, "电池有毒有害"),
            Question("single","social","节约用水应该？", listOf开着水龙头洗东西","随手关紧水龙头","多放水","浪费没关系"), 1, "节约用水人人有责"),
            Question("judge","social","公共场所插队是不文明行为。", emptyList(), 1, "要排队"),
            Question("single","social","小朋友独自在家有人敲门应该？", listOf马上开门","从猫眼看，不认识就不开","喊进来","隔着门说爸妈不在家"), 1, "安全第一，不给陌生人开门"),
        ),

        // ========== 野外求生 ==========
        "survival" to listOf(
            Question("single","survival","野外迷路了应该？", listOf乱走","留在原地等救援","随便选个方向跑","爬上树睡觉"), 1, "迷路不乱跑，原地等待最安全"),
            Question("single","survival","野外没有水应该喝什么？", listOf河水直接喝","找水源净化后喝","尿","露水也不行"), 1, "生水有细菌，要烧开或净化"),
            Question("judge","survival","野外看到不认识的野果子可以随便吃。", emptyList(), 0, "野果可能有毒，不能乱吃"),
            Question("single","survival","野外过夜最好找什么地方搭帐篷？", listOf河边","高地平坦处","悬崖下","山洞深处"), 1, "高地防洪水，平坦安全"),
            Question("single","survival","发出求救信号SOS是几短几长？", listOf三短三长三短","两短两长","四短四长","一短一长"), 0, "SOS：三短三长三短"),
            Question("judge","survival","打雷下雨天可以在大树下躲雨。", emptyList(), 0, "大树易遭雷击，不能躲树下"),
            Question("single","survival","野外遇到蛇应该？", listOf跑过去","绕开慢慢走","踩它","抓它"), 1, "不惊动蛇，慢慢绕开"),
            Question("single","survival","太阳在天上，北半球怎么分辨方向？", listOf太阳升起方向是东","太阳方向是西","随便猜","看草往哪边倒"), 0, "太阳东升西落"),
            Question("judge","survival","野外生火后一定要彻底熄灭再离开。", emptyList(), 1, "防止森林火灾"),
            Question("single","survival","冬天野外失温应该？", listOf剧烈运动产热","找避风处、干燥处保暖","喝冰水","脱衣服"), 1, "保暖防风防湿最重要"),
        ),
    )

    /**
     * 程序化生成数学题（无限题库，按难度）
     * difficulty: 1=简单 2=一般 3=难 4=奥林匹克
     */
    private fun genMathQuestion(difficulty: Int = 2): Question {
        val (min, max) = when (difficulty) {
            1 -> 1 to 20      // 简单：20以内
            2 -> 10 to 99     // 一般：两位数
            3 -> 100 to 999   // 难：三位数
            else -> 10 to 999  // 奥林匹克：综合
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
            else -> { // 奥林匹克：质数判断
                val n = if (difficulty >= 4) (10..200).random() else (10..100).random()
                var isPrime = n > 1
                for (i in 2 until n) { if (n % i == 0) { isPrime = false; break } }
                Question("judge", "math", "$n 是质数。", emptyList(), if (isPrime) 1 else 0, if (isPrime) "$n是质数" else "$n不是质数")
            }
        }
    }

    /**
     * 随机取一道题（合并内置题库+用户题库，按指定学科筛选）
     */
    fun randomQuestion(ctx: android.content.Context, subject: String = "all"): Question {
        val diff = Prefs.getDifficulty(ctx)
        // 数学题有50%概率用程序化生成，相当于无限题库
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
                val subj = if (json.has("subject")) json.getString("subject") else "custom"
                if (subject == "all" || subj == subject || subject == "custom") {
                    pool.add(Question(type, subj, q, options, answer, explain))
                }
            } catch (_: Exception) {}
        }
        return if (pool.isEmpty()) genMathQuestion() else pool.random()
    }

    fun getSubjectName(id: String): String = subjects.find { it.id == id }?.name ?: id
}
