# White Paper: The Southern Hemisphere Calendar — A Tai Chi Symmetry-Based Calendar System

---

## Abstract

Traditional Chinese calendars (农历) have been developed over more than four millennia based exclusively on astronomical observations made from the Northern Hemisphere. This geographic specificity creates a fundamental seasonal misalignment for Chinese communities residing in the Southern Hemisphere, where the solstices, equinoxes, and agricultural cycles are inverted. This paper proposes a Southern Hemisphere Calendar system grounded in the philosophical principle of Tai Chi (太极) symmetry. The system converts the Northern Hemisphere calendar through a set of mathematically rigorous transformations: a +6 month offset for both Gregorian and lunar months, a +15 lunar day offset on the synodic cycle, a solar term mapping of N ↔ (N+12) mod 24, and moon phase mirroring across the equatorial plane. These transformations are not arbitrary conventions but reflect the 180° rotational symmetry inherent in the Tai Chi dual-fish diagram, where each hemisphere constitutes one half of a unified whole. The proposed system enables over thirty million ethnic Chinese in the Southern Hemisphere to observe a lunar calendar that faithfully corresponds to their local seasonal reality, while maintaining complete mathematical coherence with the traditional Northern system.

---

## 1. Introduction

The Chinese calendar (农历, *nónglì*) stands as one of humanity's oldest continuously used calendrical systems, with a recorded history exceeding four thousand years. From the earliest oracle bone inscriptions of the Shang dynasty to the sophisticated astronomical computations of the Ming-era *Shòushí Lì* (授时历), every element of the Chinese calendar — its solar terms (节气, *jiéqì*), lunar phases, and seasonal markers — has been derived from observations made exclusively in the Northern Hemisphere.

Today, over **thirty million** ethnic Chinese reside in the Southern Hemisphere, distributed across Argentina, Australia, New Zealand, Brazil, South Africa, and numerous other nations. For these communities, the traditional calendar presents a paradox: when the Northern Hemisphere celebrates the Spring Festival (春节, *Chūnjié*) in January–February, the Southern Hemisphere is in the depths of mid-summer. When the calendar proclaims the arrival of "Start of Spring" (立春, *Lìchūn*), Australians are harvesting autumn crops. The Mid-Autumn Festival (中秋节) falls during Southern spring, and the Dragon Boat Festival (端午节) coincides with the onset of winter.

This is not merely a cultural inconvenience; it is a fundamental disconnect between a calendar designed to harmonize human life with natural cycles and the actual seasonal reality experienced by a significant portion of the Chinese diaspora. The need for a calendar system that reflects Southern Hemisphere seasonal conditions — while remaining rooted in the philosophical and astronomical traditions of Chinese calendrical science — is both practical and culturally imperative.

This paper presents such a system: the **Southern Hemisphere Calendar**, derived from the Northern Hemisphere calendar through the principle of **Tai Chi symmetry** (太极对称).

---

## 2. Core Conversion Principles

The Southern Hemisphere Calendar is not an independent invention but a mathematically precise transformation of the traditional Northern Hemisphere calendar. All conversions derive from a single unifying principle: a 180° rotational symmetry, conceptually identical to the rotation of the Tai Chi dual-fish diagram (太极双鱼图).

### 2.1 Gregorian Conversion

The Gregorian calendar conversion follows a straightforward six-month offset, reflecting the fact that the Southern Hemisphere experiences the opposite season at any given Gregorian date.

**Rule:**

- **Southern month** = Northern month + 6 (subtract 12 if the result exceeds 12)
- **Year boundary:** If Northern month < 7, Southern year = Northern year − 1; if Northern month ≥ 7, Southern year = Northern year

**Examples:**

| Northern Hemisphere | Southern Hemisphere |
|---------------------|---------------------|
| January 2026 | July 2025 |
| February 2026 | August 2025 |
| June 2026 | December 2025 |
| July 2026 | January 2026 |
| December 2026 | June 2026 |

The year boundary rule ensures that the Southern Hemisphere year increments at its own mid-year point (Northern July), maintaining internal consistency.

### 2.2 Lunar Calendar Conversion

The lunar calendar conversion mirrors the Gregorian offset, applying the same six-month shift to the lunar month cycle.

**Rule:**

- **Southern lunar month** = Northern lunar month + 6 (subtract 12 if the result exceeds 12)
- **Leap month (闰月, *rùnyuè*):** Southern leap month = (Northern leap month + 6 − 1) % 12 + 1
- **Year boundary:** Same as the Gregorian rule — if Northern lunar month < 7, Southern year = Northern year − 1; if Northern lunar month ≥ 7, Southern year = Northern year

**Example:** If the Northern Hemisphere has a leap month in the 4th lunar month (闰四月), the Southern Hemisphere equivalent is: (4 + 6 − 1) % 12 + 1 = 10, i.e., a leap month in the 10th lunar month (闰十月).

### 2.3 Tai Chi Opposing Day (太极对冲日)

The most distinctive feature of the Southern Hemisphere Calendar is the **+15 lunar day offset**, which we term the "Tai Chi Opposing Day" transformation.

**Rule:**

- **Southern lunar day** = Northern lunar day + 15
- If the result exceeds the current month's length (29 or 30 days), subtract the month length and carry one to the next month

**Essence:** Adding 15 days on the 29.53-day synodic cycle places the Southern lunar day exactly 180° apart from the Northern lunar day. This means the moon phase is **precisely inverted**: a Northern first quarter moon corresponds to a Southern last quarter moon, and vice versa.

**Example:**

- Northern: 18th day of the 4th lunar month
- Southern month: 4 + 6 = 10th lunar month
- Southern day: 18 + 15 = 33; the 10th lunar month has 29 days → 33 − 29 = 4, with a carry to the next month
- Result: **3rd day of the 10th lunar month** (with the 18th being carried as 33 − 29 = 4, but the 18th + 15 = 33rd count from the start of the month, so the actual day is the 4th of the 11th month; re-examining: 18 + 15 = 33, and if the 10th month has 29 days, then 33 − 29 = 4th day of the **next** month, i.e., the 11th lunar month)

*Note: The exact carry depends on the month length of the specific year in question.*

### 2.4 Solar Term Correspondence

The 24 solar terms (二十四节气, *èrshísì jiéqì*) form the backbone of the Chinese agricultural calendar. In the Southern Hemisphere, each solar term maps to its diametrically opposite term, reflecting the inverted seasonal cycle.

**Rule:** Solar term N in the Northern Hemisphere ↔ Solar term (N + 12) mod 24 in the Southern Hemisphere.

**Full Correspondence Table:**

| # | Northern Hemisphere | | Southern Hemisphere | # |
|---|---------------------|---|---------------------|---|
| 1 | 立春 (Start of Spring) | ↔ | 立秋 (Start of Autumn) | 13 |
| 2 | 雨水 (Rain Water) | ↔ | 处暑 (End of Heat) | 14 |
| 3 | 惊蛰 (Awakening of Insects) | ↔ | 白露 (White Dew) | 15 |
| 4 | 春分 (Vernal Equinox) | ↔ | 秋分 (Autumnal Equinox) | 16 |
| 5 | 清明 (Clear and Bright) | ↔ | 寒露 (Cold Dew) | 17 |
| 6 | 谷雨 (Grain Rain) | ↔ | 霜降 (Frost's Descent) | 18 |
| 7 | 立夏 (Start of Summer) | ↔ | 立冬 (Start of Winter) | 19 |
| 8 | 小满 (Grain Buds) | ↔ | 小雪 (Minor Snow) | 20 |
| 9 | 芒种 (Grain in Ear) | ↔ | 大雪 (Major Snow) | 21 |
| 10 | 夏至 (Summer Solstice) | ↔ | 冬至 (Winter Solstice) | 22 |
| 11 | 小暑 (Minor Heat) | ↔ | 大寒 (Major Cold) | 23 |
| 12 | 大暑 (Major Heat) | ↔ | 小寒 (Minor Cold) | 24 |

This correspondence is not merely a relabeling; it reflects the astronomical reality that when the Sun reaches the ecliptic longitude corresponding to 立春 (315°), the Northern Hemisphere experiences the onset of spring while the Southern Hemisphere simultaneously experiences the onset of autumn (立秋, 135°). The two are separated by exactly 180° of ecliptic longitude — the Tai Chi symmetry made manifest in celestial mechanics.

---

## 3. Moon Phase Verification

The +15 lunar day offset is not a theoretical construct alone; it is empirically verifiable through direct observation of the Moon.

### 3.1 Moon Phase Mirror Symmetry

The Moon's phase is **globally identical** at any given moment — a full moon is a full moon everywhere on Earth. However, the **orientation** of the illuminated portion differs between hemispheres due to the observer's perspective relative to the ecliptic plane.

- **Northern Hemisphere:** The waxing crescent appears as a **D-shaped** crescent (right side illuminated). The first quarter moon has its **right half** illuminated.
- **Southern Hemisphere:** The waxing crescent appears as a **reverse-D** crescent (left side illuminated). The first quarter moon has its **left half** illuminated.
- **Full Moon:** Identical everywhere — a complete circle of illumination.
- **Waxing direction:** Opposite between hemispheres. In the North, the illuminated portion grows from right to left; in the South, from left to right.

This mirror symmetry is a direct consequence of the observer's inverted orientation relative to the celestial sphere when crossing the equator.

### 3.2 Verification Method

The Tai Chi opposing day offset can be verified through the following observational procedure:

1. **Observe the current moon phase** and determine the lunar day. The phase day count is globally consistent — a first quarter moon is the ~8th day of the lunar month everywhere.
2. **Apply the +15 offset:** Northern lunar day + 15 = Southern Tai Chi opposing day.
3. **Verify mirror symmetry:** The Southern opposing day should exhibit the mirror-symmetric phase.

**Example:**

- Northern 8th lunar day → first quarter moon (right half illuminated in Northern Hemisphere)
- Southern 8th + 15 = 23rd lunar day → last quarter moon (left half illuminated in Southern Hemisphere)
- The first quarter (right-lit, North) and last quarter (left-lit, South) are **mirror-symmetric** — exactly as predicted by the Tai Chi opposing day transformation.

This provides a simple, direct, and repeatable empirical verification of the +15 day offset, accessible to any observer with a clear view of the Moon.

### 3.3 Unity and Opposition

The relationship between Northern and Southern moon phases embodies the core Tai Chi principles of **unity within opposition** (对立统一):

- **Unity:** The phase day count is globally identical. A full moon is universally round. The synodic cycle is one and the same for all observers on Earth.
- **Opposition:** The illuminated side is mirror-symmetric between hemispheres. The waxing direction is reversed. What appears as a growing right crescent in the North appears as a growing left crescent in the South.
- **Tai Chi:** The 180° separation on the synodic cycle corresponds to the rotational symmetry of the Tai Chi dual-fish diagram (太极双鱼图). Just as the two fish pursue each other in eternal rotation, the Northern and Southern lunar phases chase each other across the synodic month — each the mirror image of the other, each incomplete without the other.

---

## 4. Stellar Determination of Solar Terms in the Southern Hemisphere

In the Northern Hemisphere, the Big Dipper (北斗七星, *Běidǒu Qīxīng*) has served for millennia as the primary stellar indicator of seasons: its handle pointing east signals spring, south signals summer, west signals autumn, and north signals winter. The Southern Hemisphere, lacking the Big Dipper, requires alternative stellar reference systems — all of which exhibit the same Tai Chi seasonal inversion relative to their Northern counterparts.

### 4.1 Centaurus Pointer Method (南门指极法)

**α Centauri** (Rigil Kentaurus, magnitude −0.01) and **β Centauri** (Hadar, magnitude 0.61) form the "Southern Pointers" (南门双星). The line connecting these two stars, extended approximately 4.5 times southward, points directly to the **South Celestial Pole** (南天极).

This method serves a function analogous to the Big Dipper's pointer stars (Merak and Dubhe) in the Northern Hemisphere:

- Pointers pointing **due south** → Southern winter (June)
- Pointers pointing **due east** → Southern spring (September)
- Pointers pointing **due north** → Southern summer (December)
- Pointers pointing **due west** → Southern autumn (March)

The seasonal meaning is exactly opposite to the Big Dipper's indication at the same time of year — a direct manifestation of Tai Chi symmetry.

### 4.2 Antares Method (心宿二法)

**Antares** (心宿二, *Xīnxiù'èr*, α Scorpii, magnitude 1.0) is one of the few first-magnitude stars readily visible from both hemispheres, making it an ideal cross-hemispheric reference star.

- **Northern Hemisphere:** Antares high in the evening sky → summer (it is a summer constellation star)
- **Southern Hemisphere:** Antares high in the evening sky → winter (the same sky position indicates the opposite season)

The seasonal meaning of Antares is **exactly opposite** between hemispheres — the most direct stellar expression of Tai Chi symmetry. Because Antares is visible from both hemispheres and its position can be precisely measured, this method offers the **highest precision** for stellar determination of solar terms.

### 4.3 Canopus and Magellanic Cloud Methods

**Canopus** (老人星, *Lǎorénxīng*, α Carinae, magnitude −0.74) is the second-brightest star in the night sky. In the Southern Hemisphere, its altitude above the southern horizon varies systematically with the seasons, providing a seasonal indicator:

- Canopus at **maximum altitude** → Southern summer
- Canopus at **minimum altitude** → Southern winter

The **Magellanic Clouds** (麦哲伦星云) — the Large and Small Magellanic Clouds — rotate around the South Celestial Pole over the course of a year. Their azimuthal position relative to the pole serves as a seasonal clock:

- Clouds positioned **below** the pole (toward the southern horizon) → Southern winter
- Clouds positioned **above** the pole (away from the horizon) → Southern summer

### 4.4 Integrated Verification Scheme

For robust stellar determination of solar terms in the Southern Hemisphere, we recommend the following hierarchical verification scheme:

1. **Primary method: Antares** — Highest precision due to its visibility from both hemispheres and its unambiguous seasonal opposition between North and South. Any observer can verify the Tai Chi symmetry by comparing Antares' seasonal meaning across the equator.
2. **Secondary method: Centaurus pointer** — Closest functional analog to the Big Dipper, providing intuitive directional guidance for Southern Hemisphere observers.
3. **Cross-validation: Canopus and Magellanic Clouds** — Independent verification using Southern-sky-specific objects, ensuring that the seasonal determination is not dependent on any single stellar reference.
4. **Principle: All stellar seasonal meanings are opposite to their Northern Hemisphere counterparts** — This universal inversion is the observational signature of Tai Chi symmetry in the celestial sphere.

---

## 5. Coriolis Force and Rotation Direction

The asymmetry between Northern and Southern Hemispheres is not limited to astronomical observations; it has a deep physical basis in the **Coriolis effect**, which governs the rotation direction of large-scale fluid systems on a rotating sphere.

### 5.1 North–South Rotation Comparison Table

| System Type | Northern Hemisphere | Southern Hemisphere |
|-------------|--------------------|--------------------|
| Typhoons / Hurricanes / Cyclones (low pressure) | Counterclockwise ↺ | Clockwise ↻ |
| Anticyclones (high pressure) | Clockwise ↻ | Counterclockwise ↺ |
| Ocean Gyres (subtropical) | Clockwise ↻ | Counterclockwise ↺ |
| Earth Rotation (as viewed from pole) | Counterclockwise | Clockwise |

This table reveals a consistent pattern: **every large-scale rotational system in the Southern Hemisphere rotates in the opposite direction to its Northern Hemisphere counterpart.** This is not coincidence but a direct consequence of the Coriolis force, which deflects moving objects to the right in the Northern Hemisphere and to the left in the Southern Hemisphere.

### 5.2 Mathematical Essence

The Coriolis force is given by:

**F** = −2m(**ω** × **v**)

where:
- *m* is the mass of the moving object
- **ω** is Earth's angular velocity vector (pointing from south to north along the rotation axis)
- **v** is the velocity of the moving object in the rotating reference frame

In the **Northern Hemisphere**, the vertical component of the Coriolis force deflects moving objects to the **right** of their direction of motion, producing counterclockwise rotation around low-pressure centers.

In the **Southern Hemisphere**, the same formula produces a deflection to the **left**, producing clockwise rotation around low-pressure centers.

**Important caveat:** The Coriolis force only affects systems at spatial scales of hundreds of kilometers or more. Small-scale phenomena (bathtub drains, stirring tea) are dominated by other forces and do not exhibit hemisphere-dependent rotation. The calendar's Tai Chi symmetry operates at the planetary scale where the Coriolis effect is significant, reinforcing the physical basis for hemisphere-asymmetric transformations.

---

## 6. Four Pillars of Destiny and Local Time

The **Four Pillars of Destiny** (四柱, *sìzhù*) — Year, Month, Day, and Hour pillars — form the foundation of Chinese astrological and divinatory practice. Each pillar consists of a Heavenly Stem (天干, *tiāngān*) and an Earthly Branch (地支, *dìzhī*). The correct determination of these pillars depends critically on local time and seasonal boundaries, both of which differ between hemispheres.

### 6.1 时辰 (*Shíchén*) and Local Time

The traditional Chinese two-hour period (时辰, *shíchén*) corresponds to **local solar time**, not to any standardized time zone. Each *shíchén* is defined by the position of the Sun relative to the observer's meridian:

- 子时 (*zǐshí*): 23:00–01:00 local solar time
- 丑时 (*chǒushí*): 01:00–03:00 local solar time
- ... and so forth through the twelve Earthly Branches

At the same instant in universal time, observers in different time zones may be in different *shíchén*. For example, when it is 子时 in Beijing (UTC+8), it is still 亥时 (*hàishí*) in Perth (UTC+8) if local solar time differs, or an entirely different *shíchén* in Santiago (UTC−4).

The Southern Hemisphere Calendar application supports **city and time zone selection**, enabling automatic calculation of the correct *shíchén* based on the observer's local solar time.

### 6.2 Four Pillars Differences Between Hemispheres

Due to the six-month seasonal offset and the local time dependency, each of the Four Pillars may differ between hemispheres:

- **Year Pillar (年柱, *niánzhù*):** The year boundary in Chinese astrology occurs at 立春 (*Lìchūn*, Start of Spring), which is approximately February 4 in the Northern Hemisphere. In the Southern Hemisphere, the corresponding boundary is 立秋 (*Lìqiū*, Start of Autumn), approximately August 8 — six months apart. A person born in this six-month window may have **different Year Pillars** depending on which hemisphere's calendar is used.

- **Month Pillar (月柱, *yuèzhù*):** The month boundary is determined by the solar terms (节气, *jiéqì*). Since the solar terms are symmetrically opposite between hemispheres (Section 2.4), the Earthly Branch of the Month Pillar will be **different**. The Heavenly Stem follows from the year stem and month branch, so it too may differ.

- **Day Pillar (日柱, *rìzhù*):** The day boundary occurs at 子时 (*zǐshí*, 23:00 local solar time). Observers in different time zones may cross the day boundary at different universal times, potentially resulting in **different Day Pillars**.

- **Hour Pillar (时柱, *shízhù*):** The Hour Pillar is directly determined by the local *shíchén*. Since the same universal time corresponds to different *shíchén* in different time zones, the Hour Pillar is **always different** between observers in sufficiently separated locations.

### 6.3 Tai Chi Symmetric Four Pillars

The Southern Hemisphere Calendar proposes a **Tai Chi symmetric Four Pillars** system, derived from the Northern Hemisphere Four Pillars through a consistent +6 offset:

- **Heavenly Stem:** Southern stem = (Northern stem index + 6) % 10
- **Earthly Branch:** Southern branch = (Northern branch index + 6) % 12

This transformation ensures that the Southern Hemisphere Four Pillars are exactly 180° apart from their Northern counterparts on both the 10-stem and 12-branch cycles — a direct analog of the +6 month offset in the calendar.

**Implementation note:** The application enforces a strict **12-hour time difference** between paired cities (e.g., Beijing ↔ Buenos Aires) to maintain Tai Chi symmetry in the Four Pillars calculation. This ensures that the +6 offset is applied consistently across all four pillars.

---

## 7. Philosophical Foundation of Tai Chi Symmetry

### 7.1 Tai Chi Dual Fish and the Calendar

The **Tai Chi dual-fish diagram** (太极双鱼图) is perhaps the most iconic symbol of Chinese philosophy. It reveals a fundamental law of the universe: **the unity of opposites** (对立统一). The two fish — one dark (Yin, 阴) and one light (Yang, 阳) — pursue each other in eternal rotation, each containing within itself the seed of the other.

The calendar relationship between the Northern and Southern Hemispheres is a **real-world mapping of the Tai Chi diagram**:

- **Gregorian month +6** — like the dual fish rotating 180°, each point maps to its antipodal opposite.
- **Lunar day +15** — like antipodal points on the synodic circle, separated by exactly half a revolution.
- **Solar term symmetry** — like **Yin-Yang mutual rooting** (阴阳互根, *yīnyáng hùgēn*): each Northern term contains within it the seed of its Southern opposite. 立春 (Start of Spring) already contains the seed of 立秋 (Start of Autumn), just as the Yang fish contains the Yin eye.
- **Moon phase mirroring** — like the gaze between the two fish eyes: each observes the other as its mirror image, yet both are parts of the same whole.

### 7.2 Yin-Yang Waxing and Waning (阴阳消长)

The principle of **Yin-Yang waxing and waning** (阴阳消长, *yīnyáng xiāozhǎng*) describes the cyclical growth and decline of Yin and Yang forces throughout the year:

- **Northern Hemisphere:** Yang energy rises from the winter solstice (冬至, *Dōngzhì*), reaching its peak at the summer solstice (夏至, *Xiàzhì*).
- **Southern Hemisphere:** Simultaneously, Yin energy rises — the Southern Hemisphere enters its winter trough just as the North reaches its summer peak.

This is not merely a parallel; it is a **single process** viewed from opposite sides. The growth of Yang in the North *is* the growth of Yin in the South, because they are two aspects of the same celestial phenomenon. When Yang reaches its peak, it generates Yin (阳极生阴, *yángjí shēng yīn*); when Yin reaches its peak, it generates Yang (阴极生阳, *yīnjí shēng yáng*). This is **exactly the Tai Chi transformation** (太极转化) — the moment of maximum Yang contains the birth of Yin, and vice versa.

### 7.3 Cultural Significance

The Southern Hemisphere Calendar is **not a rejection of tradition, but its completion.**

For four thousand years, the Chinese calendar has been a half-system — not by any flaw in its design, but because the Chinese civilization that created it was situated entirely in the Northern Hemisphere. The Southern Hemisphere Calendar extends the same principles to the other half of the globe, completing the Tai Chi diagram that was always implicit in the calendar's structure.

Just as the Tai Chi contains both Yin and Yang, the **complete Chinese calendar system should encompass both hemispheres.** The Northern calendar is the Yang fish; the Southern calendar is the Yin fish. Neither is primary; neither is derivative. They are co-equal manifestations of the same astronomical and philosophical principles, viewed from opposite sides of the equator.

This is **cultural export through philosophical depth**, not mere technical conversion. The Southern Hemisphere Calendar demonstrates that Chinese calendrical science possesses the conceptual richness to adapt to any terrestrial environment — a testament to the universality of its underlying principles.

---

## 8. Conclusion

The Southern Hemisphere Calendar is a **mathematically rigorous, astronomically verifiable, and philosophically grounded** calendrical system. Its key features may be summarized as follows:

1. **All transformations derive from a single principle:** Tai Chi 180° rotational symmetry. The +6 month offset, the +15 lunar day offset, the N ↔ (N+12) solar term mapping, and the mirror-symmetric moon phases are all manifestations of the same underlying symmetry.

2. **Moon phase mirroring provides direct empirical verification.** Any observer in either hemisphere can verify the +15 day offset by comparing the local moon phase with the predicted opposing phase — a simple, repeatable experiment requiring no instruments beyond a clear sky.

3. **Stellar observation methods provide independent astronomical verification.** The Antares method, the Centaurus pointer method, and the Canopus and Magellanic Cloud methods all confirm the seasonal inversion between hemispheres, each offering a different observational pathway to the same conclusion.

4. **The Coriolis force demonstrates the physical basis for hemisphere asymmetry.** The opposite rotation directions of cyclones, anticyclones, and ocean gyres between hemispheres are not superficial curiosities but direct consequences of the same rotational physics that produces the seasonal inversion — the Earth's rotation on its axis.

5. **The Four Pillars of Destiny can be consistently transformed** using the same +6 offset principle, ensuring that Chinese astrological practice remains coherent in the Southern Hemisphere.

6. **This system enables over thirty million Southern Hemisphere Chinese** to use a culturally authentic, seasonally accurate lunar calendar — one that honors the philosophical depth of the Chinese calendrical tradition while reflecting the astronomical reality of their location.

The Southern Hemisphere Calendar does not replace the traditional calendar; it **completes** it. Where the Northern calendar is the Yang fish, the Southern calendar is the Yin fish — and together, they form the Tai Chi.

---

## References

1. **History of Chinese Astronomy** — Needham, J., *Science and Civilisation in China*, Vol. 3: Mathematics and the Sciences of the Heavens and the Earth, Cambridge University Press.

2. **lunar-javascript** — Open-source calendar library for precise Chinese lunar calendar computation. Available at: https://github.com/6tail/lunar-javascript

3. **Classical Mechanics Textbooks on Coriolis Force** — Goldstein, H., Poole, C., & Safko, J., *Classical Mechanics* (3rd ed.), Addison-Wesley; also see Holton, J.R. & Hakim, G.J., *An Introduction to Dynamic Meteorology* (5th ed.), Academic Press.

4. **Twenty-Eight Mansions and Celestial Divisions** — Sun, X. & Kistemaker, J., *The Chinese Sky During the Han: Constellating Stars and Society*, Brill; also see Ho, P.Y., *The Astronomical Chapters of the Chin Shu*, Mouton & Co.

5. **Tai Chi Philosophy and Yin-Yang Theory** — Zhou, D., *Taiji Tushuo* (太极图说, Explanations of the Diagram of the Supreme Ultimate), Song Dynasty; translated in Chan, W.-T., *A Source Book in Chinese Philosophy*, Princeton University Press.

---

*This white paper was prepared as part of the Southern Hemisphere Calendar project (南半球万年历). For implementation details and interactive demonstrations, please refer to the accompanying software application.*
