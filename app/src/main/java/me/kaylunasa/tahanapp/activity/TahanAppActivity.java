package me.kaylunasa.tahanapp.activity;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import me.kaylunasa.tahanapp.util.LocaleHelper;
import me.kaylunasa.tahanapp.util.SettingsDataManager;

public abstract class TahanAppActivity extends AppCompatActivity {
    protected Context originalContext;

    @Override
    protected void attachBaseContext(Context base) {
        this.originalContext = base;

        String language = SettingsDataManager.getLanguage(base);
        super.attachBaseContext(LocaleHelper.setLocale(base, language));
    }
}
