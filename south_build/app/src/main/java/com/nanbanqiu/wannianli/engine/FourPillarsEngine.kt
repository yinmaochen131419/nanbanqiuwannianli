/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.engine





import kotlin.math.floor





object FourPillarsEngine {


    private const val JD_1900_01_01 = 2415020


    private const val BASE_CYCLE_DAY = 10





    fun calculate(


        year: Int,


        month: Int,


        day: Int,


        hour: Int,


        minute: Int,


        solarTerms: List<SolarTermCalculator.SolarTermResult>,


        lunarResult: LunarCalendarEngine.LunarResult


    ): PillarsResult {


        val yearPillar = calcYearPillar(year, month, day, solarTerms)


        val monthPillar = calcMonthPillar(year, month, day, solarTerms)


        val dayPillar = calcDayPillar(year, month, day)


        val hourDayGanIndex = if (hour in 23..23) {


            val next = nextCalendarDay(year, month, day)


            calcDayPillar(next.first, next.second, next.third).dayGanIndex


        } else {


            dayPillar.dayGanIndex


        }


        val hourPillar = calcHourPillar(hour, hourDayGanIndex)





        val liChun = solarTerms.find { it.name == "立春" }
        val effectiveYear = if (liChun != null && (month < liChun.month || (month == liChun.month && day < liChun.day))) {
            year - 1
        } else {
            year
        }
        val shengXiaoIndex = ((effectiveYear - 4) % 12 + 12) % 12
        val shengXiao = CalendarConstants.SHENG_XIAO[shengXiaoIndex]

        val northLunarMonthIndex = lunarResult.month - 1
        val lunarMonthName = if (lunarResult.isLeapMonth) {
            "闰${CalendarConstants.NORTH_LUNAR_MONTH_NAMES[northLunarMonthIndex]}"
        } else {
            CalendarConstants.NORTH_LUNAR_MONTH_NAMES[northLunarMonthIndex]
        }





        val lunarDayName = CalendarConstants.LUNAR_DAY_NAMES.getOrElse(lunarResult.day - 1) { "${lunarResult.day}" }





        val ganZhiYear = calcGanZhiYear(year, month, day, solarTerms)





        return PillarsResult(


            yearGanZhi = yearPillar,


            monthGanZhi = monthPillar,


            dayGanZhi = "${CalendarConstants.TIAN_GAN[dayPillar.dayGanIndex]}${CalendarConstants.DI_ZHI[dayPillar.dayZhiIndex]}",


            hourGanZhi = hourPillar,


            shengXiao = shengXiao,


            lunarMonthName = lunarMonthName,


            lunarDayName = lunarDayName,


            lunarYear = lunarResult.year,


            lunarMonth = lunarResult.month,


            lunarDay = lunarResult.day,


            isLeapMonth = lunarResult.isLeapMonth,


            ganZhiYear = ganZhiYear


        )


    }





    private fun calcGanZhiYear(year: Int, month: Int, day: Int, solarTerms: List<SolarTermCalculator.SolarTermResult>): String {


        val liChun = solarTerms.find { it.name == "立春" }
        if (liChun != null) {
            val isBeforeLiChun = month < liChun.month ||
                    (month == liChun.month && day < liChun.day)
            val effectiveYear = if (isBeforeLiChun) year - 1 else year
            return calcYearPillarByYear(effectiveYear)
        }
        return calcYearPillarByYear(year)
    }

    private fun calcYearPillar(year: Int, month: Int, day: Int, solarTerms: List<SolarTermCalculator.SolarTermResult>): String {
        val liChun = solarTerms.find { it.name == "立春" }


        if (liChun != null) {


            val isBeforeLiChun = month < liChun.month ||


                    (month == liChun.month && day < liChun.day)


            val effectiveYear = if (isBeforeLiChun) year - 1 else year


            return calcYearPillarByYear(effectiveYear)


        }


        return calcYearPillarByYear(year)


    }





    private fun calcYearPillarByYear(year: Int): String {


        val ganIndex = ((year - 4) % 10 + 10) % 10


        val zhiIndex = ((year - 4) % 12 + 12) % 12


        return "${CalendarConstants.TIAN_GAN[ganIndex]}${CalendarConstants.DI_ZHI[zhiIndex]}"


    }





    private fun calcMonthPillar(year: Int, month: Int, day: Int, solarTerms: List<SolarTermCalculator.SolarTermResult>): String {


        val jieTerms = solarTerms.filter { CalendarConstants.JIE_NAMES.contains(it.name) }


            .sortedWith(compareBy({ it.month }, { it.day }))





        var monthIndex = 0


        for (jie in jieTerms) {


            if (jie.year > year) continue


            if (month > jie.month || (month == jie.month && day >= jie.day)) {


                monthIndex = CalendarConstants.JIE_NAMES.indexOf(jie.name)


            }


        }





        val yearGanZhi = calcYearPillar(year, month, day, solarTerms)


        val yearGanIndex = CalendarConstants.TIAN_GAN.indexOf(yearGanZhi[0].toString())


        val startIndex = CalendarConstants.ganIndexToStartIndex(yearGanIndex)


        val januaryStartGan = CalendarConstants.FIVE_TIGER_MONTH_GAN_START[startIndex]


        val monthGanIndex = (januaryStartGan + (monthIndex + 6) % 12) % 10


        val monthZhiIndex = CalendarConstants.MONTH_ZHI_INDEX[monthIndex]





        return "${CalendarConstants.TIAN_GAN[monthGanIndex]}${CalendarConstants.DI_ZHI[monthZhiIndex]}"


    }





    data class DayPillarIndices(val dayGanIndex: Int, val dayZhiIndex: Int)





    fun calcDayPillar(year: Int, month: Int, day: Int): DayPillarIndices {


        val jd = gregorianToJD(year, month, day)


        val dayDiff = jd.toLong() - JD_1900_01_01


        val cycleDay = ((BASE_CYCLE_DAY + dayDiff) % 60 + 60) % 60


        return DayPillarIndices((cycleDay % 10).toInt(), (cycleDay % 12).toInt())


    }





    private fun calcHourPillar(hour: Int, dayGanIndex: Int): String {


        val zhiIndex = when (hour) {


            23, 0 -> 0


            1, 2 -> 1


            3, 4 -> 2


            5, 6 -> 3


            7, 8 -> 4


            9, 10 -> 5


            11, 12 -> 6


            13, 14 -> 7


            15, 16 -> 8


            17, 18 -> 9


            19, 20 -> 10


            21, 22 -> 11


            else -> 0


        }





        val startIndex = CalendarConstants.ganIndexToStartIndex(dayGanIndex)


        val hourStartGan = CalendarConstants.FIVE_RAT_HOUR_GAN_START[startIndex]


        val ganIndex = (hourStartGan + zhiIndex) % 10





        return "${CalendarConstants.TIAN_GAN[ganIndex]}${CalendarConstants.DI_ZHI[zhiIndex]}"


    }





    private fun gregorianToJD(year: Int, month: Int, day: Int): Double {


        var y = year


        var m = month


        if (m <= 2) { y -= 1; m += 12 }


        val a = floor(y / 100.0)


        val b = 2 - a + floor(a / 4.0)


        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5


    }





    private fun nextCalendarDay(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {


        val d = daysInMonth(year, month)


        return if (day < d) Triple(year, month, day + 1)


        else if (month < 12) Triple(year, month + 1, 1)


        else Triple(year + 1, 1, 1)


    }





    private fun daysInMonth(year: Int, month: Int): Int = when (month) {


        1 -> 31; 2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28


        3 -> 31; 4 -> 30; 5 -> 31; 6 -> 30; 7 -> 31; 8 -> 31; 9 -> 30; 10 -> 31; 11 -> 30; 12 -> 31


        else -> 30


    }





    data class PillarsResult(


        val yearGanZhi: String,


        val monthGanZhi: String,


        val dayGanZhi: String,


        val hourGanZhi: String,


        val shengXiao: String,


        val lunarMonthName: String,


        val lunarDayName: String,


        val lunarYear: Int,


        val lunarMonth: Int,


        val lunarDay: Int,


        val isLeapMonth: Boolean,


        val ganZhiYear: String


    )





    fun calculateNorthern(


        year: Int,


        month: Int,


        day: Int,


        hour: Int,


        minute: Int,


        solarTerms: List<SolarTermCalculator.SolarTermResult>,


        lunarResult: LunarCalendarEngine.LunarResult


    ): NorthernPillars {


        val dayPillar = calcDayPillar(year, month, day)


        val hourDayGanIndex = if (hour in 23..23) {


            val next = nextCalendarDay(year, month, day)


            calcDayPillar(next.first, next.second, next.third).dayGanIndex


        } else {


            dayPillar.dayGanIndex


        }


        val hourPillar = calcHourPillar(hour, hourDayGanIndex)





        val liChun = solarTerms.find { it.northName == "立春" }


        val effectiveYear = if (liChun != null && (month < liChun.month || (month == liChun.month && day < liChun.day))) {


            year - 1


        } else {


            year


        }


        val shengXiaoIndex = ((effectiveYear - 4) % 12 + 12) % 12


        val shengXiao = CalendarConstants.SHENG_XIAO[shengXiaoIndex]





        val yearGanZhi = calcYearPillarByYear(effectiveYear)





        val jieTerms = solarTerms.filter { CalendarConstants.NORTH_JIE_NAMES.contains(it.northName) }


            .sortedWith(compareBy({ it.month }, { it.day }))





        var monthIndex = 0


        for (jie in jieTerms) {


            if (jie.year > year) continue


            if (month > jie.month || (month == jie.month && day >= jie.day)) {


                monthIndex = CalendarConstants.NORTH_JIE_NAMES.indexOf(jie.northName)


            }


        }





        val yearGanIndex = CalendarConstants.TIAN_GAN.indexOf(yearGanZhi[0].toString())


        val startIndex = CalendarConstants.ganIndexToStartIndex(yearGanIndex)


        val januaryStartGan = CalendarConstants.FIVE_TIGER_MONTH_GAN_START[startIndex]


        val monthGanIndex = (januaryStartGan + monthIndex) % 10


        val monthZhiIndex = CalendarConstants.NORTH_MONTH_ZHI_INDEX[monthIndex]


        val monthGanZhi = "${CalendarConstants.TIAN_GAN[monthGanIndex]}${CalendarConstants.DI_ZHI[monthZhiIndex]}"





        val dayGanZhi = "${CalendarConstants.TIAN_GAN[dayPillar.dayGanIndex]}${CalendarConstants.DI_ZHI[dayPillar.dayZhiIndex]}"





        return NorthernPillars(


            yearGanZhi = yearGanZhi,


            monthGanZhi = monthGanZhi,


            dayGanZhi = dayGanZhi,


            hourGanZhi = hourPillar,


            shengXiao = shengXiao


        )


    }





    data class NorthernPillars(


        val yearGanZhi: String,


        val monthGanZhi: String,


        val dayGanZhi: String,


        val hourGanZhi: String,


        val shengXiao: String


    )





    fun flipGanZhi(gz: String): String {


        if (gz.length < 2) return gz


        val ganIndex = CalendarConstants.TIAN_GAN.indexOf(gz[0].toString())


        val zhiIndex = CalendarConstants.DI_ZHI.indexOf(gz[1].toString())


        if (ganIndex < 0 || zhiIndex < 0) return gz


        val flippedGan = CalendarConstants.TIAN_GAN[(ganIndex + 6) % 10]


        val flippedZhi = CalendarConstants.DI_ZHI[(zhiIndex + 6) % 12]


        return "$flippedGan$flippedZhi"


    }





    data class FlippedPillars(


        val yearGanZhi: String,


        val monthGanZhi: String,


        val dayGanZhi: String,


        val hourGanZhi: String,


        val shengXiao: String


    )





    fun flipToSouthern(northern: PillarsResult): FlippedPillars {


        val shengXiaoIndex = CalendarConstants.SHENG_XIAO.indexOf(northern.shengXiao)


        val flippedShengXiao = if (shengXiaoIndex >= 0) {


            CalendarConstants.SHENG_XIAO[(shengXiaoIndex + 6) % 12]


        } else northern.shengXiao





        return FlippedPillars(


            yearGanZhi = flipGanZhi(northern.yearGanZhi),


            monthGanZhi = flipGanZhi(northern.monthGanZhi),


            dayGanZhi = flipGanZhi(northern.dayGanZhi),


            hourGanZhi = flipGanZhi(northern.hourGanZhi),


            shengXiao = flippedShengXiao


        )


    }


}





data class FourPillarsResult(


    val yearGanZhi: String,


    val monthGanZhi: String,


    val dayGanZhi: String,


    val hourGanZhi: String


)