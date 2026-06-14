#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix HomeScreen.kt - second pass targeting specific lines"""

path = 'south_build/app/src/main/java/com/nanbanqiu/wannianli/ui/screen/HomeScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

fixes = {}

for i, line in enumerate(lines):
    lineno = i + 1

    # Line 433: weekdays list with ? chars
    if 'listOf("一", "?"' in line:
        line = '        val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")\n'
        fixes[lineno] = line

    # Line 1109: comment truncated, code on same line
    if '值日星宿?8宿按固定顺序逐日轮值' in line:
        line = '    // 值日星宿：28宿按固定顺序逐日轮值，全球统一，不分南北半球\n'
        fixes[lineno] = line

    # Line 1115: comment truncated
    if '2025-01-01为参?index' in line:
        line = '    // 2025-01-01为参宿(index 20)，校准偏移量20\n'
        fixes[lineno] = line

    # Line 1269: "北半球节?  换算   南半球节?,
    if '北半球节?' in line and '换算' in line:
        line = '                    text = "北半球节气   换算   南半球节气",\n'
        fixes[lineno] = line

    # Line 1891: Text("☀?北半?, ... -> Text("☀️北半球", ...)
    if '"☀?北半?' in line:
        line = '                    Text("☀️北半球", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))\n'
        fixes[lineno] = line

    # Line 1905: Text("🌏 南半?, ... -> Text("🌏南半球", ...)
    if '"🌏 南半?' in line:
        line = '                    Text("🌏南半球", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))\n'
        fixes[lineno] = line

    # Line 1919: InfoRow("闰月", "?) -> InfoRow("闰月", "是")
    if 'InfoRow("闰月", "?)' in line:
        line = '                InfoRow("闰月", "是")\n'
        fixes[lineno] = line

    # Line 2017: text = "${pickerYear}?, -> text = "${pickerYear}年",
    if '"${pickerYear}?' in line:
        line = '                        text = "${pickerYear}年",\n'
        fixes[lineno] = line

    # Line 2075: text = "${m}?, -> text = "${m}月",
    if '"${m}?' in line:
        line = '                                        text = "${m}月",\n'
        fixes[lineno] = line

    lines[i] = line

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.writelines(lines)

print(f'Fixed {len(fixes)} lines')
for lineno in sorted(fixes.keys()):
    print(f'  Line {lineno}')
