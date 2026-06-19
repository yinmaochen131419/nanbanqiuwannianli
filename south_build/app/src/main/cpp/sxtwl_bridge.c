#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "sxtwl_cpp/c/sxtwl_c.h"

/* ── Ganzhi / JieQi name tables ──────────────────────────────────────────── */

static const char *TIAN_GAN[] = {
    "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
};

static const char *DI_ZHI[] = {
    "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
};

static const char *JIE_QI[] = {
    "冬至", "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
    "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露",
    "秋分", "寒露", "霜降", "立冬", "小雪", "大雪"
};

/* ── Helper: build a "甲子"-style Ganzhi string via JNI ──────────────────── */

static jstring makeGZString(JNIEnv *env, int tg, int dz)
{
    if (tg < 0 || tg > 9 || dz < 0 || dz > 11) {
        return (*env)->NewStringUTF(env, "");
    }
    char buf[8];
    /* TIAN_GAN[tg] and DI_ZHI[dz] are both 3-byte UTF-8 */
    snprintf(buf, sizeof(buf), "%s%s", TIAN_GAN[tg], DI_ZHI[dz]);
    return (*env)->NewStringUTF(env, buf);
}

/* ── 1. solarToLunar ─────────────────────────────────────────────────────── */

JNIEXPORT jintArray JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeSolarToLunar(
    JNIEnv *env, jclass clazz, jint year, jint month, jint day)
{
    sxtwl_Day *d = sxtwl_newDay();
    sxtwl_fromSolar(d, (int)year, (uint8_t)month, (int)day);

    int lunarYear  = sxtwl_getLunarYear(d, true);
    int lunarMonth = sxtwl_getLunarMonth(d);   /* negative = leap */
    int lunarDay   = sxtwl_getLunarDay(d);
    int isLeap     = sxtwl_isLunarLeap(d) ? 1 : 0;

    sxtwl_freeDay(d);

    jint result[4] = { (jint)lunarYear, (jint)lunarMonth, (jint)lunarDay, (jint)isLeap };
    jintArray arr = (*env)->NewIntArray(env, 4);
    (*env)->SetIntArrayRegion(env, arr, 0, 4, result);
    return arr;
}

/* ── 2. getGanZhi ────────────────────────────────────────────────────────── */

JNIEXPORT jobjectArray JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeGetGanZhi(
    JNIEnv *env, jclass clazz, jint year, jint month, jint day, jint hour)
{
    sxtwl_Day *d = sxtwl_newDay();
    sxtwl_fromSolar(d, (int)year, (uint8_t)month, (int)day);

    void *gzYear  = GZ_new(0, 0);
    void *gzMonth = GZ_new(0, 0);
    void *gzDay   = GZ_new(0, 0);
    void *gzHour  = GZ_new(0, 0);

    sxtwl_getYearGZ(gzYear,  d, true);
    sxtwl_getMonthGZ(gzMonth, d);
    sxtwl_getDayGZ(gzDay,    d);
    sxtwl_getHourGZ(gzHour,  d, (uint8_t)hour, false);

    int yTg = GZ_getTg(gzYear),  yDz = GZ_getDz(gzYear);
    int mTg = GZ_getTg(gzMonth), mDz = GZ_getDz(gzMonth);
    int dTg = GZ_getTg(gzDay),   dDz = GZ_getDz(gzDay);
    int hTg = GZ_getTg(gzHour),  hDz = GZ_getDz(gzHour);

    GZ_free(gzYear);
    GZ_free(gzMonth);
    GZ_free(gzDay);
    GZ_free(gzHour);
    sxtwl_freeDay(d);

    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    jobjectArray arr = (*env)->NewObjectArray(env, 4, strClass, NULL);

    jstring s;
    s = makeGZString(env, yTg, yDz); (*env)->SetObjectArrayElement(env, arr, 0, s); (*env)->DeleteLocalRef(env, s);
    s = makeGZString(env, mTg, mDz); (*env)->SetObjectArrayElement(env, arr, 1, s); (*env)->DeleteLocalRef(env, s);
    s = makeGZString(env, dTg, dDz); (*env)->SetObjectArrayElement(env, arr, 2, s); (*env)->DeleteLocalRef(env, s);
    s = makeGZString(env, hTg, hDz); (*env)->SetObjectArrayElement(env, arr, 3, s); (*env)->DeleteLocalRef(env, s);

    return arr;
}

/* ── 3. getJieQi ─────────────────────────────────────────────────────────── */

JNIEXPORT jintArray JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeGetJieQi(
    JNIEnv *env, jclass clazz, jint year, jint month, jint day)
{
    sxtwl_Day *d = sxtwl_newDay();
    sxtwl_fromSolar(d, (int)year, (uint8_t)month, (int)day);

    jint result[6];
    if (sxtwl_hasJieQi(d)) {
        int    jqIdx = (int)sxtwl_getJieQi(d);
        double jd    = sxtwl_getJieQiJD(d);
        sxtwl_Time t = sxtwl_JD2DD(jd);

        result[0] = (jint)jqIdx;
        result[1] = (jint)t.year;
        result[2] = (jint)t.month;
        result[3] = (jint)t.day;
        result[4] = (jint)t.hour;
        result[5] = (jint)t.min;
    } else {
        result[0] = -1;
        result[1] = 0;
        result[2] = 0;
        result[3] = 0;
        result[4] = 0;
        result[5] = 0;
    }

    sxtwl_freeDay(d);

    jintArray arr = (*env)->NewIntArray(env, 6);
    (*env)->SetIntArrayRegion(env, arr, 0, 6, result);
    return arr;
}

/* ── 4. getWeekday ───────────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeGetWeekday(
    JNIEnv *env, jclass clazz, jint year, jint month, jint day)
{
    sxtwl_Day *d = sxtwl_newDay();
    sxtwl_fromSolar(d, (int)year, (uint8_t)month, (int)day);

    int w = (int)sxtwl_getWeek(d);   /* 0=Sunday */

    sxtwl_freeDay(d);
    return (jint)w;
}

/* ── 5. getRunMonth ──────────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeGetRunMonth(
    JNIEnv *env, jclass clazz, jint lunarYear)
{
    return (jint)sxtwl_getRunMonth((int)lunarYear);
}

/* ── 6. getLunarMonthDays ────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeGetLunarMonthDays(
    JNIEnv *env, jclass clazz, jint lunarYear, jint lunarMonth, jboolean isLeap)
{
    return (jint)sxtwl_getLunarMonthNum((int)lunarYear, (uint8_t)lunarMonth, (bool)isLeap);
}

/* ── 7. getSolarTermsForYear ─────────────────────────────────────────────── */

JNIEXPORT jdoubleArray JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeGetSolarTermsForYear(
    JNIEnv *env, jclass clazz, jint year)
{
    /* Initialize all 24 entries to -1 (not found) */
    jdouble result[24];
    for (int i = 0; i < 24; i++) {
        result[i] = -1.0;
    }

    sxtwl_Day *cur = sxtwl_newDay();
    sxtwl_Day *nxt = sxtwl_newDay();

    /* Start at January 1 */
    sxtwl_fromSolar(cur, (int)year, 1, 1);

    /* Walk through the year (max 366 days) */
    for (int i = 0; i < 366; i++) {
        if (sxtwl_hasJieQi(cur)) {
            int idx = (int)sxtwl_getJieQi(cur);
            if (idx >= 0 && idx < 24) {
                result[idx] = sxtwl_getJieQiJD(cur);
            }
        }

        /* Advance to next day */
        sxtwl_after(cur, nxt, 1);

        /* Check if we've crossed into the next year */
        if (sxtwl_getSolarYear(nxt) != (int)year) {
            break;
        }

        /* Swap cur and nxt */
        sxtwl_Day *tmp = cur;
        cur = nxt;
        nxt = tmp;
    }

    sxtwl_freeDay(cur);
    sxtwl_freeDay(nxt);

    jdoubleArray arr = (*env)->NewDoubleArray(env, 24);
    (*env)->SetDoubleArrayRegion(env, arr, 0, 24, result);
    return arr;
}

/* ── 8. solarToJD ────────────────────────────────────────────────────────── */

JNIEXPORT jdouble JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeSolarToJD(
    JNIEnv *env, jclass clazz, jint year, jint month, jint day,
    jint hour, jint minute, jint second)
{
    sxtwl_Time t;
    t.year  = (int)year;
    t.month = (int)month;
    t.day   = (int)day;
    t.hour  = (double)hour;
    t.min   = (double)minute;
    t.sec   = (double)second;

    return (jdouble)sxtwl_toJD(&t);
}

/* ── 9. getSolarTermJD ───────────────────────────────────────────────────── */

JNIEXPORT jdouble JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeGetSolarTermJD(
    JNIEnv *env, jclass clazz, jint year, jint month, jint day)
{
    sxtwl_Day *d = sxtwl_newDay();
    sxtwl_fromSolar(d, (int)year, (uint8_t)month, (int)day);

    jdouble jd = -1.0;
    if (sxtwl_hasJieQi(d)) {
        jd = (jdouble)sxtwl_getJieQiJD(d);
    }

    sxtwl_freeDay(d);
    return jd;
}

/* ── 10. JD2DD (儒略日转公历) ─────────────────────────────────────────────── */

JNIEXPORT jintArray JNICALL
Java_com_nanbanqiu_wannianli_engine_SxtwlBridge_nativeJD2DD(
    JNIEnv *env, jclass clazz, jdouble jd)
{
    sxtwl_Time t = sxtwl_JD2DD((double)jd);

    jint result[6];
    result[0] = t.year;
    result[1] = t.month;
    result[2] = t.day;
    result[3] = (jint)t.hour;
    result[4] = (jint)t.min;
    result[5] = (jint)t.sec;

    jintArray arr = (*env)->NewIntArray(env, 6);
    (*env)->SetIntArrayRegion(env, arr, 0, 6, result);
    return arr;
}