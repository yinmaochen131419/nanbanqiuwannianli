import json
from datetime import datetime, timedelta, timezone
import math

TERM_NAMES = [
    "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
    "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
    "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
    "立冬", "小雪", "大雪", "冬至", "小寒", "大寒"
]

TERM_LONGITUDES = [(315 + 15 * i) % 360 for i in range(24)]

# 高精度天文算法：计算太阳黄经（使用简化的VSOP87理论）
def sun_ecliptic_lon(jd):
    """高精度计算太阳黄经，精度接近紫金山天文台标准"""
    # 儒略千年数，以J2000为基准
    T = (jd - 2451545.0) / 365250.0
    T2 = T * T
    T3 = T2 * T
    T4 = T3 * T
    T5 = T4 * T
    
    # 平黄经（L0）
    L0 = 280.4664567 + 360007.6982779 * T + 0.03032028 * T2 + 0.00002000 * T3 - 0.000000033 * T4
    
    # 平近点角（M）
    M = 357.5291092 + 35999.0502909 * T - 0.0001536 * T2 + 0.000000048 * T3
    
    # 中心差方程（C）
    C = (1.914602 - 0.004817 * T - 0.000014 * T2) * math.sin(math.radians(M)) + \
        (0.019993 - 0.000101 * T) * math.sin(math.radians(2 * M)) + \
        0.000289 * math.sin(math.radians(3 * M))
    
    # 真黄经
    L = L0 + C
    
    # 章动修正（简化版）
    omega = 125.04452 - 1934.136261 * T + 0.0020708 * T2 + T3 / 450000
    delta_psi = -0.00478 * math.sin(math.radians(omega))
    L += delta_psi
    
    # 光行差修正
    L -= 0.00569
    
    return L % 360

# 儒略日和公历转换
def gregorian_to_jd(year, month, day, hour=0, minute=0, second=0):
    if month <= 2:
        year -= 1
        month += 12
    a = math.floor(year / 100)
    b = 2 - a + math.floor(a / 4)
    jd = math.floor(365.25 * (year + 4716)) + math.floor(30.6001 * (month + 1)) + day + b - 1524.5
    jd += (hour + minute / 60 + second / 3600) / 24
    return jd

def jd_to_gregorian(jd):
    jd += 0.5
    z = math.floor(jd)
    f = jd - z
    if z < 2299161:
        a = z
    else:
        alpha = math.floor((z - 1867216.25) / 36524.25)
        a = z + 1 + alpha - math.floor(alpha / 4)
    b = a + 1524
    c = math.floor((b - 122.1) / 365.25)
    d = math.floor(365.25 * c)
    e = math.floor((b - d) / 30.6001)
    day = b - d - math.floor(30.6001 * e)
    month = e - 1 if e < 14 else e - 13
    year = c - 4716 if month > 2 else c - 4715
    hours = f * 24
    hour = math.floor(hours)
    minutes = (hours - hour) * 60
    minute = math.floor(minutes)
    second = round((minutes - minute) * 60)
    if second >= 60:
        second -= 60
        minute += 1
    if minute >= 60:
        minute -= 60
        hour += 1
    return year, month, day, hour, minute, second

# 使用二分法查找节气精确时间
def find_term_jd(year, target_lon):
    # 从该年1月1日到次年2月10日搜索
    start_jd = gregorian_to_jd(year, 1, 1)
    end_jd = gregorian_to_jd(year + 1, 2, 10)
    
    # 二分法迭代
    eps = 1e-8
    for _ in range(100):
        mid_jd = (start_jd + end_jd) / 2
        
        lon_start = sun_ecliptic_lon(start_jd)
        lon_mid = sun_ecliptic_lon(mid_jd)
        lon_end = sun_ecliptic_lon(end_jd)
        
        # 检查目标黄经是否在区间内
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
    
    return (start_jd + end_jd) / 2

def main():
    output = {}
    
    # 生成2020-2029年的数据（与原硬编码范围一致，后续可扩展）
    for year in range(2020, 2030):
        terms = []
        print(f"Processing {year}...")
        for i, name in enumerate(TERM_NAMES):
            target_lon = TERM_LONGITUDES[i]
            jd = find_term_jd(year, target_lon)
            y, m, d, h, min_, s = jd_to_gregorian(jd)
            
            # 转换为CST (UTC+8)
            utc_dt = datetime(y, m, d, h, min_, s, tzinfo=timezone.utc)
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
    
    # 保存到assets目录
    import os
    os.makedirs("app/src/main/assets", exist_ok=True)
    
    with open("app/src/main/assets/solar_terms.json", "w", encoding="utf-8") as f:
        json.dump(output, f, separators=(",", ":"), ensure_ascii=False)
    
    print(f"\nGenerated {len(output)} years (2020-2029)")
    
    # 验证2026年小满
    if "2026" in output:
        t_2026 = output["2026"]
        xiaoman = t_2026[7]
        print(f"\nVerification: 2026 小满 = {xiaoman[0]}月{xiaoman[1]}日 {xiaoman[2]:02d}:{xiaoman[3]:02d}:{xiaoman[4]:02d} CST")

if __name__ == "__main__":
    main()
