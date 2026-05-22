package com.example.wannianli.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.wannianli.data.model.ChangelogEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ChangelogDataSource {
    private const val PREFS_NAME = "wannianli_changelog"
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
                version = "v1.0.37",
                date = "2026-05-22",
                content = "择日板块交互优化：顶部日期区域新增年月快捷跳转功能。点击\"2026年6月5日\"文本弹出年月选择器，可快速跳转到任意年月（默认当日），解决原来仅支持左右箭头逐日翻页的交互缺失问题。改动仅涉及ZeRiScreen.kt的DayNavigator组件，完全不触及择日/四柱/历法/算法逻辑。"
            ),
            ChangelogEntry(
                version = "v1.0.36",
                date = "2026-05-22",
                content = "修复生肖未与年柱同步的bug：原先生肖直接取公历年计算（如2026年全年属马），现改为根据立春判断effectiveYear（立春前year-1），确保1月1日至立春前一天生肖与年柱一致（如2026年正月初一前属蛇）。改动仅3行，不影响四柱干支计算。"
            ),
            ChangelogEntry(
                version = "v1.0.35",
                date = "2026-05-22",
                content = "一劳永逸修复闰月闪退：lunar-java 闰月返回负值月号（如-5=闰五月），LUNAR_MONTH_NAMES[负下标] 导致数组越界崩溃。在 LunarCalendarEngine 中 month = abs(lunar.month) 将月号归一化到1-12正数，覆盖范围从公元前4712年到公元9999年全部闰月期。同时修复 computeHolidays 中闰月期间节日识别静默失败问题。仅改1文件1行代码。"
            ),
            ChangelogEntry(
                version = "v1.0.34",
                date = "2026-05-22",
                content = "修复远期年月崩溃（真正根因）：CalendarRepository.kt computeHolidays() 中秋循环 for(d in 1..31) 改为 1..30。9月只有30天，旧版 hutool 不校验日期合法性静默通过，v1.0.32 迁移到 lunar-java 后 Solar.fromYmd(year,9,31) 触发 IllegalArgumentException 导致翻到12月时崩溃。修复仅1行，不影响四柱/择日/节气。"
            ),
            ChangelogEntry(
                version = "v1.0.33",
                date = "2026-05-22",
                content = "修复远期年月崩溃：SolarTermCalculator.kt 中节气查询的强断言 !! 替换为空安全处理 mapIndexedNotNull + 日志记录。根因是翻页到12月时下月补位格触发跨年节气计算（如2028→2029），jieQiTable 中某键缺失导致空指针崩溃。修复后缺失节气被优雅跳过并打日志，不再影响四柱/择日等下游功能。"
            ),
            ChangelogEntry(
                version = "v1.0.32",
                date = "2026-05-22",
                content = "农历库统一迁移：将hutool-core农历转换替换为lunar-java，消除双库共存导致的数据不一致隐患。LunarCalendarEngine从hutool ChineseDate切换至lunar-java Solar/Lunar，对外接口保持不变（solarToLunar返回LunarResult），四柱、择日、节气等所有下游模块零改动。移除hutool-core依赖（约1.5MB），APK体积减小。"
            ),
            ChangelogEntry(
                version = "v1.0.31",
                date = "2026-05-22",
                content = "节气算法一劳永逸：引入Gitee 6.6k+ Star开源库lunar-java（寿星天文历VSOP87理论+63项章动修正），替代原有的2020-2029硬编码查表+低精度fallback方案；节气精度从分钟级提升至秒级（误差<1秒），覆盖年份从10年扩展到-4712~9999年；SolarTermCalculator从212行精简到66行，完全离线纯数学计算，零网络依赖"
            ),
            ChangelogEntry(
                version = "v1.0.30",
                date = "2026-05-21",
                content = "工程规范化：新建.gitignore忽略规则文件，防止local.properties本地路径/用户名泄露，防止app/build/.gradle等数十MB构建产物误提交，为Gitee代码仓库上传提供前置必要条件"
            ),
            ChangelogEntry(
                version = "v1.0.29",
                date = "2026-05-21",
                content = "首页月视图优化：点击顶部年份月份文字弹出年份-月份快速选择器（年月选择器），支持1900-2100年跨度一键跳转；日课应验笔记新增搜索功能：搜索栏支持按笔记内容关键字实时搜索，含清除按钮与无结果提示"
            ),
            ChangelogEntry(
                version = "v1.0.28",
                date = "2026-05-21",
                content = "节气算法优化探索（方案C纯离线VSOP87天文算法/方案A高精度星历PyEphem批量生成），实测精度未达紫金山天文台标准；回滚至稳定硬编码节气预计算数据表（2020-2029），确保所有功能正常运行；节气数据仍保留原有预计算表作为唯一数据源"
            ),
            ChangelogEntry(
                version = "v1.0.21",
                date = "2026-05-20",
                content = "新增通天窍择日（独立标签页）：十二时辰黄道黑道神煞系统（青龙明堂天刑朱雀金匮天德白虎玉堂天牢玄武司命勾陈）；按日支起青龙顺排十二时辰；最优时辰推荐（首选+次选）；六大黄道吉时标注；坐山联动+本命相主联动预留接口"
            ),
            ChangelogEntry(
                version = "v1.0.20",
                date = "2026-05-20",
                content = "斗首择日全面升级：新增斗首与坐山匹配分析（联动正体五行坐山，五星生克制化）；十干化气古法校验（年上起元辰正宗标注）；催丁催贵文案精细化（含破军干扰/武曲生贪狼等细则）；禁忌事项升级（元辰制破军化解+天德月德）；新增本命年命联动参考"
            ),
            ChangelogEntry(
                version = "v1.0.19",
                date = "2026-05-20",
                content = "新增乌兔太阳太阴择日（杨公古法：太阳到山化煞、太阴到向催财，绑定坐山动态判定）；新增四大类神煞优先级体系（山家八煞/年家大煞/日家四大凶日/杨公忌日，按优先级分层，含四离四绝重丧正四废等全新检查）；山家煞扩展（龙虎煞血刃年家白虎小月建黄幡豹尾）"
            ),
            ChangelogEntry(
                version = "v1.0.18",
                date = "2026-05-20",
                content = "正体五行择日重大优化：四柱五行旺衰分析（干支八字符五行统计）；扶山补龙匹配（坐山五行生克判定）；山家犯大煞动态评分覆盖（岁破三煞阴府都天强制判大凶0-25分）；宜忌事项绑定坐山分场景；神煞优先级分层（山家煞＞日神煞天德月德）"
            ),
            ChangelogEntry(
                version = "v1.0.17",
                date = "2026-05-20",
                content = "斗首择日优化：新增斗首全局格局分析（年时双元辰/三元辰/武曲得令/破军受制等模式识别）；新增禁忌事项分析（破军所在柱位具体禁忌、化解方法）"
            ),
            ChangelogEntry(
                version = "v1.0.16",
                date = "2026-05-20",
                content = "十二建除深度解析（建除满平定执破危成收开闭各附详解+宜忌清单）；二十八宿深度解析（28星宿简介+宜忌清单）；新增神煞冲突深度分析（破日遇天德月德可化解等规则）"
            ),
            ChangelogEntry(
                version = "v1.0.15",
                date = "2026-05-20",
                content = "正体五行新增山家煞分析：二十四山坐山选择器；年家煞（三煞岁破大将军巡山罗睺戊己都天大月建土府）；山家煞（阴府罗天大退坐三煞方冲山坐山刑害）；天德月德虽吉犯山家大煞照样凶"
            ),
            ChangelogEntry(
                version = "v1.0.14",
                date = "2026-05-20",
                content = "修复河洛日课闪退bug：洛书卦序(含跳跃1-9)直代标准六十四卦索引(0-63)导致离卦索引64数组越界，增加LUOSHU_TO_ARRAY_INDEX映射表修复"
            ),
            ChangelogEntry(
                version = "v1.0.13",
                date = "2026-05-20",
                content = "择日板块新增河洛纳卦日课：纳甲纳支配八卦合六十四重卦；河洛数理/卦气通气/阴阳相配分析；催丁催贵稳大局评估；含六十四卦吉凶判定"
            ),
            ChangelogEntry(
                version = "v1.0.12",
                date = "2026-05-20",
                content = "择日板块新增斗首择日：天干化气五行定元辰；提取元辰廉贞贪狼武曲破军五星；分析斗首旺衰/催财催丁催贵力度；包含原理说明"
            ),
            ChangelogEntry(
                version = "v1.0.11",
                date = "2026-05-20",
                content = "择日页面重构：显示当天阳历农历四柱；十二建除宜忌映射（宜嫁娶出行开市/忌破土安葬等）；支持左右箭头切换日期+滑动手势"
            ),
            ChangelogEntry(
                version = "v1.0.10",
                date = "2026-05-20",
                content = "合并农历与四柱为同一卡片，农历横向展示（农历：四月初四）；移除生肖显示；干支年统一格式"
            ),
            ChangelogEntry(
                version = "v1.0.9",
                date = "2026-05-20",
                content = "首页底部新增导航栏（首页/择日）；新增正体五行择日功能：十二建除、三煞岁破月破、天德月德、劫煞灾煞、二十八宿、五行生克综合评分；择日页面展示原理说明与详细神煞分析"
            ),
            ChangelogEntry(
                version = "v1.0.8",
                date = "2026-05-20",
                content = "修复时柱计算：添加五鼠遁显式查表（FIVE_RAT），确保甲午日寅时正确显示丙寅；清除缓存防止旧数据残留"
            ),
            ChangelogEntry(
                version = "v1.0.7",
                date = "2026-05-20",
                content = "导出分享新增微信、QQ专用分享按钮，可直接发送文件到微信或QQ；未安装时自动提示"
            ),
            ChangelogEntry(
                version = "v1.0.6",
                date = "2026-05-20",
                content = "导出优化：全部笔记自动合并为一个文件；导出完成后显示文件路径；支持一键分享导出文件"
            ),
            ChangelogEntry(
                version = "v1.0.5",
                date = "2026-05-20",
                content = "新增日课应验笔记功能：自动填写四柱、日期时间，支持保存/删除/编辑；按年/月筛选；导出TXT/Word/Excel格式"
            ),
            ChangelogEntry(
                version = "v1.0.4",
                date = "2026-05-20",
                content = "月份视图新增国家法定节假日标注（元旦/春节/清明/劳动/端午/中秋/国庆）和二十四节气标注"
            ),
            ChangelogEntry(
                version = "v1.0.3",
                date = "2026-05-20",
                content = "新增月份视图（月历网格），支持左右切换月份、点击日期查看详情，标注农历日期"
            ),
            ChangelogEntry(
                version = "v1.0.2",
                date = "2026-05-20",
                content = "修复月柱、日柱计算错误，更换四柱算法；修正节气年份归属逻辑；日柱公式校准为甲午日基准"
            ),
            ChangelogEntry(
                version = "v1.0.1",
                date = "2026-05-19",
                content = "修复农历、节气、四柱计算错误，更正历法核心算法；引入hutool-core库确保农历转换准确性；使用预计算天文数据表确保节气精确到分钟"
            ),
            ChangelogEntry(
                version = "v1.0.0",
                date = "2026-05-19",
                content = "初始版本，实现万年历基础功能"
            )
        )
    }

    fun resetToDefault(context: Context) {
        val json = Gson().toJson(getDefaultChangelog())
        getPrefs(context).edit().putString(KEY_CHANGELOG, json).apply()
    }
}