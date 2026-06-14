"""
Fix remaining structural damage in broken Kotlin files.
Focus on: emoji surrogate pairs, unclosed strings.
"""
import re, os

files = [
    r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\ScheduleScreen.kt',
    r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\PureLunarCalendarScreen.kt',
    r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\SettingsScreen.kt',
    r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\ZeRiScreen.kt',
]

for fp in files:
    print(f'\n=== {os.path.basename(fp)} ===')
    with open(fp, 'r', encoding='utf-8') as f:
        text = f.read()
    
    # Find all lines with unmatched quotes
    lines = text.split('\n')
    for i, line in enumerate(lines, 1):
        # Count quotes
        count = line.count('"')
        if count % 2 != 0 and 'import' not in line and '//' not in line[:line.find('"')]:
            # Check if it's not a multiline string
            stripped = line.strip()
            if stripped and not stripped.startswith('*') and not stripped.startswith('/'):
                print(f'  Line {i}: unpaired quotes ({count}): {stripped[:80]}')
    
    # Find surrogate pairs (broken emoji)
    for match in re.finditer(r'\\u[dD][89aAbBcCdDeEfF][0-9a-fA-F]{2}', text):
        pos = match.start()
        line_num = text[:pos].count('\n') + 1
        print(f'  Line {line_num}: surrogate emoji: {match.group()}...')
    
    # Count remaining bad chars
    bad = 0
    for ch in text:
        if ord(ch) > 0xFFFF or (0xD800 <= ord(ch) <= 0xDFFF):
            bad += 1
    print(f'  Surrogate chars: {bad}')
    print(f'  Zero bytes: {text.count(chr(0))}')
