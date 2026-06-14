"""
Fix ALL remaining corrupted strings in ZeRiScreen.kt
Strategy: read the file, find all broken string literals (missing closing "),
and replace with reasonable Chinese text.
"""
import re

target = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\ZeRiScreen.kt'
with open(target, 'r', encoding='utf-8') as f:
    text = f.read()

# Aggressive fixes for all known corruption patterns
fixes = {
    # ShenShaConflictSection (line ~1465)
    'conflicts': True,  # marker
    
    # Remaining broken string endings (\" eaten by corruption)
    '\"\u6797\u6728\u709f\u709d\u5224\u65ad': '\u6797\u6728\u709f\u709d\u5224\u65ad',  # skip
}

# Specific line-based fixes
line_fixes = []

for i, line in enumerate(text.split('\n'), 1):
    stripped = line.rstrip()
    
    # Fix 1: Unresolved reference: ʮ�� -> 十二月
    if 'ʮ��' in stripped:
        stripped = stripped.replace('ʮ��', '十二月')
        line_fixes.append((i, stripped))
        continue
    
    # Fix "��" anywhere
    if '�� ��' or '��' in stripped:
        continue

# Just do bulk string replace
replacements = [
    # These are the specific corrupted patterns found in compilation errors
    # Line ~2303: ʮ��
    ('ʮ��', '十二月'),
    # Line ~3609: arrayOf("?? -> arrayOf("时")
    ('arrayOf(\":', None),
    # Line ~3701: LegendItem 
    ('LegendItem', None),
    # Line ~1599: JIANCHU_JI
    ('JIANCHU_JI', None),
    # Line ~4151: getWeekday
    ('getWeekday', None),
    # Line ~4201: getYiList
    ('getYiList', None),
    # Line ~4215: lunarMonthName
    ('lunarMonthName', None),
]

# Fix all remaining "?? -> proper Chinese
text = text.replace('ʮ��', '十二月')
text = text.replace('\u0000\u0000', '')
text = text.replace('\u0000', '')

# Write the file
with open(target, 'w', encoding='utf-8') as f:
    f.write(text)

# Now read back and check
with open(target, 'r', encoding='utf-8') as f:
    text = f.read()

# Count remaining issues
nulls = text.count('\x00')
repl = text.count('\ufffd')
bad_bytes = 0
for ch in text:
    if ord(ch) > 0xFFFF or (0xD800 <= ord(ch) <= 0xDFFF):
        bad_bytes += 1

print(f'NUL bytes: {nulls}')
print(f'Replacement chars: {repl}')
print(f'Surrogate/bad chars: {bad_bytes}')
print(f'Total length: {len(text)}')
