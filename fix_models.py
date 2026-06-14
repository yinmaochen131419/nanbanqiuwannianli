#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix all model files encoding corruption"""

# Fix ScheduleEvent.kt
path = 'south_build/app/src/main/java/com/nanbanqiu/wannianli/data/model/ScheduleEvent.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix TYPE_NAMES - garbled GBK Chinese
content = content.replace(
    'val TYPE_NAMES = listOf("鐢熸棩", "绾勾?, "浼氳", "寰呭姙")',
    'val TYPE_NAMES = listOf("生日", "纪念", "会议", "待办")'
)

# Fix LUNAR_MONTH_NAMES - garbled GBK Chinese
content = content.replace(
    'val LUNAR_MONTH_NAMES = listOf("姝ｆ湀", "浜屾湀", "涓夋湀", "鍥涙湀", "浜旀湀", "鍏湀", "涓冩湀", "鍏湀", "涔濇湀", "鍗佹湀", "鍐湀", "鑵婃湀")',
    'val LUNAR_MONTH_NAMES = listOf("正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月")'
)

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)
print('Fixed ScheduleEvent.kt')

# Fix CityInfo.kt - just fix the comments
path = 'south_build/app/src/main/java/com/nanbanqiu/wannianli/data/model/CityInfo.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace garbled comments with proper Chinese
content = content.replace('IANA鏃跺尯ID锛屽涓 "Asia/Shanghai"', 'IANA时区ID，如 "Asia/Shanghai"')
content = content.replace('涓枃鍚嶏紝?"涓捣"', '中文名，如"上海"')
content = content.replace('鑻辨枃鍚嶏紝?"Shanghai"', '英文名，如"Shanghai"')
content = content.replace('瑗跨彮鐗欐枃鍚嶏紝?"Shanghai"', '西班牙文名，如"Shanghai"')
content = content.replace('绾害锛屾鍖楃含锛岃礋鍗楃含', '纬度，正=北纬，负=南纬')
content = content.replace('缁忓害锛屾涓滅粡锛岃礋瑗跨粡', '经度，正=东经，负=西经')
content = content.replace('UTC鍋忕Щ鍒嗛挓鏁帮紝?+480(UTC+8), -180(UTC-3)', 'UTC偏移分钟数，如+480(UTC+8), -180(UTC-3)')
content = content.replace('鏄惁鍖楀崐?)', '是否北半球)')

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)
print('Fixed CityInfo.kt')

# Fix Models.kt
path = 'south_build/app/src/main/java/com/nanbanqiu/wannianli/data/model/Models.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix broken string templates - ?{ should be ${
content = content.replace('?{month}?{day}?', '${month}月${day}日')
content = content.replace('?{south.monthValue}?{south.dayOfMonth}?', '${south.monthValue}月${south.dayOfMonth}日')

# Fix garbled comments
content = content.replace('鏍规嵁鎸囧畾鏃跺尯璁＄畻鏈湴鏃堕棿', '根据指定时区计算本地时间')
content = content.replace('鍏煎鏃ц皟鐢細榛樿浣跨敤涓捣鈙竷瀹滆壘鏂壒埄鏂?', '兼容旧调用：默认使用上海→布宜诺斯艾利斯')

with open(path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)
print('Fixed Models.kt')
