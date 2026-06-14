/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.viewmodel



import android.app.Application

import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.viewModelScope

import com.nanbanqiu.wannianli.data.local.ChangelogDataSource

import com.nanbanqiu.wannianli.data.local.CityPreferences

import com.nanbanqiu.wannianli.data.CityDataSource

import com.nanbanqiu.wannianli.data.model.CalendarDay

import com.nanbanqiu.wannianli.data.model.ChangelogEntry

import com.nanbanqiu.wannianli.data.repository.CalendarRepository

import com.nanbanqiu.wannianli.engine.PureLunarCalendarEngine

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch

import java.util.*



data class MonthDayItem(

    val gregorianYear: Int,

    val gregorianMonth: Int,

    val gregorianDay: Int,

    val isCurrentMonth: Boolean,

    val lunarDayName: String,

    val lunarMonthName: String,

    val isLunarNewMonth: Boolean,

    val isToday: Boolean,

    val isSelected: Boolean,

    val isWeekend: Boolean,

    val solarTermName: String? = null

)



data class PureLunarDayItem(

    val gregorianYear: Int,

    val gregorianMonth: Int,

    val gregorianDay: Int,

    val pureLunarDay: String,

    val isNewMoon: Boolean,

    val isFullMoon: Boolean,

    val moonPhaseName: String

)



class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CalendarRepository(application)

    private val cityPrefs = CityPreferences(application)



    private val _northCityId = MutableStateFlow(cityPrefs.getNorthCityId())

    val northCityId: StateFlow<String> = _northCityId.asStateFlow()



    private val _southCityId = MutableStateFlow(computeAntipodalZoneId(_northCityId.value))

    val southCityId: StateFlow<String> = _southCityId.asStateFlow()



    private fun computeAntipodalZoneId(northId: String): String {

        val northCity = CityDataSource.getCityById(northId) ?: CityDataSource.defaultNorthCity

        return CityDataSource.getAntipodalInfo(northCity).antipodalZoneId

    }



    fun setNorthCity(id: String) {

        _northCityId.value = id

        cityPrefs.setNorthCityId(id)

        _southCityId.value = computeAntipodalZoneId(id)

        loadToday()

    }



    private val _calendarDay = MutableStateFlow<CalendarDay?>(null)

    val calendarDay: StateFlow<CalendarDay?> = _calendarDay.asStateFlow()



    private val _isLoading = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()



    private val _errorMessage = MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()



    private val _viewYear = MutableStateFlow(2026)

    val viewYear: StateFlow<Int> = _viewYear.asStateFlow()



    private val _viewMonth = MutableStateFlow(1)

    val viewMonth: StateFlow<Int> = _viewMonth.asStateFlow()



    private val _monthDays = MutableStateFlow<List<MonthDayItem>>(emptyList())

    val monthDays: StateFlow<List<MonthDayItem>> = _monthDays.asStateFlow()



    private val _selectedYear = MutableStateFlow(2026)

    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()



    private val _selectedMonth = MutableStateFlow(1)

    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()



    private val _selectedDay = MutableStateFlow(1)

    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()



    private val _pureLunarMonthDays = MutableStateFlow<List<PureLunarDayItem>>(emptyList())

    val pureLunarMonthDays: StateFlow<List<PureLunarDayItem>> = _pureLunarMonthDays.asStateFlow()



    private val _changelog = MutableStateFlow<List<ChangelogEntry>>(emptyList())

    val changelog: StateFlow<List<ChangelogEntry>> = _changelog.asStateFlow()



    init {

        loadToday()

        loadChangelog()

    }



    fun loadToday() {

        viewModelScope.launch(Dispatchers.IO) {

            _isLoading.value = true

            _errorMessage.value = null

            try {

                repository.clearCache()



                // 鐢ㄥ寳鍗婄悆鍩庡競鐨勬湰鍦版椂闂磋幏鍙栨棩鏈熷拰鏃惰景

                val northZoneId = _northCityId.value

                val northZone = java.time.ZoneId.of(northZoneId)

                val northNow = java.time.ZonedDateTime.now(northZone)

                val year = northNow.year

                val month = northNow.monthValue

                val day = northNow.dayOfMonth

                val hour = northNow.hour

                val minute = northNow.minute



                _viewYear.value = year

                _viewMonth.value = month

                _selectedYear.value = year

                _selectedMonth.value = month

                _selectedDay.value = day



                val result = repository.getCalendarDay(year, month, day, hour, minute, northZoneId)

                _calendarDay.value = result



                loadMonthDays(year, month, year, month, day)

            } catch (e: Exception) {

                _errorMessage.value = "鍔犺浇鏃ュ巻鏁版嵁澶辫触: ${e.message}"

            } finally {

                _isLoading.value = false

            }

        }

    }



    fun loadDate(year: Int, month: Int, day: Int) {

        viewModelScope.launch(Dispatchers.IO) {

            _isLoading.value = true

            _errorMessage.value = null

            try {

                _selectedYear.value = year

                _selectedMonth.value = month

                _selectedDay.value = day



                if (year != _viewYear.value || month != _viewMonth.value) {

                    _viewYear.value = year

                    _viewMonth.value = month

                }



                val northZone = java.time.ZoneId.of(CityDataSource.toZoneId(_northCityId.value))

                val northNow = java.time.ZonedDateTime.now(northZone)

                val result = repository.getCalendarDay(year, month, day, northNow.hour, northNow.minute, CityDataSource.toZoneId(_northCityId.value))

                _calendarDay.value = result



                loadMonthDays(_viewYear.value, _viewMonth.value, year, month, day)

            } catch (e: Exception) {

                _errorMessage.value = "鍔犺浇鏃ュ巻鏁版嵁澶辫触: ${e.message}"

            } finally {

                _isLoading.value = false

            }

        }

    }



    fun navigateToYearMonth(year: Int, month: Int) {

        _viewYear.value = year

        _viewMonth.value = month



        val selYear = _selectedYear.value

        val selMonth = _selectedMonth.value

        val selDay = _selectedDay.value



        viewModelScope.launch(Dispatchers.IO) {

            loadMonthDays(year, month, selYear, selMonth, selDay)

        }

    }



    fun navigateMonth(offset: Int) {

        var newYear = _viewYear.value

        var newMonth = _viewMonth.value + offset



        if (newMonth > 12) {

            newYear += 1

            newMonth = 1

        } else if (newMonth < 1) {

            newYear -= 1

            newMonth = 12

        }



        _viewYear.value = newYear

        _viewMonth.value = newMonth



        val selYear = _selectedYear.value

        val selMonth = _selectedMonth.value

        val selDay = _selectedDay.value



        viewModelScope.launch(Dispatchers.IO) {

            loadMonthDays(newYear, newMonth, selYear, selMonth, selDay)

        }

    }



    fun selectDay(year: Int, month: Int, day: Int) {

        if (year == _selectedYear.value && month == _selectedMonth.value && day == _selectedDay.value) return



        _selectedYear.value = year

        _selectedMonth.value = month

        _selectedDay.value = day



        if (year != _viewYear.value || month != _viewMonth.value) return



        viewModelScope.launch(Dispatchers.IO) {

            try {

                val northZone = java.time.ZoneId.of(CityDataSource.toZoneId(_northCityId.value))

                val northNow = java.time.ZonedDateTime.now(northZone)

                val result = repository.getCalendarDay(year, month, day, northNow.hour, northNow.minute, CityDataSource.toZoneId(_northCityId.value))

                _calendarDay.value = result

            } catch (_: Exception) {}



            loadMonthDays(_viewYear.value, _viewMonth.value, year, month, day)

        }

    }



    private fun loadMonthDays(viewYear: Int, viewMonth: Int, selYear: Int, selMonth: Int, selDay: Int) {

        val northZone = java.time.ZoneId.of(CityDataSource.toZoneId(_northCityId.value))

        val northNow = java.time.ZonedDateTime.now(northZone)

        val todayYear = northNow.year

        val todayMonth = northNow.monthValue

        val todayDay = northNow.dayOfMonth



        val dayCount = repository.getMonthDayCount(viewYear, viewMonth)



        val firstDayWeekday = (java.time.LocalDate.of(viewYear, viewMonth, 1).dayOfWeek.value - 1) % 7



        val items = mutableListOf<MonthDayItem>()



        val prevMonth: Int

        val prevYear: Int

        if (viewMonth == 1) {

            prevMonth = 12

            prevYear = viewYear - 1

        } else {

            prevMonth = viewMonth - 1

            prevYear = viewYear

        }

        val prevDayCount = repository.getMonthDayCount(prevYear, prevMonth)



        for (i in 0 until firstDayWeekday) {

            val d = prevDayCount - firstDayWeekday + i + 1

            items.add(buildMonthDayItem(prevYear, prevMonth, d, false, todayYear, todayMonth, todayDay, selYear, selMonth, selDay))

        }



        for (d in 1..dayCount) {

            items.add(buildMonthDayItem(viewYear, viewMonth, d, true, todayYear, todayMonth, todayDay, selYear, selMonth, selDay))

        }



        val remaining = 42 - items.size

        val nextMonth: Int

        val nextYear: Int

        if (viewMonth == 12) {

            nextMonth = 1

            nextYear = viewYear + 1

        } else {

            nextMonth = viewMonth + 1

            nextYear = viewYear

        }



        for (d in 1..remaining) {

            items.add(buildMonthDayItem(nextYear, nextMonth, d, false, todayYear, todayMonth, todayDay, selYear, selMonth, selDay))

        }



        _monthDays.value = items

    }



    private fun buildMonthDayItem(

        year: Int, month: Int, day: Int, isCurrentMonth: Boolean,

        todayYear: Int, todayMonth: Int, todayDay: Int,

        selYear: Int, selMonth: Int, selDay: Int

    ): MonthDayItem {

        return MonthDayItem(

            gregorianYear = year,

            gregorianMonth = month,

            gregorianDay = day,

            isCurrentMonth = isCurrentMonth,

            lunarDayName = repository.getLunarDayName(year, month, day),

            lunarMonthName = repository.getLunarMonthName(year, month, day),

            isLunarNewMonth = repository.isLunarNewMonth(year, month, day),

            isToday = year == todayYear && month == todayMonth && day == todayDay,

            isSelected = year == selYear && month == selMonth && day == selDay,

            isWeekend = repository.isWeekend(year, month, day),

            solarTermName = repository.getSolarTermDayName(year, month, day)

        )

    }



    fun loadPureLunarMonth(viewYear: Int, viewMonth: Int) {

        viewModelScope.launch(Dispatchers.IO) {

            try {

                val dayCount = repository.getMonthDayCount(viewYear, viewMonth)

                val items = mutableListOf<PureLunarDayItem>()



                for (d in 1..dayCount) {

                    val pureLunarDate = PureLunarCalendarEngine.getPureLunarDate(viewYear, viewMonth, d)

                    items.add(

                        PureLunarDayItem(

                            gregorianYear = viewYear,

                            gregorianMonth = viewMonth,

                            gregorianDay = d,

                            pureLunarDay = pureLunarDate?.dayName ?: "",

                            isNewMoon = pureLunarDate?.isNewMoon ?: false,

                            isFullMoon = pureLunarDate?.isFullMoon ?: false,

                            moonPhaseName = pureLunarDate?.moonPhase ?: ""

                        )

                    )

                }



                _pureLunarMonthDays.value = items

            } catch (e: Exception) {

                _pureLunarMonthDays.value = emptyList()

            }

        }

    }



    fun loadChangelog() {

        viewModelScope.launch(Dispatchers.IO) {

            try {

                val context = getApplication<Application>()

                _changelog.value = ChangelogDataSource.getChangelog(context)

            } catch (_: Exception) {}

        }

    }



    fun getRepository(): CalendarRepository = repository

}