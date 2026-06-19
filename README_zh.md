[English](README.md) | [简体中文](README_zh.md) | [Español](README_es.md)

# ☯ 南半球万年历

[在线预览效果](https://yinmaochen131419.github.io/nanbanqiuwannianli/)
> 「太极生两仪——北半球庆春之际，南半球正逢金秋。」

## 项目愿景

传统中国历法完全基于北半球天文观测制定。北京过春节时，布宜诺斯艾利斯正值盛夏。**南半球万年历**基于太极对称原理，将北半球历法系统转换为南半球对应体系，使南半球用户也能使用符合当地季节的农历系统。

这不仅是技术转换，更是哲学的补全。正如太极包含阴阳，完整的中国历法体系应当涵盖南北两个半球。

## 核心换算原理

| 换算项目 | 公式 | 示例 |
|---------|------|------|
| 阳历月份 | +6（mod 12） | 北1月 → 南7月 |
| 农历月份 | +6（mod 12） | 北四月 → 南十月 |
| 农历日（太极对立日） | +15 | 北十八 → 南初三（次月） |
| 节气 | N ↔ (N+12) mod 24 | 夏至 ↔ 冬至 |
| 月相 | 镜像翻转180° | D形蛾眉月 ↔ 反D形蛾眉月 |
| 四柱八字 | 天干+6，地支+6 | 丙午 → 壬子 |

## 功能特性

### Android 应用
- 🌍 南北半球历法对照
- 🌙 月相镜像翻转
- 📅 节气对应关系及实际日期
- 🔮 四柱八字太极对称
- ⭐ 值日星宿与月躔星宿
- 🏙️ 多城市时区支持（北半球21城 + 南半球17城）
- 📅 日程管理
- 🌐 4种语言：简体中文、繁體中文、English、Español (Argentina)
- 📚 历法知识库（9个章节）

### Web Demo
- 🌐 零依赖单HTML文件
- 📱 响应式设计（手机 + 桌面）
- 🌐 4种语言支持
- 🔄 交互式日期选择器，实时对照
- 📚 可折叠知识卡片

## 截图

<!-- 截图待添加 -->
*即将上线*

## 下载

- Android APK：见 [Releases](../../releases)
- Web Demo：浏览器打开 `web-demo/index.html`

## 白皮书

完整学术白皮书，三种语言版本：
- [中文白皮书](web-demo/whitepaper_zh.md)
- [English White Paper](web-demo/whitepaper_en.md)
- [Libro Blanco en Español](web-demo/whitepaper_es.md)

## 技术栈

- **Android**：Kotlin、Jetpack Compose、Material 3
- **历法引擎**：[lunar-javascript](https://github.com/6tail/lunar-javascript)（Android端使用 cn.6tail:lunar）
- **Web Demo**：原生 HTML/CSS/JS（零依赖）
- **天文计算**：行星位置计算（月躔星宿）

## 哲学基础

> 太极双鱼图揭示：对立统一是宇宙的基本法则。

南北半球的历法关系，正是太极双鱼图的现实映射：
- **阳历月份+6** —— 如双鱼旋转180°
- **农历日+15** —— 如朔望圆上对径点
- **节气对称** —— 如阴阳消长互根
- **月相镜像** —— 如双鱼眼的对望

## 项目进展

- ✅ Android App（v1.0）—— 核心功能完成
- ✅ Web Demo（第一阶段+第二阶段）—— 历法对照 + 知识卡片
- 📋 Web Demo 第三阶段 —— 更多功能规划中
- 📋 GitHub Pages 部署
- 📋 Gitee 镜像

## 许可证

Copyright © 2025-2026 南半球万年历项目组. All rights reserved.
