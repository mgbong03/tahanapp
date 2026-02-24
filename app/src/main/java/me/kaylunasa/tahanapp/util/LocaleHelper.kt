package me.kaylunasa.tahanapp.util

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import java.util.IllformedLocaleException
import java.util.Locale

object LocaleHelper {
    @JvmStatic
    fun setLocale(context: Context, languageCode: String?): Context {
        if (languageCode == null) return context

        try {
            val locale = Locale.Builder().setLanguage(languageCode).build()
            Locale.setDefault(locale)
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