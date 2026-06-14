#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix HomeScreen.kt - targeted line-by-line repair"""

path = 'south_build/app/src/main/java/com/nanbanqiu/wannianli/ui/screen/HomeScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Build a map of line number -> fix
# Based on compiler error lines and content analysis
fixes = {}

for i, line in enumerate(lines):
    lineno = i + 1
    stripped = line.strip()

    # Fix: Icon(Icons.Default.ChevronLeft, "上个月"  -> add closing )
    if 'Icons.Default.ChevronLeft, "上个月"' in line and ')' not in line.split('"上个月"')[1]:
        line = line.replace('"上个月"', '"上个月")')
        fixes[lineno] = line

    # Fix: Icon(Icons.Default.ChevronRight, "下个月"  -> add closing )
    if 'Icons.Default.ChevronRight, "下个月"' in line and ')' not in line.split('"下个月"')[1]:
        line = line.replace('"下个月"', '"下个月")')
        fixes[lineno] = line

    # Fix: "北半球阳?{viewYear}?{viewMonth}?, -> "北半球阳历${viewYear}年${viewMonth}月",
    if '北半球阳?{viewYear}' in line:
        line = line.replace('北半球阳?{viewYear}?{viewMonth}?,', '北半球阳历${viewYear}年${viewMonth}月",')
        fixes[lineno] = line

    # Fix: "南半球阳?{southYear}?{southMonth}?, -> "南半球阳历${southYear}年${southMonth}月",
    if '南半球阳?{southYear}' in line:
        line = line.replace('南半球阳?{southYear}?{southMonth}?,', '南半球阳历${southYear}年${southMonth}月",')
        fixes[lineno] = line

    # Fix: listOf("一", "?", "?", "?", "?", "?", "?) -> proper weekdays
    if 'listOf("一", "?"' in line:
        line = line.replace(
            'listOf("一", "?", "?", "?", "?", "?", "?)',
            'listOf("一", "二", "三", "四", "五", "六", "日")'
        )
        fixes[lineno] = line

    # Fix: day == "? || day == "? -> day == "六" || day == "日"
    if 'day == "?"' in line:
        line = line.replace('day == "?" || day == "?"', 'day == "六" || day == "日"')
        fixes[lineno] = line

    # Fix: "北半球历?  换算   南半球历?, -> "北半球历法   换算   南半球历法",
    if '北半球历?' in line and '换算' in line:
        line = line.replace('北半球历?  换算   南半球历?,', '北半球历法   换算   南半球历法",')
        fixes[lineno] = line

    # Fix: "北半球节气?  换算   南半球节气?, -> "北半球节气   换算   南半球节气",
    if '北半球节气?' in line and '换算' in line:
        line = line.replace('北半球节气?  换算   南半球节气?,', '北半球节气   换算   南半球节气",')
        fixes[lineno] = line

    # Fix: arrayOf("星期一", "星期?, "星期?, ... -> proper weekday names
    if 'arrayOf("星期一", "星期?' in line:
        line = line.replace(
            'arrayOf("星期一", "星期?", "星期?", "星期?", "星期?", "星期?", "星期?")',
            'arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")'
        )
        fixes[lineno] = line

    # Fix: "南阳历?" -> "南阳历"
    if '"南阳历?"' in line:
        line = line.replace('"南阳历?"', '"南阳历"')
        fixes[lineno] = line

    # Fix: "北阳历?" -> "北阳历"
    if '"北阳历?"' in line:
        line = line.replace('"北阳历?"', '"北阳历"')
        fixes[lineno] = line

    # Fix: "南农历?" -> "南农历"
    if '"南农历?"' in line:
        line = line.replace('"南农历?"', '"南农历"')
        fixes[lineno] = line

    # Fix: "北农历?" -> "北农历"
    if '"北农历?"' in line:
        line = line.replace('"北农历?"', '"北农历"')
        fixes[lineno] = line

    # Fix: "值日星宿?" -> "值日星宿"
    if '"值日星宿?"' in line:
        line = line.replace('"值日星宿?"', '"值日星宿"')
        fixes[lineno] = line

    # Fix: "月躔星宿?" -> "月躔星宿"
    if '"月躔星宿?"' in line:
        line = line.replace('"月躔星宿?"', '"月躔星宿"')
        fixes[lineno] = line

    # Fix: ?{ -> ${ (broken string templates)
    # Pattern: "?{variable}" should be "${variable}"
    if '?{' in line and '${' not in line:
        # Replace ?{ with ${
        line = line.replace('?{', '${')
        fixes[lineno] = line
    elif '?{' in line:
        line = line.replace('?{', '${')
        fixes[lineno] = line

    # Fix: strings ending with ?, (missing closing quote + comma)
    # Pattern: "Chinese text?, -> "Chinese text",
    import re
    # Find patterns like "XX? where XX ends with Chinese char
    line = re.sub(r'"([\u4e00-\u9fff]+)\?,', r'"\1",', line)
    line = re.sub(r'"([\u4e00-\u9fff]+)\?\)', r'"\1")', line)

    # Fix: "XX? at end of line (missing closing quote)
    line = re.sub(r'"([\u4e00-\u9fff]+)\?\s*$', r'"\1"', line)

    # Fix: ?" patterns (corrupted char before closing quote)
    # This shouldn't normally appear

    lines[i] = line

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.writelines(lines)

print(f'Applied fixes to {len(fixes)} lines')
for lineno in sorted(fixes.keys())[:20]:
    print(f'  Line {lineno}: {fixes[lineno].strip()[:80]}')
