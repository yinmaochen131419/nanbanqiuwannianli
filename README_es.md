[English](README.md) | [简体中文](README_zh.md) | [Español](README_es.md)

# ☯ Calendario del Hemisferio Sur — Nanbanqiu Wannianli

> «El Tai Chi genera las Dos Fuerzas — cuando el Norte celebra la Primavera, el Sur abraza el Otoño.»

## Visión

Los calendarios tradicionales chinos se basan enteramente en observaciones astronómicas del hemisferio norte. Cuando es el Festival de Primavera en Beijing, es pleno verano en Buenos Aires. **Nanbanqiu Wannianli** (南半球万年历) cierra esta brecha aplicando principios de simetría Tai Chi para crear un sistema calendario que refleja las estaciones, fases lunares y términos solares del hemisferio sur.

Esto no es solo una conversión técnica — es una completitud filosófica. Así como el Tai Chi contiene tanto al Yin como al Yang, el sistema calendario chino completo debería abarcar ambos hemisferios.

## Principios Fundamentales

| Transformación | Fórmula | Ejemplo |
|---------------|---------|---------|
| Mes gregoriano | +6 (mod 12) | Norte Ene → Sur Jul |
| Mes lunar | +6 (mod 12) | Norte 4to mes → Sur 10mo mes |
| Día lunar (Día Opuesto Tai Chi) | +15 | Norte 18 → Sur 3 (mes siguiente) |
| Término solar | N ↔ (N+12) mod 24 | Solsticio de Verano ↔ Solsticio de Invierno |
| Fase lunar | Espejo 180° | Creciente forma D ↔ Creciente forma D invertida |
| Cuatro Pilares (Bazi) | Tronco+6, Rama+6 | 丙午 → 壬子 |

## Características

### 📱 Aplicación Android
- 🌍 Comparación calendario dual (Norte ↔ Sur)
- 🌙 Espejo de fases lunares con inversión visual
- 📅 Correspondencia de términos solares con fechas reales
- 🔮 Cuatro Pilares del Destino (四柱八字) con simetría Tai Chi
- ⭐ Mansión Lunar Diaria (值日星宿) y Mansión de la Luna (月躔星宿)
- 🏙️ Soporte de zonas horarias multi-ciudad (21 ciudades norte + 17 ciudades sur)
- 📅 Gestión de horarios
- 🌐 4 idiomas: 简体中文, 繁體中文, English, Español (Argentina)
- 📚 Base de conocimiento calendario (9 secciones)

### 🌐 Demo Web
- 🌐 Archivo HTML único sin dependencias
- 📱 Diseño responsivo (móvil + escritorio)
- 🌐 4 idiomas soportados
- 🔄 Selector de fecha interactivo con comparación en tiempo real
- 📚 Tarjetas de conocimiento desplegables

## 📸 Capturas de pantalla

<!-- Agregar capturas aquí -->
*Próximamente*

## 📥 Descarga

- **APK Android**: Ver [Releases](../../releases)
- **Demo Web**: Abrir `web-demo/index.html` en cualquier navegador

## 📖 Libro Blanco

Libro blanco académico completo disponible en tres idiomas:

- [中文白皮书](web-demo/whitepaper_zh.md)
- [English White Paper](web-demo/whitepaper_en.md)
- [Libro Blanco en Español](web-demo/whitepaper_es.md)

## 🛠️ Stack Tecnológico

- **Android**: Kotlin, Jetpack Compose, Material 3
- **Motor Calendario**: [lunar-javascript](https://github.com/6tail/lunar-javascript) (cn.6tail:lunar para Android)
- **Demo Web**: HTML/CSS/JS nativo (sin dependencias)
- **Astronomía**: Cálculo de posición planetaria para Mansión de la Luna

## ☯ Filosofía

> El diagrama de los dos peces Tai Chi revela: la unidad de los opuestos es la ley fundamental del universo.

La relación calendaria entre hemisferios es un mapeo real del diagrama Tai Chi:

- **Mes gregoriano +6** — como los dos peces rotando 180°
- **Día lunar +15** — como puntos antipodales en el círculo sinódico
- **Simetría de términos solares** — como el enraizamiento mutuo Yin-Yang
- **Espejo de fases lunares** — como la mirada entre los ojos de los dos peces

## 📊 Estado del Proyecto

- ✅ App Android (v1.0) — Funciones principales completas
- ✅ Demo Web (Fase 1-2) — Comparación calendario + Tarjetas de conocimiento
- 📋 Demo Web Fase 3 — Funciones adicionales planificadas
- 📋 Despliegue en GitHub Pages
- 📋 Espejo en Gitee

## 📄 Licencia

Copyright © 2025-2026 Proyecto Nanbanqiu Wannianli. Todos los derechos reservados.
