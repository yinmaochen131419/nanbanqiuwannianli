#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Comprehensive fix for all remaining encoding issues"""

import os

def fix_double_spacing(content):
    """Remove double blank lines caused by encoding corruption"""
    lines = content.split('\n')
    result = []
    for i, line in enumerate(lines):
        # Skip blank line if previous line was also blank (or if it's a blank line after a non-blank that was followed by blank)
        if line.strip() == '' and i > 0 and lines[i-1].strip() == '':
            continue
        result.append(line)
    return '\n'.join(result)

def fix_file(path, fixes, remove_double_spacing=True):
    """Apply fixes to a file"""
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    if remove_double_spacing:
        content = fix_double_spacing(content)

    for old, new in fixes:
        content = content.replace(old, new)

    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print(f'Fixed: {os.path.basename(path)}')

base = 'south_build/app/src/main/java/com/nanbanqiu/wannianli'

# Fix ScheduleEvent.kt
fix_file(os.path.join(base, 'data/model/ScheduleEvent.kt'), [
    ('val TYPE_NAMES = listOf("鐢熸棩", "绾勾?, "浼氳", "寰呭姙")',
     'val TYPE_NAMES = listOf("生日", "纪念", "会议", "待办")'),
    ('val LUNAR_MONTH_NAMES = listOf("姝ｆ湀", "浜屾湀", "涓夋湀", "鍥涙湀", "浜旀湀", "鍏湀", "涓冩湀", "鍏湀", "涔濇湀", "鍗佹湀", "鍐湀", "鑵婃湀")',
     'val LUNAR_MONTH_NAMES = listOf("正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月")'),
])

# Fix CityInfo.kt
fix_file(os.path.join(base, 'data/model/CityInfo.kt'), [
    ('IANA鏃跺尯ID锛屽涓 "Asia/Shanghai"', 'IANA时区ID，如 "Asia/Shanghai"'),
    ('涓枃鍚嶏紝?"涓捣"', '中文名，如"上海"'),
    ('鑻辨枃鍚嶏紝?"Shanghai"', '英文名，如"Shanghai"'),
    ('瑗跨彮鐗欐枃鍚嶏紝?"Shanghai"', '西班牙文名，如"Shanghai"'),
    ('绾害锛屾鍖楃含锛岃礋鍗楃含', '纬度，正=北纬，负=南纬'),
    ('缁忓害锛屾涓滅粡锛岃礋瑗跨粡', '经度，正=东经，负=西经'),
    ('鏄惁鍖楀崐?)', '是否北半球)'),
])

# Fix CalendarRepository.kt - remove double spacing and fix remaining issues
fix_file(os.path.join(base, 'data/repository/CalendarRepository.kt'), [
    # Fix getSouthernSeason - the corrupted emoji strings
    ('9, 10, 11 -> "? to "\\uD83C\\uDF38"',
     '9, 10, 11 -> "春" to "\\uD83C\\uDF38"'),
    ('12, 1, 2 -> "? to "\\u2600\\uFE0F"',
     '12, 1, 2 -> "夏" to "\\u2600\\uFE0F"'),
    ('3, 4, 5 -> "? to "\\uD83C\\uDF42"',
     '3, 4, 5 -> "秋" to "\\uD83C\\uDF42"'),
    ('6, 7, 8 -> "? to "\\u2744\\uFE0F"',
     '6, 7, 8 -> "冬" to "\\u2744\\uFE0F"'),
    # Fix southPhaseNames
    ('val southPhaseNames = arrayOf("新月", "残月", "下弦?, "亏凸?, "满月", "盈凸?, "上弦?, "蛾眉?)',
     'val southPhaseNames = arrayOf("新月", "残月", "下弦月", "亏凸月", "满月", "盈凸月", "上弦月", "蛾眉月")'),
    # Fix ?monthDisplayName
    ('"?monthDisplayName"', '"闰$monthDisplayName"'),
])

# Fix Models.kt - remove double spacing
fix_file(os.path.join(base, 'data/model/Models.kt'), [
    ('?{month}?{day}?', '${month}月${day}日'),
    ('?{south.monthValue}?{south.dayOfMonth}?', '${south.monthValue}月${south.dayOfMonth}日'),
    ('鏍规嵁鎸囧畾鏃跺尯璁＄畻鏈湴鏃堕棿', '根据指定时区计算本地时间'),
    ('鍏煎鏃ц皟鐢細榛樿浣跨敤涓捣鈙竷瀹滆壘鏂壒埄鏂?', '兼容旧调用：默认使用上海→布宜诺斯艾利斯'),
])

# Fix SettingsScreen.kt - remove double spacing
fix_file(os.path.join(base, 'ui/screen/SettingsScreen.kt'), [])

# Fix ScheduleScreen.kt - remove double spacing
fix_file(os.path.join(base, 'ui/screen/ScheduleScreen.kt'), [])

# Fix KnowledgeScreen.kt - remove double spacing
fix_file(os.path.join(base, 'ui/screen/KnowledgeScreen.kt'), [])

# Fix HomeScreen.kt - remove double spacing
fix_file(os.path.join(base, 'ui/screen/HomeScreen.kt'), [])

# Fix AppNavigation.kt - remove double spacing
fix_file(os.path.join(base, 'ui/navigation/AppNavigation.kt'), [])

# Fix EclipseScreen.kt - remove double spacing
fix_file(os.path.join(base, 'ui/screen/EclipseScreen.kt'), [])

# Fix PureLunarCalendarEngine.kt - remove double spacing
fix_file(os.path.join(base, 'engine/PureLunarCalendarEngine.kt'), [])

# Fix LunarCalendarEngine.kt - remove double spacing
fix_file(os.path.join(base, 'engine/LunarCalendarEngine.kt'), [])

# Fix SolarEclipseEngine.kt - remove double spacing
fix_file(os.path.join(base, 'engine/SolarEclipseEngine.kt'), [])

# Fix ZeRiEngine.kt - remove double spacing
fix_file(os.path.join(base, 'engine/ZeRiEngine.kt'), [])

print('\nAll files processed!')
