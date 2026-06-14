"""
Brute-force encoding recovery: Read each file as raw bytes, try all common
Asian encodings, pick the one with most valid Chinese characters.
"""
import os, glob

base = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli'
files = glob.glob(base + r'\**\*.kt', recursive=True)

# Chinese character ranges
def chinese_chars(text):
    count = 0
    bad = 0
    for ch in text:
        cp = ord(ch)
        if 0x4E00 <= cp <= 0x9FFF or 0x3400 <= cp <= 0x4DBF:
            count += 1
        if cp == 0xFFFD or cp < 0x20 and cp not in (0x09, 0x0A, 0x0D):
            bad += 1
    return count, bad

encodings = ['utf-8', 'gbk', 'gb2312', 'gb18030', 'big5', 'shift_jis']

for fp in files:
    with open(fp, 'rb') as f:
        raw = f.read()
    
    best_text = None
    best_score = -1
    best_enc = None
    
    for enc in encodings:
        try:
            text = raw.decode(enc)
            cn_chars, bad_chars = chinese_chars(text)
            score = cn_chars - bad_chars * 10
            if score > best_score:
                best_score = score
                best_text = text
                best_enc = enc
        except:
            pass
    
    if best_enc and best_enc != 'utf-8':
        with open(fp, 'w', encoding='utf-8') as f:
            f.write(best_text)
        print(f'{os.path.basename(fp)}: {best_enc} -> utf-8 ({best_score} score)')
    elif best_enc == 'utf-8':
        cn, bad = chinese_chars(best_text)
        if bad > 0:
            print(f'{os.path.basename(fp)}: utf-8 but {bad} bad chars')

print('Done')
