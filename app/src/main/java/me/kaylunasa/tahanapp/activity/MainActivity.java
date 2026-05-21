package me.kaylunasa.tahanapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.util.LocaleHelper;
import me.kaylunasa.tahanapp.util.SessionDataManager;
import me.kaylunasa.tahanapp.util.SettingsDataManager;
import me.kaylunasa.tahanapp.util.Sha256Kt;
import me.kaylunasa.tahanapp.util.UserDataManager;

public class MainActivity extends TahanAppActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (SettingsDataManager.isFirstLaunch(this)) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_language, null);

            AlertDialog.Builder chooseSystemLanguageBuilder = new AlertDialog.Builder(this);
            AlertDialog chooseSystemLanguage = chooseSystemLanguageBuilder.setTitle(getResources().getText(R.string.first_time_language))
                    .setView(dialogView)
                    .setCancelable(false)
                    .setPositiveButton("OK", (v, e) -> {
                        try {
                            RadioButton englishRadioBtn = dialogView.findViewById(R.id.englishRadioBtn);
                            RadioButton filipinoRadioBtn = dialogView.findViewById(R.id.filipinoRadioBtn);

                            String locale = null;
                            if (englishRadioBtn.isChecked())
                                locale = "en";
                            if (filipinoRadioBtn.isChecked())
                                locale = "fil";

                            try {
                                SettingsDataManager.putLanguage(this, locale);
                                LocaleHelper.setLocale(this, locale);
                                Toast.makeText(this, getResources().getText(R.string.settings_saved), Toast.LENGTH_SHORT).show();
                                SettingsDataManager.markNonFirstLaunch(MainActivity.this);
                                recreate();
                            }
                            catch (JSONException ex) {
                                Log.e(TAG, "locale change: Could not change locale", ex);
                                Toast.makeText(this, "Unable to set language", Toast.LENGTH_SHORT)
                                        .show();
                                throw ex;
                            }
                        }
                        catch (JSONException ex) {
                            Log.e(TAG, "onCreate: Could not mark as first launch", ex);
                        }
                    })
                    .create();

            chooseSystemLanguage.show();
            chooseSystemLanguage.setOnDismissListener(d -> setSessionData());
        }
        else
            setSessionData();
    }

    private void setSessionData() {
        try {
            if (!UserDataManager.userDataExists(this, "default")) {
                UserDataManager.createUserData(this, "default", Sha256Kt.sha256("default"));
            }

            SessionDataManager.putSessionUsername(this, "default");
            SessionDataManager.putSessionPasswordHash(this, Sha256Kt.sha256("default"));

            if (SessionDataManager.getSessionUsername(this) != null) {
                Log.d(TAG, "onCreate: Session data detected");
                SessionDataManager.dumpSessionData(this);

                String username = Objects.requireNonNullElse(SessionDataManager.getSessionUsername(this), "");
                String passwordHash = Objects.requireNonNullElse(SessionDataManager.getSessionPasswordHash(this), "");
                Log.d(TAG, "onCreate: Dumping user data");
                Log.d(TAG, "onCreate: " + UserDataManager.getUserData(this, username, passwordHash));

                if (UserDataManager.checkUserPassword(this, username, passwordHash))
                    moveToHomeActivity();
                else {
                    Log.d(TAG, "onCreate: Existing user data password does not match");
                    try {
                        SessionDataManager.resetSessionData(this);
                    }
                    catch (IOException ex) {
                        Toast.makeText(this, "Could not reset stored data.", Toast.LENGTH_SHORT)
                                .show();
                        System.exit(0);
                        return;
                    }
                    Toast.makeText(this, getResources().getText(R.string.logged_out_force), Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (JSONException e) {
            Toast.makeText(this, "Could not write session data.", Toast.LENGTH_SHORT)
                    .show();
            System.exit(0);
        }
    }

    private void moveToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}