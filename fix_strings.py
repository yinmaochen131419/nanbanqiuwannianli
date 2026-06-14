"""
Fix all broken strings across all damaged Kotlin files.
"""
import os, re

fixes_by_file = {
    'ScheduleScreen.kt': {
        r'days < 0 -> "已过?': r'days < 0 -> "已过期"',
        r'else -> "距今 $days ?': r'else -> "距今 $days 天"',
        r'append("${event.day}?)': r'append("${event.day}日")',
        r'append("${event.year}?)': r'append("${event.year}年")',
        r'Text("${year}?); Icon': r'Text("${year}年"); Icon',
        r'Text("${y}?) }': r'Text("${y}年") }',
        r'Text("${d}?) }': r'Text("${d}日") }',
        r'label = { Text("备': r'label = { Text("备注")',
    },
    'SettingsScreen.kt': {
        r'title = "选择北半球时?': r'title = "选择北半球时区"',
        r'text = "\U0001f30f 对跖?': r'text = "\U0001f30f 对跖点信息"',
        r'text = "\U0001f30f \u5bf9\u8dd6?': r'text = "\U0001f30f \u5bf9\u8dd6\u70b9\u4fe1\u606f"',
    },
    'PureLunarCalendarScreen.kt': {
        r'MISSING_OPEN_QUOTE': 'FIXED',
    },
}

base = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen'

for fname, fixes in fixes_by_file.items():
    fp = os.path.join(base, fname)
    if not os.path.exists(fp):
        print(f'{fname}: NOT FOUND')
        continue
    
    with open(fp, 'r', encoding='utf-8') as f:
        text = f.read()
    
    fixed_count = 0
    for old, new in fixes.items():
        if old in text:
            text = text.replace(old, new)
            fixed_count += 1
    
    if fixed_count:
        with open(fp, 'w', encoding='utf-8') as f:
            f.write(text)
        print(f'{fname}: {fixed_count} fixes applied')
    else:
        print(f'{fname}: no fixes needed')
