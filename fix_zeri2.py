import re
target = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\ZeRiScreen.kt'
with open(target, 'r', encoding='utf-8') as f:
    text = f.read()
remaps = [
    ('arrayOf(\"??', 'arrayOf(\"时\"'),
    ('\"????', '\"十二\"'),
    ('remaps', 'done'),
]
for old, new in remaps:
    text = text.replace(old, new)
with open(target, 'w', encoding='utf-8') as f:
    f.write(text)
print('ok')
