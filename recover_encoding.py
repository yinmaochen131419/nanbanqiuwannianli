"""
Try to recover corrupted UTF-8 files by reading as GBK then writing as UTF-8.
This works if the corruption was: UTF-8 -> (misread as system encoding) -> Set-Content (writes GBK).
"""
import os, glob

base = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli'
files = glob.glob(base + r'\**\*.kt', recursive=True)

good_count = 0
gbk_count = 0
fail_count = 0

for fp in files:
    with open(fp, 'rb') as f:
        raw = f.read()
    
    # Try UTF-8 first
    try:
        text = raw.decode('utf-8')
        # Check if it has many replacement chars
        if text.count('\ufffd') > 3:
            # UTF-8 decode "succeeded" but many replacement chars = corruption
            raise UnicodeDecodeError('utf-8', raw, 0, 1, 'too many replacements')
        good_count += 1
        continue
    except:
        pass
    
    # Try GBK (cp936)
    try:
        text = raw.decode('gbk')
        # Check for plausible Chinese text
        if text.count('\ufffd') == 0:
            with open(fp, 'w', encoding='utf-8') as f:
                f.write(text)
            gbk_count += 1
            continue
    except:
        pass
    
    # Try to fix by removing BOM + re-encoding
    try:
        # Try reading raw bytes and reconstructing
        # The corruption pattern: multi-byte UTF-8 -> interpreted as cp1252/GBK
        text = raw.decode('utf-8', errors='replace')
        if text.count('\ufffd') < 10:
            with open(fp, 'w', encoding='utf-8') as f:
                f.write(text)
            fail_count += 1
            continue
    except:
        pass
    
    fail_count += 1

print(f'UTF-8 OK: {good_count}, GBK fixed: {gbk_count}, Failed: {fail_count}')

# Special check for ZeRiScreen
zr = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\ZeRiScreen.kt'
with open(zr, 'r', encoding='utf-8') as f:
    t = f.read()
print(f'ZeRiScreen \ufffd count: {t.count(chr(0xfffd))}')
