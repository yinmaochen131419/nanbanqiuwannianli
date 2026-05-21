import json
import ephem
from datetime import datetime, timedelta, timezone

TERM_NAMES = [
    "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
    "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
    "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
    "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"
]

CST = timezone(timedelta(hours=8))
PI = 3.141592653589793

def sun_ecliptic_lon(pydate):
    sun = ephem.Sun()
    observer = ephem.Observer()
    observer.date = pydate
    sun.compute(observer)
    ecl = ephem.Ecliptic(sun)
    return float(ecl.lon) * 180.0 / PI

def angle_crosses(a, b, target):
    a = a % 360.0
    b = b % 360.0
    target = target % 360.0
    if a <= b:
        return a <= target < b
    else:
        return a <= target or target < b

def find_term_utc_dt(year, target_angle):
    d0 = datetime(year, 1, 1, tzinfo=timezone.utc)
    py0 = ephem.Date(d0)

    for day_off in range(370):
        py_a = ephem.Date(py0 + day_off)
        py_b = ephem.Date(py0 + day_off + 1)
        lon_a = sun_ecliptic_lon(py_a)
        lon_b = sun_ecliptic_lon(py_b)

        if angle_crosses(lon_a, lon_b, target_angle):
            lo = float(py_a)
            hi = float(py_b)
            for _ in range(45):
                mid_val = (lo + hi) / 2.0
                mid_py = ephem.Date(mid_val)
                lon_mid = sun_ecliptic_lon(mid_py)
                if angle_crosses(lon_a, lon_mid, target_angle):
                    hi = mid_val
                else:
                    lo = mid_val
            result = (lo + hi) / 2.0
            utc_naive = ephem.Date(result).datetime()
            utc_dt = utc_naive.replace(tzinfo=timezone.utc)
            return utc_dt

    return None

def main():
    output = {}
    for year in range(1900, 2101):
        terms = []
        for i, name in enumerate(TERM_NAMES):
            angle = (315 + i * 15) % 360
            utc_dt = find_term_utc_dt(year, angle)
            if utc_dt is None:
                print(f"ERROR: {year} {name} not found!")
                continue
            cst_dt = utc_dt.astimezone(CST)
            terms.append([cst_dt.month, cst_dt.day, cst_dt.hour, cst_dt.minute, cst_dt.second])
        output[str(year)] = terms
        if year % 20 == 0:
            print(f"  {year} done...")

    import os
    os.makedirs("app/src/main/assets", exist_ok=True)
    with open("app/src/main/assets/solar_terms.json", "w", encoding="utf-8") as f:
        json.dump(output, f, separators=(',', ':'), ensure_ascii=False)

    print(f"\nGenerated {len(output)} years -> assets/solar_terms.json")

    t2026 = output["2026"]
    xm = t2026[7]
    print(f"\n2026 小满 = {xm[0]}月{xm[1]}日 {xm[2]:02d}:{xm[3]:02d}:{xm[4]:02d} CST")
    print(f"Expected: 5月21日 08:36:28 CST (Purple Mountain Observatory)")

    t2026_liqiu = t2026[12]
    print(f"2026 立秋 = {t2026_liqiu[0]}月{t2026_liqiu[1]}日 {t2026_liqiu[2]:02d}:{t2026_liqiu[3]:02d}:{t2026_liqiu[4]:02d} CST")

if __name__ == "__main__":
    main()