import json
from skyfield.api import load
from datetime import datetime, timedelta, timezone

TS = load.timescale()
PLANETS = load("de421.bsp")
EARTH = PLANETS["earth"]
SUN = PLANETS["sun"]

TERM_NAMES = [
    "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
    "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
    "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
    "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"
]

TERM_LONGITUDES = [(315 + 15 * i) % 360 for i in range(24)]


def sun_ecliptic_lon(t):
    """Return sun's apparent ecliptic longitude in degrees."""
    e = EARTH.at(t)
    s = e.observe(SUN).apparent()
    _, lon_deg, _ = s.frame_latlon(load.framelib.ecliptic_frame)
    return lon_deg.degrees % 360


def find_term_jd(year, target_lon):
    """Find skyfield Time when sun reaches target longitude."""
    # Search from January 1 to February 10 next year
    start_t = TS.utc(year, 1, 1)
    end_t = TS.utc(year + 1, 2, 10)
    start_jd = start_t.tt
    end_jd = end_t.tt

    eps = 1e-8

    for _ in range(100):
        mid_jd = (start_jd + end_jd) / 2.0
        mid_t = TS.tt_jd(mid_jd)

        lon_start = sun_ecliptic_lon(TS.tt_jd(start_jd))
        lon_mid = sun_ecliptic_lon(mid_t)
        lon_end = sun_ecliptic_lon(TS.tt_jd(end_jd))

        # Helper: check if target is in interval [a, b) mod 360
        def in_interval(a, b, x):
            a_mod = a % 360
            b_mod = b % 360
            x_mod = x % 360
            if a_mod <= b_mod:
                return a_mod <= x_mod < b_mod
            else:
                return x_mod >= a_mod or x_mod < b_mod

        if in_interval(lon_start, lon_mid, target_lon):
            end_jd = mid_jd
        else:
            start_jd = mid_jd

        if end_jd - start_jd < eps:
            break

    return TS.tt_jd((start_jd + end_jd) / 2.0)


def main():
    output = {}
    for year in range(1900, 2101):
        terms = []
        for i, name in enumerate(TERM_NAMES):
            target_lon = TERM_LONGITUDES[i]
            t = find_term_jd(year, target_lon)
            utc_dt = t.utc_datetime()

            # Convert UTC to CST (UTC+8)
            cst_tz = timezone(timedelta(hours=8))
            cst_dt = utc_dt.astimezone(cst_tz)

            terms.append([
                cst_dt.month,
                cst_dt.day,
                cst_dt.hour,
                cst_dt.minute,
                cst_dt.second
            ])

        output[str(year)] = terms
        if year % 20 == 0:
            print(f"  {year} done...")

    import os
    os.makedirs("app/src/main/assets", exist_ok=True)

    with open("app/src/main/assets/solar_terms.json", "w", encoding="utf-8") as f:
        json.dump(output, f, separators=(",", ":"), ensure_ascii=False)

    print(f"\nGenerated {len(output)} years (1900-2100)")

    t_2026 = output["2026"]
    xiaoman = t_2026[7]
    print(f"\nVerification: 2026 小满 = {xiaoman[0]}月{xiaoman[1]}日 {xiaoman[2]:02d}:{xiaoman[3]:02d}:{xiaoman[4]:02d} CST")


if __name__ == "__main__":
    main()
