/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.util

import android.content.Context
import android.os.LocaleList
import java.util.Locale

object LanguageHelper {
    private const val PREFS_NAME = "app_language_prefs"
    private const val KEY_LANGUAGE = "app_language"

    const val LANG_ZH = "zh"
    const val LANG_ZH_TW = "zh_TW"
    const val LANG_EN = "en"
    const val LANG_ES = "es"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANG_ZH) ?: LANG_ZH
    }

    fun saveLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    fun getAppLocale(langCode: String): Locale {
        return when (langCode) {
            LANG_ZH_TW -> Locale.TRADITIONAL_CHINESE
            LANG_EN -> Locale.ENGLISH
            LANG_ES -> Locale("es")
            else -> Locale.SIMPLIFIED_CHINESE
        }
    }

    fun applyLanguage(context: Context, langCode: String): Context {
        val locale = getAppLocale(langCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLocales(LocaleList(locale))

        return context.createConfigurationContext(config)
    }

    fun getLanguageDisplayName(langCode: String): String {
        return when (langCode) {
            LANG_ZH -> "简体中文"
            LANG_ZH_TW -> "繁體中文"
            LANG_EN -> "English"
            LANG_ES -> "Español (Argentina)"
            else -> "简体中文"
        }
    }

    fun getAllLanguages(): List<String> = listOf(LANG_ZH, LANG_ZH_TW, LANG_EN, LANG_ES)
}
