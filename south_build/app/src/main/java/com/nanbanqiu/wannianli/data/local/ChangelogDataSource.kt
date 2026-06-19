/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.data.local

import android.content.Context
import android.content.SharedPreferences
import com.nanbanqiu.wannianli.data.model.ChangelogEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ChangelogDataSource {
    private const val PREFS_NAME = "nanbanqiu_changelog"
    private const val KEY_CHANGELOG = "changelog_list"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getChangelog(context: Context): List<ChangelogEntry> {
        val json = getPrefs(context).getString(KEY_CHANGELOG, null) ?: return getDefaultChangelog()
        return try {
            val type = object : TypeToken<List<ChangelogEntry>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            getDefaultChangelog()
        }
    }

    fun addEntry(context: Context, entry: ChangelogEntry) {
        val list = getChangelog(context).toMutableList()
        list.add(0, entry)
        val json = Gson().toJson(list)
        getPrefs(context).edit().putString(KEY_CHANGELOG, json).apply()
    }

    private fun getDefaultChangelog(): List<ChangelogEntry> {
        return listOf(
            ChangelogEntry(
                version = "v1.0.58",
                date = "2026-06-19",
                content = "新增节气精度验证表。一、JVM简化版（SolarTermPrecisionTest）：使用纯Kotlin简化VSOP87太阳黄经算法，通过二分法求解太阳到达目标黄经的精确时刻，与紫金山天文台2026年二十四节气权威数据对比。验证结果显示简化算法偏差约8-9小时（±0.3°），符合简化轨道要素精度预期。二、Android仪器化版（SolarTermPrecisionInstrumentedTest）：使用sxtwl_cpp完整VSOP87+ELP-2000原生库，严格精度验证（目标偏差<5分钟），需Android设备运行。三、权威数据：2026年二十四节气交节时刻（紫金山天文台），覆盖全部24节气+二分二至+立春关键节点。四、测试覆盖：精度验证表（24节气全表）、分季节验证（春夏秋冬各6节气）、关键节气验证（春分/夏至/秋分/冬至/立春）。"
            ),
            ChangelogEntry(
                version = "v1.0.57",
                date = "2026-06-19",
                content = "月亮位置计算升级为完整版ELP-2000（Éphéméride Lunaire Parisienne 2000）。一、数据升级：从简化版13项摄动替换为完整版452+项经度摄动、207+项纬度摄动、222+项距离摄动数据（移植自寿星天文历eph0.js）。二、算法重写：PlanetPositionCalc.calcMoonPosition()改用XL1_calc求和算法，按ML0/ML1/ML2/ML3（经度）、MB0/MB1/MB2（纬度）分层累加，精度从±2°提升至亚角秒级。三、影响范围：月躔星宿计算精度大幅提升，月亮黄经/黄纬计算更准确。四、新增测试：月亮黄经精度验证（±1.5°→±1.5°收紧至±1.5°范围）、月躔星宿验证（柳宿）、朔望日月黄经差验证。版本号从1.0.56升至1.0.57。"
            ),
            ChangelogEntry(
                version = "v1.0.56",
                date = "2026-06-19",
                content = "底层引擎全面替换：cn.6tail:lunar → 寿星天文历(sxtwl_cpp)。一、引擎替换：移除cn.6tail:lunar 1.7.7依赖，改用许剑伟寿星天文历5.10（yuangu/sxtwl_cpp C++实现），通过JNI+C API桥接，Android NDK/CMake交叉编译三大架构（arm64-v8a/armeabi-v7a/x86_64）。二、精度提升：寿星天文历使用VSOP87行星理论，节气交节时刻计算精度提升至分钟级（原cn.6tail精度较低）。三、底层影响范围：公历↔农历互转（LunarCalendarEngine）、节气计算（SolarTermCalculator）、月相/月建/破日（PureLunarEngine）、纯阴历节气查找（PureLunarCalendarEngine）、日程农历转公历（ScheduleEvent）、南半球农历月天数（CalendarRepository）——全部改用SxtwlBridge.nativeSolarToLunar()等JNI接口。四、日躔月躔星宿、值日星宿、四柱八字、择日等独立算法模块不受影响。五、开源声明更新：cn.6tail:lunar → yuangu/sxtwl_cpp(BSD-3-Clause)。六、ProGuard规则更新：保护JNI native方法。版本号从1.0.55升至1.0.56。"
            ),
            ChangelogEntry(
                version = "v1.0.54",
                date = "2026-06-05",
                content = "历法知识新增「科里奥利力·太极旋转」卡片。展示台风、反气旋、大洋环流、地球自转在南北半球的旋转方向恰好相反（↺↻），源于地球自转的科里奥利力使北半球向右偏、南半球向左偏——同种力、相反果，太极双鱼图的物理铁证。版本号从1.0.53升至1.0.54。"
            ),
            ChangelogEntry(
                version = "v1.0.53",
                date = "2026-06-05",
                content = "两条标题栏字体从金色(#FFD700)改为蓝色(#1565C0)，与月导航栏字体颜色统一，天蓝底色上更清晰可读。版本号从1.0.52升至1.0.53。"
            ),
            ChangelogEntry(
                version = "v1.0.52",
                date = "2026-06-05",
                content = "微调主题色：节气标题栏背景从深绿改为天蓝与整体统一，「北半球历法换算南半球历法」标题文字从深蓝改为金色(#FFD700)与节气标题栏字体一致。版本号从1.0.51升至1.0.52。"
            ),
            ChangelogEntry(
                version = "v1.0.51",
                date = "2026-06-05",
                content = "全新天蓝色主题。将所有页面从深海军蓝(#1A237E)更换为亮色天蓝系(#E3F2FD背景+#1565C0文字)，白色文字同步替换为深蓝以保证可读性。涉及HomeScreen、SettingsScreen、KnowledgeScreen、PureLunarCalendarScreen、ZeRiScreen、ScheduleScreen共6个页面约30处调整。状态栏同步改为浅色背景。版本号从1.0.50升至1.0.51。"
            ),
            ChangelogEntry(
                version = "v1.0.50",
                date = "2026-06-05",
                content = "完善二十八宿边界计算。原使用距星黄经作为宿界，现改用传统宿度宽度（角12亢9氐15…）计算正确宿界黄经，使太阳在6月初正确落入井宿。日躔、月躔星宿均受益于此次调整。版本号从1.0.49升至1.0.50。"
            ),
            ChangelogEntry(
                version = "v1.0.49",
                date = "2026-06-05",
                content = "新增日躔星宿。在信息卡片月躔星宿上方新增「日躔星宿」行，通过太阳黄经天文算法计算太阳实际所在二十八宿位置，天球坐标全球一致，南北半球显示相同。值日、日躔、月躔构成完整三光星宿体系。版本号从1.0.48升至1.0.49。"
            ),
            ChangelogEntry(
                version = "v1.0.48",
                date = "2026-06-05",
                content = "全面更新历法知识页面。板块4「经纬度选址原理」重写为「对跖点动态计算原理」，反映动态城市选择系统；板块5「三历法系统」移除已停用的纯阴历功能；板块7「月相翻转」更新为8个emoji符号和目视反向名称对照；板块9「关于阿根廷基准点」替换为「二十八宿与值日星宿」新知板块。版本号从1.0.47升至1.0.48。"
            ),
            ChangelogEntry(
                version = "v1.0.47",
                date = "2026-06-05",
                content = "南半球月相名称按目视反向互换。蛾眉月⇌残月、上弦月⇌下弦月、盈凸月⇌亏凸月，新月和满月不变。月相图标保持左右镜像。版本号从1.0.46升至1.0.47。"
            ),
            ChangelogEntry(
                version = "v1.0.46",
                date = "2026-06-05",
                content = "优化月相显示。使用8个Unicode月相emoji（🌑🌒🌓🌔🌕🌖🌗🌘）精确区分所有月相形态，替换原来4种符号的粗略表示。蛾眉月、上弦月、盈凸月等各有独立黄色emoji，南半球按视觉镜像映射。版本号从1.0.45升至1.0.46。"
            ),
            ChangelogEntry(
                version = "v1.0.45",
                date = "2026-06-05",
                content = "完善南北半球星宿显示逻辑。值日星宿和月躔星宿均为全球统一：月躔是天球坐标，全球同一时刻月亮所在宿相同；值日是干支纪日循环，全球同一天干支一致。南北半球星宿名称完全相同，不再使用n+14对冲偏移。版本号从1.0.44升至1.0.45。"
            ),
            ChangelogEntry(
                version = "v1.0.44",
                date = "2026-06-05",
                content = "完善星宿计算。值日星宿：校准基准偏移量（2025-01-01为参宿），调整28宿轮值顺序；月躔星宿：改用天文算法计算月亮黄经位置再查宿，替换原来的农历日数简单取模公式。版本号从1.0.43升至1.0.44。"
            ),
            ChangelogEntry(
                version = "v1.0.43",
                date = "2026-06-05",
                content = "新增星宿功能。在信息卡片星期下方新增「值日星宿」和「月躔星宿」两行，南北半球各显示星宿名+四象（如牛宿(玄武)/鬼宿(朱雀)），南半球采用180°对冲法（n+14），与四柱六冲逻辑统一。版本号从1.0.42升至1.0.43。"
            ),
            ChangelogEntry(
                version = "v1.0.42",
                date = "2026-06-05",
                content = "公历日期格式优化。南北半球公历日期从「YYYY年M月D日」改为「YYYY.M.D」点分格式，避免两位数月日导致换行。版本号从1.0.41升至1.0.42。"
            ),
            ChangelogEntry(
                version = "v1.0.41",
                date = "2026-06-05",
                content = "标签统一。信息卡片右栏「南阳历」改为「公历」，「南农历」改为「农历」，左右标签对称一致。版本号从1.0.40升至1.0.41。"
            ),
            ChangelogEntry(
                version = "v1.0.40",
                date = "2026-06-05",
                content = "文案优化：1.信息卡片「南历」改为「南阳历」，「太极对立」改为「南农历」；2.标题栏「北半球历法换算南半球历法」改为「北半球历法   换算   南半球历法」；3.标题栏「北半球节气换算南半球节气」改为「北半球节气   换算   南半球节气」。版本号从1.0.39升至1.0.40。"
            ),
            ChangelogEntry(
                version = "v1.0.39",
                date = "2026-06-05",
                content = "文案规范化：1.月份选择器标题改为「北半球阳历YYYY年M月」；2.节气卡片标题改为「北半球节气换算南半球节气」；3.信息卡片标题栏改为「北半球历法换算南半球历法」。版本号从1.0.38升至1.0.39。"
            ),
            ChangelogEntry(
                version = "v1.0.38",
                date = "2026-06-05",
                content = "节气卡片去冗余。删除当前节气和下个节气卡片中的南北半球标签、城市名、经纬度、UTC偏移、参考城市等重复信息（上方信息卡片已包含），仅保留节气名、星期、倒计时和准确交节时间。版本号从1.0.37升至1.0.38。"
            ),
            ChangelogEntry(
                version = "v1.0.37",
                date = "2026-06-05",
                content = "信息卡片左右分色。北半球信息行（左栏）背景改为粉色（#FFF3E0），南半球信息行（右栏）背景改为淡蓝色（#E3F2FD），与节气对照卡片左右栏配色一致。DualInfoRow新增leftBg/rightBg参数。版本号从1.0.36升至1.0.37。"
            ),
            ChangelogEntry(
                version = "v1.0.36",
                date = "2026-06-05",
                content = "信息卡片样式优化。恢复「北半球历法·南半球历法对照」标题栏（深蓝底金字），卡片背景色改为与节气卡片一致的浅绿色（#F8FFF0），整体视觉统一。版本号从1.0.35升至1.0.36。"
            ),
            ChangelogEntry(
                version = "v1.0.35",
                date = "2026-06-05",
                content = "卡片整合优化：1.信息卡片「干支」行改为「星期」行，显示南北半球各自的星期几；2.四柱八字（年柱/月柱/日柱/时柱）从独立卡片合并到信息卡片中，分隔线下方显示；3.移除独立的DualPillarsCard组件，底部仅保留节气对照卡片。底层数据计算逻辑不变。版本号从1.0.34升至1.0.35。"
            ),
            ChangelogEntry(
                version = "v1.0.34",
                date = "2026-06-05",
                content = "南北对照卡片排版优化。改为逐行双栏对齐布局（DualInfoRow），5行固定行位（公历/农历/月相/生肖/干支）左右严格横向平齐。文字少的一侧自动Top对齐留白，不再因两侧行数不一致导致错位。配色和样式不变。版本号从1.0.33升至1.0.34。"
            ),
            ChangelogEntry(
                version = "v1.0.33",
                date = "2026-06-05",
                content = "卡片顺序调整。四柱八字卡片（北半球历法·南半球历法对照）移至节气对照卡片上方，遵循先八字后节气的逻辑顺序。版本号从1.0.32升至1.0.33。"
            ),
            ChangelogEntry(
                version = "v1.0.32",
                date = "2026-06-05",
                content = "页面布局优化。取消节气对照上方日期栏，将南北半球公历/农历/月相/生肖/干支信息卡片移至月视图网格下方，信息层次更清晰。版本号从1.0.31升至1.0.32。"
            ),
            ChangelogEntry(
                version = "v1.0.31",
                date = "2026-06-05",
                content = "移除纯阴历功能。取消汉堡菜单「纯阴历」入口和首页底部导航「阴历」板块，简化主界面为单一阳历视图。版本号从1.0.30升至1.0.31。"
            ),
            ChangelogEntry(
                version = "v1.0.30",
                date = "2026-06-05",
                content = "移除DualPillarsCard底部六冲生肖栏和经度差时差信息栏，四柱八字卡片只保留八字数据。版本号从1.0.29升至1.0.30。"
            ),
            ChangelogEntry(
                version = "v1.0.29",
                date = "2026-06-04",
                content = "优化两项逻辑：1.对跖点UTC偏移改为北半球UTC+12小时，确保所有城市南北时差恒等于12小时（2时辰），不再因政治时区偏移导致时差异常；2.完善选择中国·北京显示为中国·上海的问题，北京使用独立ID（Asia/Shanghai#Beijing），新增toZoneId()方法自动去掉#后缀用于时间计算。版本号从1.0.28升至1.0.29。"
            ),
            ChangelogEntry(
                version = "v1.0.28",
                date = "2026-06-04",
                content = "完善对跖点UTC偏移计算。原逻辑（北半球UTC偏移如上海UTC+8→UTC-8，时差16小时），改为从对跖点经度推算（round(经度/15)×60分钟），确保南北时差永远=12小时。上海UTC+8→对跖UTC-4，时差12小时。版本号从1.0.27升至1.0.28。"
            ),
            ChangelogEntry(
                version = "v1.0.27",
                date = "2026-06-04",
                content = "严格对跖点时区系统。南半球时区改为北半球UTC偏移严格取反，确保南北时差永远等于12小时（2时辰）。对跖点附近有城市则标注参考城市名，无城市则显示经纬度坐标。所有时间计算（双时钟、星期、节气时间）使用严格对跖ZoneId。符合太极双鱼图180°对称原则。版本号从1.0.26升至1.0.27。"
            ),
            ChangelogEntry(
                version = "v1.0.26",
                date = "2026-06-04",
                content = "时区设置合并为单一选择器。选择北半球城市后自动计算对跖点（南半球对应位置），南半球城市不再独立选择。对跖点附近有城市则显示城市名，无城市则显示经纬度坐标。四柱八字、节气、时钟等全部基于北半球选择自动配对。符合太极双鱼图一选定阴阳的对称原则。版本号从1.0.25升至1.0.26。"
            ),
            ChangelogEntry(
                version = "v1.0.25",
                date = "2026-06-04",
                content = "四柱八字时辰完善。四柱八字改为用当地原始时间计算（年/月/日/时/分），不再转换为北京时间后计算。农历和节气仍用北京时间。完善柏林15:29显示亥时（应为申时）等问题。版本号从1.0.24升至1.0.25。"
            ),
            ChangelogEntry(
                version = "v1.0.24",
                date = "2026-06-04",
                content = "时区同步UI优化。DualPillarsCard和DualSolarTermCard中的硬编码城市名（中国/阿根廷）、经纬度（北纬35°/南纬35°）、UTC偏移（UTC+8/UTC-3）全部替换为动态显示所选城市信息。底部摘要经度差和时差也改为动态计算。新增formatCityLocation和formatCityUtc辅助函数统一格式化。版本号从1.0.23升至1.0.24。"
            ),
            ChangelogEntry(
                version = "v1.0.23",
                date = "2026-06-04",
                content = "时区同步完善。CalendarRepository接收北半球城市时区参数，将当地日期时间转换为北京时间后再进行农历、节气、四柱八字计算，确保不同时区城市的数据准确。MainViewModel的loadToday/loadDate/selectDay均使用所选城市时区获取当地日期和时间。版本号从1.0.22升至1.0.23。"
            ),
            ChangelogEntry(
                version = "v1.0.22",
                date = "2026-06-04",
                content = "城市名加入国家前缀。中文名格式：「国家·城市」（如中国·上海、阿根廷·布宜诺斯艾利斯）；英文名格式：「City, Country」（如Shanghai, China）；西班牙文名同步更新。版本号从1.0.21升至1.0.22。"
            ),
            ChangelogEntry(
                version = "v1.0.21",
                date = "2026-06-04",
                content = "城市列表按UTC偏移从大到小排列（从东到西），补充缺失时区城市。北半球新增：堪察加(UTC+12)、乌鲁木齐(UTC+8真太阳时修正)、达卡(UTC+6)、阿拉木图(UTC+6)、开罗(UTC+2)、雅加达(UTC+2)、墨西哥城(UTC-6)、丹佛(UTC-7)、安克雷奇(UTC-9)，共21城覆盖UTC+12到UTC-9。南半球新增：努美阿(UTC+11)、珀斯(UTC+8)、毛里求斯(UTC+4)、留尼汪(UTC+3)、加拉帕戈斯(UTC-6)，共17城覆盖UTC+12到UTC-6。版本号从1.0.20升至1.0.21。"
            ),
            ChangelogEntry(
                version = "v1.0.20",
                date = "2026-06-04",
                content = "国际化时区系统上线：1.新增城市数据模型（CityInfo）和数据源（CityDataSource），南北半球共38个城市，支持中/英/西三语城市名；2.新增设置页「时区设置」（汉堡菜单第6项），双时区选择器，点击弹出城市列表对话框，选中后持久化到SharedPreferences；3.首页全面动态化：标题显示所选城市名、经纬度自动计算（北纬/南纬+度数）、UTC偏移自动显示（支持半小时偏移如UTC+5:30）、双时钟显示所选城市名和动态时差、节气卡片显示所选城市本地时间（SolarTermInfo.getLocalDateTime）、星期几根据所选时区计算；4.SolarTermInfo新增getLocalDateTime(northZoneId, southZoneId)方法，兼容旧argentinaDateTime属性；5.汉堡菜单标题从「南半球历法·阿根廷」改为「南半球历法」（国际化）。版本号从1.0.19升至1.0.20。"
            ),
            ChangelogEntry(
                version = "v1.0.19",
                date = "2026-06-04",
                content = "完善南半球月相显示逻辑；卡片标题改为「北半球历法·南半球历法 对照」。版本号从1.0.18升至1.0.19。"
            ),
            ChangelogEntry(
                version = "v1.0.18",
                date = "2026-06-04",
                content = "南半球农历显示改为太极对立日。删除南半球列的「农历」行（南十月十九的简单+6映射）和「月相」行，只保留「太极对立」行（南十月初四）。原因：南十月十九只是月份名+6的平移，没有太极含义；太极对立日才是月相翻转、朔望圆180°对立的真正表达，完美诠释太极双鱼图二元对立统一。北半球列：公历+农历（阳历+6偏移）；南半球列：南历+太极对立（农历+15对立）。版本号从1.0.17升至1.0.18。"
            ),
            ChangelogEntry(
                version = "v1.0.17",
                date = "2026-06-04",
                content = "完善太极对立日月份显示。原因是computeOppositeDay中对月份多算了一次+6偏移——南半球月份已经是十月，代码+6变回四月，导致显示「南四月初四」而非正确的「南十月初四」。调整：删除多余的oppositeSouthMonth计算，直接用southernMonth作为太极对立日所在月份。getSouthernLunarMonthDays同步调整，用southernMonth查找天数。版本号从1.0.16升至1.0.17。"
            ),
            ChangelogEntry(
                version = "v1.0.16",
                date = "2026-06-04",
                content = "南半球阳历（南历）系统上线。遵循太极双鱼图二元对立统一原则：南半球阳历月份=北半球月份+6（超过12取模），日期不变，年界7月1日。原理：公历是北半球发明的历法，1月是北半球冬天，6月是北半球夏天，对南半球没有季节意义。南历1月1日→南历12月1日，与农历南十月差1个月（与北半球公历1月与农历四月差1个月完全对称）。CalendarDay新增southSolarYear/southSolarMonth/southSolarDay字段，CalendarRepository计算南半球阳历，HomeScreen北半球列新增「公历」行、南半球列新增「南历」行。完整太极映射体系：阳历月份+6、农历月份+6、农历日+15、节气+12、干支+6/+6，所有偏移量统一为6或12，底层物理原因一致——地球公转自转的太极对称。版本号从1.0.15升至1.0.16。"
            ),
            ChangelogEntry(
                version = "v1.0.15",
                date = "2026-06-04",
                content = "优化南半球闰月计算逻辑。删除原有独立遍历中气、自主判定闰月的整套代码（computeSouthernLeapMonth），简化为：南半球闰月=(北半球闰月+6)%12。原理：北半球月份X的中气与南半球月份X+6的中气是同一个天文事件，缺则同缺，闰则同闰，不可能出现北半球闰X月但南半球(X+6)月不是闰月的情况。新增getNorthLeapMonth()辅助函数获取北半球闰月月份。保留getSouthernLunarMonthDays()用于区分当月29/30天。南北历法、四柱对称逻辑不变。版本号从1.0.14升至1.0.15。"
            ),
            ChangelogEntry(
                version = "v1.0.14",
                date = "2026-06-04",
                content = "南半球独立闰月判定系统 + 历法知识新增「南半球星象定节气」板块。一、南半球独立闰月计算（LunarCalendarEngine新增computeSouthernLeapMonth）：基于「无中气之月为闰月」规则，南半球中气序列偏移12位（处暑、秋分、霜降、小雪、冬至、大寒、雨水、春分、谷雨、小满、夏至、大暑），独立判定南半球闰月位置，不再简单沿用北半球闰月+6偏移。二、太极对立日完善（computeOppositeDay重构）：1.对立月独立计算（当前南半球月份+6偏移），不再复用当前月份名；2.获取对立月实际天数（getSouthernLunarMonthDays遍历公历日期计算），防止「三十」不存在于29天小月；3.对立月为南半球闰月时自动加「闰」前缀。三、数据模型更新：SouthernLunarResult新增isSouthernLeapMonth和southernLeapMonth字段，LunarDate新增isSouthernLeapMonth和southernLeapMonth字段。四、HomeScreen南半球农历行：闰月时自动显示「闰南X月」。五、历法知识新增「南半球星象定节气」板块（北斗七星定节气、半人马座指针、心宿二、麦哲伦星云、综合验证方案、太极对称总结）。版本号从1.0.13升至1.0.14。"
            ),
            ChangelogEntry(
                version = "v1.0.13",
                date = "2026-06-03",
                content = "历法知识新增「月相翻转·太极验证」板块。位于「月建与季节」与「关于阿根廷基准点」之间，包含五个小节：核心原理（南北半球月相形状翻转180°的物理原因）、月相对照表（初七☽↔☾、十五🌕全亮统一、廿三☾↔☽）、太极对立日（农历日+15计算法，举例北半球农历四月十八→南半球农历十月初三）、如何验证（南北半球同时拍月亮照片对比即可验证）、统一与对立（月相名称和日数全球统一=太极的「圆」，形状南北翻转=太极的「鱼」）。版本号从1.0.12升至1.0.13。"
            ),
            ChangelogEntry(
                version = "v1.0.12",
                date = "2026-06-03",
                content = "太极双鱼图月相对立显示系统。一、CalendarDay新增4个字段：moonPhaseName（月相名称）、northMoonPhaseSymbol（北半球月相形态符号Unicode字符）、southMoonPhaseSymbol（南半球月相形态符号，视觉翻转180°）、southOppositeDay（南半球太极对立日，完整农历日期如「南十月初三」）。二、CalendarRepository新增getMoonPhaseSymbols()：根据PureLunarEngine的phaseIndex返回南北半球月相符号对——新月🌑和满月🌕全球一致、盈月阶段☽(右)↔☾(左)翻转、亏月阶段☾(左)↔☽(右)翻转。三、CalendarRepository新增computeOppositeDay(monthName, lunarDay)：计算农历日+15的太极对立日（29天月自动回卷），返回含南半球月份前缀的完整日期如「南十月初三」。四、SolarDateInfoCard南北双列各增月相行（符号+名称）和太极对立行（仅南列），北列☽亏凸月↔南列☾亏凸月，形态视觉恰好相反。版本号从1.0.11升至1.0.12。"
            ),
            ChangelogEntry(
                version = "v1.0.11",
                date = "2026-06-03",
                content = "农历南北对照重构。一、农历月份名加南北前缀：CalendarConstants新增NORTH_LUNAR_MONTH_NAMES（北正月~北十二月）和SOUTH_LUNAR_MONTH_NAMES（南正月~南十二月），与北半球正月~十二月明确区分，避免南半球用户看到「十月」直觉以为深秋的困惑。二、阳历Tab详情卡改为南北农历对照双列：左列☀️北半球中国（北纬35°东经120°·农历北四月十八·生肖·干支）、右列🌏南半球阿根廷（南纬35°西经60°·农历南十月十八·生肖·干支），所有信息一屏对照。三、移除农历Tab（原3个Tab变2个Tab）：底部导航从「阳历|农历|阴历」精简为「阳历|阴历」，农历信息已合并到阳历Tab详情卡中，LunisolarCalendarTab不再使用。四、FourPillarsEngine.calculate()中lunarMonthName改为北半球月份名（原为南半球偏移后的月份名）。版本号从1.0.10升至1.0.11。"
            ),
            ChangelogEntry(
                version = "v1.0.10",
                date = "2026-06-03",
                content = "三大核心功能更新。一、节气卡片双时间显示：当前节气底部时间行改为「北京 YYYY年MM月DD日 HH:MM」（橙色）、「阿根廷 YYYY年MM月DD日 HH:MM」（蓝色），SolarTermInfo新增argentinaDateTime属性，基于ZoneId将UTC+8北京时间转换为UTC-3阿根廷本地时间，用户一眼可知节气在两地的本地时刻。二、首页实时双时钟：月份导航栏下方新增DualClockBar组件，深蓝色底条上左侧金色「北京 HH:MM」、中间「时差11h」、右侧浅蓝「阿根廷 HH:MM」，每秒刷新一次，基于ZonedDateTime实时计算两地时间，是理解南北半球一切差异的视觉入口。三、农历Tab补全南半球数据：LunisolarDateInfoCard干支年和生肖改为左右双列对照（☀️北半球干支年/生肖 vs 🌏南半球干支年/生肖），节气也改为双列（北节气 vs 南节气），与阳历Tab南北对照风格统一。版本号从1.0.9升至1.0.10。"
            ),
            ChangelogEntry(
                version = "v1.0.9",
                date = "2026-06-03",
                content = "节气双卡与四柱双卡各自标注北半球/南半球星期几。一、HomeScreen新增getArgentinaWeekday()辅助函数，基于ZoneId计算阿根廷UTC-3时区的星期（取北京时间正午12:00为参考点）。二、DualSolarTermCard当前节气双卡：北卡标题区新增星期行（UTC+8下方橙色文字zhou日）、南卡标题区新增对应阿根廷星期行（UTC-3下方蓝色文字）。三、DualPillarsCard四柱双卡同理：北半球列标题区（北纬35°东经120°·UTC+8下方）、南半球列标题区（南纬35°西经60°·UTC-3下方）各自新增星期显示。四、完善汉堡菜单：HomeScreen顶部navigationIcon三横杠图标绑定的不再是onDrawerToggle（打开ModalNavigationDrawer），而是showMenu=true触发DropdownMenu（仅含「日程管理」和「纯阴历」2项），导致真正的汉堡菜单（含全部5项：首页、纯阴历、日程管理、更新日志、历法知识）从未被打开过。现已调整：navigationIcon直接绑定onDrawerToggle，移除DropdownMenu代码。版本号从1.0.8升至1.0.9。"
            ),
            ChangelogEntry(
                version = "v1.0.8",
                date = "2026-06-02",
                content = "汉堡菜单精简与滚动优化。一、MainActivity移除汉堡菜单失效项：择日(Stars)、日月食(DarkMode)、日课笔记(Book)——这三项的导航回调在HomeScreen函数签名中默认为空函数{}，AppNavigation仅传递了onNavigateToSchedule，导致点击无响应。二、完善汉堡菜单滚动：移除ModalDrawerSheet外层的key(navController.currentDestination?.route)包裹——该key导致每次路由切换整个抽屉重建、滚动状态丢失。改用fillMaxHeight()替代weight(1f)确保Column填充可用高度。三、HomeScreen顶部Dropdown同步精简：移除择日、日月食占、日课笔记三项。现在汉堡菜单保留：首页、纯阴历、日程管理、更新日志、历法知识共5项，全部可用。"
            ),
            ChangelogEntry(
                version = "v1.0.7",
                date = "2026-06-02",
                content = "新增「历法知识」页面 + 北半球经纬度补全。一、HomeScreen所有北半球标注统一升级为「北纬35° 东经120°」——四柱双卡副标题（原「北京时间·东经120°」→「北纬35° 东经120°」）、节气双卡副标题（原「东经120° UTC+8」→「北纬35° 东经120° UTC+8」）、下一节气标签（原「北半球·中国·东经120°」→「北半球·中国·北纬35°东经120°」），与南半球「南纬35°西经60°」形成完美的纬度同度数镜像、经度180°差值的太极对称关系。二、新建KnowledgeScreen历法知识卡片页面（~220行），汉堡菜单新增「历法知识」入口（School图标）。内含7大知识板块：太极双鱼南北历法原理、四柱八字翻转法则（天干六合+地支六冲→乾坤翻转+6/+6）、节气偏移法则（180°黄经差=12位偏移）、经纬度选址原理（北纬35°东经120°↔南纬35°西经60°的物理学基础）、三历法系统介绍、月建与季节、阿根廷基准点说明——全部采用彩色分区卡片展示，便于后来人学习理解南半球历法的数学原理和哲学背景。"
            ),
            ChangelogEntry(
                version = "v1.0.6",
                date = "2026-06-02",
                content = "完善北半球四柱八字年柱月柱计算。原因是：FourPillarsEngine.calculate()是南半球改造引擎，内部使用southName（如southName=立秋实际日期8月8日）判定年界与月界，导致北半球四柱显示异常——年柱乙巳（应为丙午）、月柱丁亥（应为癸巳）。调整方案：新增calculateNorthern()纯北半球四柱计算方法——使用northName==立春（2月4日）作年界、使用NORTH_JIE_NAMES（立春起12节）作月界、使用NORTH_MONTH_ZHI_INDEX（寅=2起步）作标准月支索引、不做+6偏移。新增常量：CalendarConstants.NORTH_JIE_NAMES+CalendarConstants.NORTH_MONTH_ZHI_INDEX，FourPillarsEngine.NorthernPillars+calculateNorthern()。CalendarRepository中北半球四柱改用calculateNorthern()结果、南半球四柱由flipGanZhi北半球正确结果翻转得出。南半球原来的calculate()/flipToSouthern保留供lunarDate等非四柱字段使用。影响范围：仅改变北半球四柱的显示值，不改变任何南半球节气/月建/择日逻辑。"
            ),
            ChangelogEntry(
                version = "v1.0.5",
                date = "2026-06-02",
                content = "节气对照双卡系统上线。一、CalendarConstants新增NORTH_SOLAR_TERM_NAMES（北半球标准24节气，立春起头）。二、SolarTermCalculator重写：迭代NORTH_SOLAR_TERM_NAMES按北半球标准名查找jieQiTable获取准确日期时刻，同一索引取SOLAR_TERM_NAMES为南半球节气名，SolarTermResult新增northName+southName双命名，name属性get()=southName保持向后兼容。三、SolarTermInfo新增northName字段，CalendarRepository同步传递。四、HomeScreen阳历Tab新增DualSolarTermCard——当前节气双卡并排：暖橙北半球vs冷蓝南半球，22sp大字显示节气名，底部标注精确时间+「黄经差180°=12节气偏移」，下分界线后展示下一节气双卡(18sp+倒计时天数)，整卡绿色主题条+金色标题☯节气对照·阴阳二分。全部24节气均可一一对照验证。"
            ),
            ChangelogEntry(
                version = "v1.0.4",
                date = "2026-06-02",
                content = "南北对照四柱卡片经纬度标注。北半球卡片标注「北京时间 · 东经120° · UTC+8」，南半球卡片标注「阿根廷 · 南纬35°西经60° · UTC-3」，底部说明栏更新为「经度差180° 时差12h · 时柱+6翻转」。应用名称改为「南半球历法 · 阿根廷」。三处统一：strings.xml(app_name)、HomeScreen(标题)、MainActivity(抽屉标题)。"
            ),
            ChangelogEntry(
                version = "v1.0.3",
                date = "2026-06-02",
                content = "重磅更新：太极双鱼·南北四柱对照系统上线。一、引擎层（FourPillarsEngine）：新增flipGanZhi()天干地支翻转函数——全体(ganIndex+6)%10+(zhiIndex+6)%12，新增flipToSouthern()乾坤翻转方法，输入北半球PillarsResult，输出南半球FlippedPillars（年柱/月柱/日柱/时柱四组干支全体+6/+6翻转+生肖六冲）。数学本质：天干六合（甲己/乙庚/丙辛/丁壬/戊癸互为镜像）、地支六冲（子午/丑未/寅申/卯酉/辰戌/巳亥），太极双鱼图在干支体系中的完美投射——北半球丙午⇔壬子(丙火壬水·午火子水，水火既济⇔未济)；癸巳⇔己亥(巳亥六冲)；丁未⇔癸丑(未丑六冲)；庚戌⇔丙辰(戌辰六冲)。二、数据模型层（Models.kt）：CalendarDay新增10个字段——northYearGanZhi/northMonthGanZhi/northDayGanZhi/northHourGanZhi/northShengXiao（北半球五柱）和southYearGanZhi/southMonthGanZhi/southDayGanZhi/southHourGanZhi/southShengXiao（南半球五柱）。三、数据仓库层（CalendarRepository）：getCalendarDay()中在计算北半球PillarsResult后调用flipToSouthern()同步产出南半球数据，一步到位填充至CalendarDay。四、UI层（HomeScreen）：全新的DualPillarsCard双卡对照布局取代原有单一四柱卡片——左侧暖色(橙色底·☀️北半球·中国标准)vs右侧冷色(蓝色底·🌏南半球·太极翻转)，每侧竖列显示年月日时四柱，底部灰底通栏显示「马↔鼠 六冲生肖」「天干+6 地支+6 翻转法则」。阳历Tab和农历Tab的详情卡片均已替换为DualPillarsCard。五、去除废弃的PillarColumn组件。"
            ),
            ChangelogEntry(
                version = "v1.0.2",
                date = "2026-06-02",
                content = "完善月干支计算。南半球版monthIndex从立秋（申月=0）起算，但五虎遁起干法是固定从寅月（立春）起算的，原代码未做offset转换导致申月至丑月（南半球正月~六月）的月干全部取错。调整：calcMonthPillar中新增(monthIndex+6)%12偏移，将南半球月序映射回寅月起算的传统月序，确保月干支正确。影响范围：四柱八字的月柱天干、择日九大流派的月干评分、日程管理的农历月显示。"
            ),
            ChangelogEntry(
                version = "v1.0.1",
                date = "2026-06-02",
                content = "南半球历法正式发布。本版本为南半球专属三历法系统——阳历（公历+南半球24节气+南半球季节）、农历（太阴太阳合历，申月为正月，月柱偏移+6，乾坤扭转）、阴历（纯太阴历，12个太阴月/年，约354天，无闰月）。核心功能：①首页三标签页（阳历/农历/阴历）月视图，阳历显示南半球季节，农历显示申月起正月，阴历显示完整月相日历；②四柱八字（月柱南半球偏移适配）；③择日系统（正体五行/山家/核心禁忌/斗首/河洛/通天窍/乌兔/彭祖百忌/本命九大流派）；④日月食推演；⑤日程管理；⑥日课应验笔记（导出TXT/Word/Excel+微信/QQ分享）。技术栈：Kotlin+Jetpack Compose+Material3，lunar-java天文历法库，Meeus/ELP-2000月相算法。南半球节气偏移：立秋→处暑→白露→秋分→寒露→霜降→立冬→小雪→大雪→冬至→小寒→大寒→立春→雨水→惊蛰→春分→清明→谷雨→立夏→小满→芒种→夏至→小暑→大暑（偏移12位）。月建：正月建申。"
            )
        )
    }

    fun resetToDefault(context: Context) {
        val json = Gson().toJson(getDefaultChangelog())
        getPrefs(context).edit().putString(KEY_CHANGELOG, json).apply()
    }
}
