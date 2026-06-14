#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix Models.kt - solar term date format and south hemisphere conversion"""

path = 'south_build/app/src/main/java/com/nanbanqiu/wannianli/data/model/Models.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix fullDateTime format: add 年 and 公历
content = content.replace(
    'get() = "${year}${month}',
    'get() = "${year}年公历${month}'
)

# Fix getLocalDateTime return format: add 年 and 公历, and convert to south hemisphere date
old_return = 'return "${south.year}${south.monthValue}月${south.dayOfMonth}日'
new_return = 'return "${southYear}年公历${southMonth}月${southDay}日'
content = content.replace(old_return, new_return)

# Add south hemisphere month+6 conversion before the return statement
# Find the .toLocalDateTime() line and add conversion logic after it
old_toLocal = """            .toLocalDateTime()

        return "${southYear}"""
new_toLocal = """            .toLocalDateTime()
        // 南半球阳历：月份+6，超过12则减12，年份相应调整
        var southYear = south.year
        var southMonth = south.monthValue + 6
        if (southMonth > 12) {
            southMonth -= 12
            southYear += 1
        }
        val southDay = south.dayOfMonth
        return "${southYear}"""
content = content.replace(old_toLocal, new_toLocal)

# Fix garbled comments
content = content.replace('鏍规嵁鎸囧畾鏃跺尯璁＄畻鏈湴鏃堕棿', '根据指定时区计算本地时间')
content = content.replace('鍏煎鏃ц皟鐢細榛樿浣跨敤涓捣鈙竷瀹滆壘鏂壒埄鏂?', '兼容旧调用：默认使用上海→布宜诺斯艾利斯')

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)
print('Fixed Models.kt')
