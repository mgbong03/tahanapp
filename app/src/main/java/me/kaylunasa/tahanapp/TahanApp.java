package me.kaylunasa.tahanapp;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import org.json.JSONException;

import me.kaylunasa.tahanapp.util.LocaleHelper;
import me.kaylunasa.tahanapp.util.SettingsDataManager;

public class TahanApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        String language = SettingsDataManager.getLanguage(base);
        super.attachBaseContext(LocaleHelper.setLocale(base, language));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String language = SettingsDataManager.getLanguage(getBaseContext());
        LocaleHelper.setLocale(this, language);
    }
}
