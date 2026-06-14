#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix HomeScreen.kt encoding corruption - systematic string repair"""

import re

path = 'south_build/app/src/main/java/com/nanbanqiu/wannianli/ui/screen/HomeScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix specific corrupted strings based on context

# Line 311: "上个?) -> "上个月"
content = content.replace('"上个?)', '"上个月"')

# Line 319/333: "下个?) -> "下个月"
content = content.replace('"下个?)', '"下个月"')

# Line 433: weekdays list
content = content.replace(
    'listOf("一", "?", "?", "?", "?", "?", "?)',
    'listOf("一", "二", "三", "四", "五", "六", "日")'
)

# Line 437: weekend check
content = content.replace('day == "? || day == "?', 'day == "六" || day == "日"')

# Line 779: calendar title
content = content.replace(
    '"北半球历?  换算   南半球历?,',
    '"北半球历法   换算   南半球历法",'
)

# Line 1175: weekday names array
content = content.replace(
    'arrayOf("星期一", "星期?, "星期?, "星期?, "星期?, "星期?, "星期?)',
    'arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")'
)

# Fix more patterns - search for common broken patterns

# Pattern: "XX?" at end of string (missing char + closing quote)
# Common Chinese calendar terms that might be broken

# "北半球阳历?" -> "北半球阳历"
content = content.replace('"北半球阳历?,', '"北半球阳历",')
content = content.replace('"南半球阳历?,', '"南半球阳历",')

# "北半球节气?" -> "北半球节气"
content = content.replace('"北半球节气?,', '"北半球节气",')
content = content.replace('"南半球节气?,', '"南半球节气",')

# "南阳历?" -> "南阳历"
content = content.replace('"南阳历?",', '"南阳历",')
content = content.replace('"北阳历?",', '"北阳历",')

# "南农历?" -> "南农历"
content = content.replace('"南农历?",', '"南农历",')
content = content.replace('"北农历?",', '"北农历",')

# Fix any remaining ? followed by missing quote patterns
# Pattern: Chinese text ending with ? and no closing quote
# This is tricky - we need to be careful not to break valid code

# Fix "换算?" -> "换算"
content = content.replace('"换算?,', '"换算",')

# Fix common month/day names
for i in range(1, 13):
    month_names = ['正', '二', '三', '四', '五', '六', '七', '八', '九', '十', '冬', '腊']
    # Fix broken month references

# Fix "初一" through "三十" patterns
# These might appear as "初?" with missing closing quote

# Fix specific patterns found in the file
# "值日星宿?" -> "值日星宿"
content = content.replace('"值日星宿?,', '"值日星宿",')
content = content.replace('"月躔星宿?,', '"月躔星宿",')

# Fix 四象 annotations
content = content.replace('"(玄武?)', '"(玄武)")')
content = content.replace('"(朱雀?)', '"(朱雀)")')
content = content.replace('"(青龙?)', '"(青龙)")')
content = content.replace('"(白虎?)', '"(白虎)")')

# Fix more general patterns - any string ending with ? instead of proper char + "
# We need to find lines with "X? patterns

# Let's do a more thorough pass - find all lines with potential broken strings
lines = content.split('\n')
fixed_lines = []
for line in lines:
    original = line

    # Fix pattern: "some Chinese text? followed by , or ) without closing quote
    # Pattern: "...? followed by , or ) or whitespace
    # This is the most common corruption: last Chinese char lost + closing quote lost

    # Fix "北正月?" through "北十二月?" and "南正月?" through "南十二月?"
    for prefix in ['北', '南']:
        for m in ['正月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']:
            broken = f'"{prefix}{m[:-1]}?'
            fixed = f'"{prefix}{m}"'
            if broken in line and fixed not in line:
                line = line.replace(broken, fixed)

    if line != original:
        fixed_lines.append((original.strip(), line.strip()))

    # Fix "闰?" -> "闰X" patterns
    # "闰?" without closing quote usually means "闰月" or similar

content_new = '\n'.join(lines)

# Second pass: fix remaining ? patterns more aggressively
# Find all instances of "X? where X is Chinese character and ? is corruption marker
# Replace with educated guesses based on context

# Fix specific known broken strings
replacements = [
    # Calendar display strings
    ('"北半球历法   换算   南半球历法"', '"北半球历法   换算   南半球历法"'),  # already fixed
    ('"北半球节气   换算   南半球节气"', '"北半球节气   换算   南半球节气"'),  # if exists

    # Moon phase
    ('"月相?"', '"月相"'),

    # Season names
    ('"春?"', '"春"'),
    ('"夏?"', '"夏"'),
    ('"秋?"', '"秋"'),
    ('"冬?"', '"冬"'),

    # Common UI strings
    ('"选择?"', '"选择"'),
    ('"设置?"', '"设置"'),
    ('"关于?"', '"关于"'),

    # Solar term names that might be broken
    ('"立春?"', '"立春"'),
    ('"立夏?"', '"立夏"'),
    ('"立秋?"', '"立秋"'),
    ('"立冬?"', '"立冬"'),
    ('"春分?"', '"春分"'),
    ('"秋分?"', '"秋分"'),
    ('"夏至?"', '"夏至"'),
    ('"冬至?"', '"冬至"'),
]

for old, new in replacements:
    content_new = content_new.replace(old, new)

# Third pass: fix remaining broken string patterns using regex
# Pattern: "Chinese text? followed by , or ) - missing closing quote
# This catches patterns like "历法?, -> "历法",
import re

# Fix strings ending with ? before comma or closing paren (missing closing quote)
# Be very careful with this - only fix obvious cases
def fix_broken_string(match):
    prefix = match.group(1)
    suffix = match.group(2)
    # Remove the trailing ? and add closing quote
    return f'"{prefix}"{suffix}'

# Only apply to strings that clearly have a ? at the end before punctuation
# content_new = re.sub(r'"([^"]*[\u4e00-\u9fff])\?([,)])', fix_broken_string, content_new)

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content_new)

print('Fixed HomeScreen.kt')
print(f'Fixed {len(fixed_lines)} lines with specific replacements')
