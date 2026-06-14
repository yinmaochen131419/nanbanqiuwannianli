/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli

import android.app.Application
import com.nanbanqiu.wannianli.util.LanguageHelper

class WannianliApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val langCode = LanguageHelper.getSavedLanguage(this)
        LanguageHelper.applyLanguage(this, langCode)
    }
}