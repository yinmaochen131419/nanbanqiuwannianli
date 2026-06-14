#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix CalendarRepository.kt encoding corruption"""

path = 'south_build/app/src/main/java/com/nanbanqiu/wannianli/data/repository/CalendarRepository.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix truncated comments
content = content.replace('// 用北京时间计算农?', '// 用北京时间计算农历')
content = content.replace('// 农历计算用北京时?', '// 农历计算用北京时间')
content = content.replace(
    '// 四柱八字用当地原始时间计算（八字描述当地天地能量，必须用当地时辰?',
    '// 四柱八字用当地原始时间计算（八字描述当地天地能量，必须用当地时辰）'
)
content = content.replace(
    '// 南半球阳历：月份+6，日期不变，年界7??',
    '// 南半球阳历：月份+6，日期不变，年界7月'
)
content = content.replace(
    '// 北半?个月相emoji：🌑🌒🌓🌔🌕🌖🌗?',
    '// 北半球8个月相emoji'
)
content = content.replace(
    '// 南半球视觉镜像：phaseIndex ?(8 - phaseIndex) % 8',
    '// 南半球视觉镜像：phaseIndex -> (8 - phaseIndex) % 8'
)
content = content.replace(
    '// 计算太极对立日：农历?+ 15，月份就是当前南半球月份（不?6?',
    '// 计算太极对立日：农历日+15，月份就是当前南半球月份（不再+6）'
)
content = content.replace(
    '// 获取南半球当前农历月的实际天数，防止"三十"不存?',
    '// 获取南半球当前农历月的实际天数，防止"三十"不存在'
)
content = content.replace(
    '// 获取南半球指定农历月的实际天?',
    '// 获取南半球指定农历月的实际天数'
)
content = content.replace('// 计算南半球月?', '// 计算南半球月份')

# Fix getSouthernSeason - the corrupted lines have broken emoji and Chinese
# Replace the entire function
old_season = '''    private fun getSouthernSeason(month: Int): Pair<String, String> {
        return when (month) {
            9, 10, 11 -> "? to "\uD83C\uDF38"
            12, 1, 2 -> "? to "\u2600\uFE0F"
            3, 4, 5 -> "? to "\uD83C\uDF42"
            6, 7, 8 -> "? to "\u2744\uFE0F"
            else -> "" to ""
        }
    }'''
new_season = '''    private fun getSouthernSeason(month: Int): Pair<String, String> {
        return when (month) {
            9, 10, 11 -> "春" to "\uD83C\uDF38"
            12, 1, 2 -> "夏" to "\u2600\uFE0F"
            3, 4, 5 -> "秋" to "\uD83C\uDF42"
            6, 7, 8 -> "冬" to "\u2744\uFE0F"
            else -> "" to ""
        }
    }'''
content = content.replace(old_season, new_season)

# Fix southPhaseNames - corrupted strings with missing closing quotes
old_phase = 'val southPhaseNames = arrayOf("新月", "残月", "下弦?, "亏凸?, "满月", "盈凸?, "上弦?, "蛾眉?)'
new_phase = 'val southPhaseNames = arrayOf("新月", "残月", "下弦月", "亏凸月", "满月", "盈凸月", "上弦月", "蛾眉月")'
content = content.replace(old_phase, new_phase)

# Fix ?monthDisplayName -> 闰$monthDisplayName
content = content.replace('"?monthDisplayName"', '"闰$monthDisplayName"')

# Fix contains(?) -> contains("闰")
# The corrupted character is a broken Chinese char
lines = content.split('\n')
new_lines = []
for line in lines:
    if 'contains("' in line and '闰' not in line and '?' in line:
        # Replace the broken pattern
        line = line.replace('contains("?)', 'contains("闰")')
    new_lines.append(line)
content = '\n'.join(new_lines)

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)
print('Fixed CalendarRepository.kt')
