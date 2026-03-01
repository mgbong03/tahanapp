package me.kaylunasa.tahanapp.util

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.IllformedLocaleException
import java.util.Locale

object LocaleHelper {
    @JvmStatic
    fun setLocale(context: Context, languageCode: String?): Context {
        try {
            val locale = languageCode?.let { Locale.Builder().setLanguage(it).build() }
                ?: Resources.getSystem().configuration.locales[0]
            Locale.setDefault(locale)
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(locale.toLanguageTag())
            )
            return updateResources(context, locale)
        }
        catch (e: IllformedLocaleException) {
            return context
        }
    }

    private fun updateResources(context: Context, locale: Locale): Context {
        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}