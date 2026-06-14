[English](README.md) | [简体中文](README_zh.md) | [Español](README_es.md)

# ☯ Nanbanqiu Wannianli — Southern Hemisphere Calendar

> "Tai Chi generates Two Forces — when the North celebrates Spring, the South embraces Autumn."

## Vision

Traditional Chinese calendars are based entirely on Northern Hemisphere astronomical observations. When it's Spring Festival in Beijing, it's mid-summer in Buenos Aires. **Nanbanqiu Wannianli** (南半球万年历) bridges this gap by applying Tai Chi symmetry principles to create a calendar system that reflects Southern Hemisphere seasons, moon phases, and solar terms.

This is not just a technical conversion — it's a philosophical completion. Just as Tai Chi contains both Yin and Yang, the complete Chinese calendar system should encompass both hemispheres.

## Core Principles

| Transformation | Formula | Example |
|---------------|---------|---------|
| Gregorian Month | +6 (mod 12) | North Jan → South Jul |
| Lunar Month | +6 (mod 12) | North 4th Month → South 10th Month |
| Lunar Day (Tai Chi Opposing Day) | +15 | North 18th → South 3rd (next month) |
| Solar Term | N ↔ (N+12) mod 24 | Summer Solstice ↔ Winter Solstice |
| Moon Phase | Mirror 180° | D-shape crescent ↔ Reverse-D crescent |
| Four Pillars (Bazi) | Stem+6, Branch+6 | 丙午 → 壬子 |

## Features

### Android App

- 🌍 Dual-hemisphere calendar comparison (North ↔ South)
- 🌙 Moon phase mirroring with visual inversion
- 📅 Solar term correspondence with actual dates
- 🔮 Four Pillars of Destiny (四柱八字) with Tai Chi symmetry
- ⭐ Daily Lunar Mansion (值日星宿) and Moon Mansion (月躔星宿)
- 🏙️ Multi-city time zone support (21 Northern + 17 Southern cities)
- 📅 Schedule management
- 🌐 4 languages: 简体中文, 繁體中文, English, Español (Argentina)
- 📚 Calendar knowledge base (9 sections)

### Web Demo

- 🌐 Zero-dependency single HTML file
- 📱 Responsive design (mobile + desktop)
- 🌐 4 languages supported
- 🔄 Interactive date picker with real-time comparison
- 📚 Collapsible knowledge cards

## Screenshots

<!-- Add screenshots here when available -->
*Coming soon*

## Download

- **Android APK**: See [Releases](../../releases)
- **Web Demo**: Open `web-demo/index.html` in any browser

## White Paper

Full academic white paper available in three languages:

- [中文白皮书](web-demo/whitepaper_zh.md)
- [English White Paper](web-demo/whitepaper_en.md)
- [Libro Blanco en Español](web-demo/whitepaper_es.md)

## Tech Stack

- **Android**: Kotlin, Jetpack Compose, Material 3
- **Calendar Engine**: [lunar-javascript](https://github.com/6tail/lunar-javascript) (cn.6tail:lunar for Android)
- **Web Demo**: Vanilla HTML/CSS/JS (zero dependencies)
- **Astronomy**: Planet position calculation for Moon Mansion

## Philosophy

> The Tai Chi dual-fish diagram reveals: unity of opposites is the fundamental law of the universe.

The calendar relationship between hemispheres is a real-world mapping of the Tai Chi diagram:

- **Gregorian month +6** — like dual fish rotating 180°
- **Lunar day +15** — like antipodal points on the synodic circle
- **Solar term symmetry** — like Yin-Yang mutual rooting
- **Moon phase mirroring** — like the gaze between the two fish eyes

## Project Status

- ✅ Android App (v1.0) — Core features complete
- ✅ Web Demo (Phase 1-2) — Calendar comparison + Knowledge cards
- 📋 Web Demo Phase 3 — Additional features planned
- 📋 GitHub Pages deployment
- 📋 Gitee mirror

## License

Copyright © 2025-2026 Nanbanqiu Wannianli Project. All rights reserved.
