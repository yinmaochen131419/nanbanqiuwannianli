"""
Fix ALL broken string terminations across all Kotlin files.
Pattern: A string literal where the last Chinese char got corrupted,
eating the closing " and the comma/paren after it.
"""
import os, re, glob

base = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli'
files = glob.glob(base + r'\**\*.kt', recursive=True)

total = 0
for fp in files:
    with open(fp, 'r', encoding='utf-8') as f:
        text = f.read()
    
    orig = text
    
    # Fix pattern: "text", ...??,  -> "text",
    # The corrupted char ate the closing quote and comma
    # We need to find lines where an unclosed string has ??)
    # and close it properly

    # Fix: append("...? )  -> append("...") (corruption ate " before ))
    text = re.sub(r'append\("([^"]*)\? \)', r'append("\1")', text)
    
    # Fix: "...? ) -> "...")  
    text = re.sub(r'"([^"]*)\? \)', r'"\1")', text)
    
    # Fix: "...? } -> "...")
    text = re.sub(r'"([^"]*)\? }', r'"\1")', text)
    
    # Fix: text = "...?,  where line ends after ??,
    text = re.sub(r'text = "([^"]*)\?,$', r'text = "\1",', text, flags=re.M)
    
    # Fix: "...?, (with space before comma)  
    text = re.sub(r'"([^"]*)\? ,', r'"\1",', text)
    
    # Fix remaining getOrElse patterns
    text = re.sub(r'\$\{event\.month\}\? \)', r'${event.month}")', text)
    
    # Fix: "some text??, -> "some text",
    text = re.sub(r'"([^"]{3,})\?\?,', r'"\1",', text)
    
    # Fix: "${...}? ) -> "${...}")
    text = re.sub(r'\$\{[^}]+\}\? \)', lambda m: m.group(0).replace('? )', '")'), text)
    
    if text != orig:
        with open(fp, 'w', encoding='utf-8') as f:
            f.write(text)
        total += 1
        print(f'Fixed: {os.path.basename(fp)}')

print(f'\nTotal files fixed: {total}')
