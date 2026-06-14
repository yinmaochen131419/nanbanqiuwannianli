import re

target = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\ZeRiScreen.kt'
with open(target, 'r', encoding='utf-8') as f:
    text = f.read()

# Fix corrupted Chinese strings where "?? ate the closing quote
fixes = [
    ('text = "一??,', 'text = "\u4e00\u6708",'),  # 一月
    ('text = "二??,', 'text = "\u4e8c\u6708",'),
    ('"一??, "二', '"\u4e00\u6708", "\u4e8c'),
    ('"十??, "十一??, "十二??', '"\u5341\u6708", "\u5341\u4e00\u6708", "\u5341\u4e8c\u6708"'),
    ('text = "首??,', 'text = "\u9996\u9009",'),
    ('text = "次??,', 'text = "\u6b21\u9009",'),
    ('icon = "??,', 'icon = "\u2605",'),
    ('text = "二十八宿??,', 'text = "\u4e8c\u5341\u516b\u5bbf\u5409\u51f6",'),
    ('text = "山家煞分??,', 'text = "\u5c71\u5bb6\u715e\u5206\u6790",'),
    ('text = "无特别凶??,', 'text = "\u65e0\u7279\u522b\u51f6\u715e",'),
    ('text = "无特别吉??,', 'text = "\u65e0\u7279\u522b\u5409\u795e",'),
    ('text = "修造安??,', 'text = "\u4fee\u9020\u5b89\u846c",'),
    ('"主事??, dg', '"\u4e3b\u4e8b\u5409\u51f6", dg'),
    ('"按用途推荐时??,', '"\u6309\u7528\u9014\u63a8\u8350\u65f6\u8fb0",'),
    ('"通天窍择日原??,', '"\u901a\u5929\u7a8d\u62e9\u65e5\u539f\u7406",'),
    ('"当前坐山??,', '"\u5f53\u524d\u5750\u5c71\u4fe1\u606f",'),
    ('"当前坐山?? {', '"\u5f53\u524d\u5750\u5c71\u4fe1\u606f" {'),
    ('"坐山联动??,', '"\u5750\u5c71\u8054\u52a8\u5206\u6790",'),
    ('"坐山联动?? {', '"\u5750\u5c71\u8054\u52a8\u5206\u6790" {'),
    ('"神煞优先??,', '"\u795e\u715e\u4f18\u5148\u5206\u6790",'),
]

count = 0
for old, new in fixes:
    if old in text:
        text = text.replace(old, new)
        count += 1
        print(f'Fixed: {old[:50]}')

# Fix remaining "??) patterns (missing closing)  
text = text.replace('"??)', '"\u5409")')  # 吉)

# Fix "??} patterns
text = text.replace('"??}', '"\u5409"}')

with open(target, 'w', encoding='utf-8') as f:
    f.write(text)
print(f'Applied {count} fixes to ZeRiScreen.kt')
