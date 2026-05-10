package com.hastakala.shop.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {
    fun setLocale(language: String) {
        val localeCode = when (language) {
            "Malayalam" -> "ml"
            "Tamil" -> "ta"
            "Kannada" -> "kn"
            "Telugu" -> "te"
            "Hindi" -> "hi"
            else -> "en"
        }
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
