#!/usr/bin/env python3
"""
Copy ZeRiScreen.kt from app/ module to south_build/ module,
fixing package names and imports.
"""
import re

source = r'd:\nanbanqiuwannianli\app\src\main\java\com\example\wannianli\ui\screen\ZeRiScreen.kt'
dest = r'd:\nanbanqiuwannianli\south_build\app\src\main\java\com\nanbanqiu\wannianli\ui\screen\ZeRiScreen.kt'

# Read clean source
with open(source, 'r', encoding='utf-8') as f:
    text = f.read()

# Verify clean
repl = text.count('\ufffd')
print(f'Source clean: {repl == 0} (replacement chars: {repl})')

# Fix package and imports for south_build
text = text.replace(
    'package com.example.wannianli.ui.screen',
    'package com.nanbanqiu.wannianli.ui.screen'
)
text = text.replace(
    'import com.example.wannianli.',
    'import com.nanbanqiu.wannianli.'
)

# Save
with open(dest, 'w', encoding='utf-8') as f:
    f.write(text)

print(f'Saved ZeRiScreen.kt to south_build ({len(text)} chars)')
