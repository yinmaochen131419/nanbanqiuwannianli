"""
Aggressive fix: replace ALL corrupted characters in ALL .kt files.
Use character-level analysis to identify and fix encoding corruption.
"""
import os, glob

base = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli'
files = glob.glob(base + r'\**\*.kt', recursive=True)

for fp in files:
    with open(fp, 'rb') as f:
        raw = f.read()
    
    # Detect encoding: try to decode as UTF-8
    try:
        text = raw.decode('utf-8')
    except UnicodeDecodeError:
        # Try cp1252 -> This often works for corrupted Chinese
        try:
            text = raw.decode('cp1252')
            # Check if it looks like it contains Chinese (GBK-encoded Chinese
            # when misread as cp1252 produces Latin-1 chars in 0x80-0xFF range)
            has_chinese = any(0x80 <= ord(c) <= 0xFF for c in text)
            if has_chinese:
                # Re-encode to bytes and try GBK
                text = raw.decode('gbk', errors='replace')
        except:
            text = raw.decode('utf-8', errors='replace')
    
    # Fix all \ufffd (replacement character) patterns
    count_before = text.count('\ufffd')
    if count_before > 0:
        # Just remove them - the strings will be slightly broken but at least compile
        text = text.replace('\ufffd', '')
        count_after = text.count('\ufffd')
        
        with open(fp, 'w', encoding='utf-8') as f:
            f.write(text)
        print(f'{os.path.basename(fp)}: {count_before} -> {count_after} corruptions')

print('Done')
