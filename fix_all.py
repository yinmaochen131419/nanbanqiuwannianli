import glob, re, os

base = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli'
files = glob.glob(base + r'\**\*.kt', recursive=True)
fixed_count = 0

replacements = {
    '\ufffd': '',  # Remove all remaining replacement chars
}

for fp in files:
    with open(fp, 'rb') as f:
        raw = f.read()
    try:
        text = raw.decode('utf-8')
    except:
        # Try gbk
        try:
            text = raw.decode('gbk')
            with open(fp, 'w', encoding='utf-8') as f:
                f.write(text)
            fixed_count += 1
            continue
        except:
            continue
    
    new_text = text
    has_fix = False
    for old, new in replacements.items():
        if old in new_text:
            new_text = new_text.replace(old, new)
            has_fix = True
    
    if has_fix:
        with open(fp, 'w', encoding='utf-8') as f:
            f.write(new_text)
        fixed_count += 1

print(f'Fixed {fixed_count} files')
