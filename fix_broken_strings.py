"""
Fix all encoding-corrupted Kotlin files in south_build.
Strategy: For each file, find lines where a string literal is broken
(missing closing quote due to encoding corruption) and repair them.
"""
import os, re, glob

base = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli'
files = glob.glob(base + r'\**\*.kt', recursive=True)

total_fixes = 0

for fp in files:
    with open(fp, 'r', encoding='utf-8') as f:
        orig = f.read()
    
    text = orig
    
    # Fix 1: Find "text = "...??, patterns (missing closing quote eats the comma)
    # Pattern: start with "text = "... or similar, ends with ??, (without closing ")
    text = re.sub(r'= "([^"]*)\?\?', r'= "\1"/*FIXED*/', text)
    
    # Fix 2: More aggressive - find any line that has an unclosed string 
    # with ?? right before a comma or end of line
    text = re.sub(r'"([^"]*)\?\?,', r'"\1",', text)
    text = re.sub(r'"([^"]*)\?\?\)', r'"\1")', text)
    
    if text != orig:
        with open(fp, 'w', encoding='utf-8') as f:
            f.write(text)
        total_fixes += 1
        print(f'Fixed: {os.path.basename(fp)}')

print(f'\nTotal files fixed: {total_fixes}')
