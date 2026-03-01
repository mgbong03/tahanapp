package me.kaylunasa.tahanapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.fragment.ChangePasswordFragment;
import me.kaylunasa.tahanapp.util.LocaleHelper;
import me.kaylunasa.tahanapp.util.SessionDataManager;
import me.kaylunasa.tahanapp.util.SettingsDataManager;
import me.kaylunasa.tahanapp.util.Sha256Kt;
import me.kaylunasa.tahanapp.util.UserDataManager;

public class SettingsActivity extends TahanAppActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private String username;
    private String settingLanguage;
    private boolean settingForcedNarration;
    private boolean settingShowPreliminary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            this.username = SessionDataManager.getSessionUsername(this);

            this.settingLanguage = SettingsDataManager.getLanguage(this);
            this.settingForcedNarration = SettingsDataManager.getForceNarrations(this);
            this.settingShowPreliminary = SettingsDataManager.getShowPreliminary(this);
        }
        catch (IllegalStateException | JSONException e) {
            Toast.makeText(this, getResources().getText(R.string.logged_out_force), Toast.LENGTH_SHORT).show();
            try {
                SessionDataManager.resetSessionData(this);
            }
            catch (IOException ex) {
                Toast.makeText(this, "Could not reset stored data.", Toast.LENGTH_SHORT)
                        .show();
                System.exit(0);
                return;
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        SettingsDataManager.dumpSettings(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                TextView title = findViewById(R.id.toolbarTitle);
                title.setText(getResources().getText(R.string.settings));

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            toolbar.setNavigationOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());

            if (toolbar.getNavigationIcon() != null)
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.black));
        }

        TextView accountDisplayView = findViewById(R.id.accountDisplay);
        accountDisplayView.setText(String.format(
                getResources().getText(R.string.settings_account).toString(),
                username
        ));

        Button changePasswordBtn = findViewById(R.id.editPassword);
        changePasswordBtn.setOnClickListener((v) -> {
            ChangePasswordFragment passwordFragment = new ChangePasswordFragment();
            passwordFragment.show(getSupportFragmentManager(), "ChangePasswordFragment");
        });

        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener((v) -> logout());

        RadioButton defaultRadioBtn = findViewById(R.id.systemDefaultRadioBtn);
        RadioButton englishRadioBtn = findViewById(R.id.englishRadioBtn);
        RadioButton filipinoRadioBtn = findViewById(R.id.filipinoRadioBtn);

        if (settingLanguage != null) switch (settingLanguage) {
            case "en":
                englishRadioBtn.setChecked(true);
                break;
            case "fil":
                filipinoRadioBtn.setChecked(true);
                break;
            default:
                defaultRadioBtn.setChecked(true);
        }
        else
            defaultRadioBtn.setChecked(true);

        Button saveLanguageBtn = findViewById(R.id.saveLanguageBtn);
        saveLanguageBtn.setOnClickListener((v) -> {
            String locale = null;
            if (englishRadioBtn.isChecked())
                locale = "en";
            if (filipinoRadioBtn.isChecked())
                locale = "fil";

            try {
                SettingsDataManager.putLanguage(this, locale);
                LocaleHelper.setLocale(this, locale);
            } catch (JSONException ex) {
                Log.e(TAG, "locale change: Could not change locale", ex);
                Toast.makeText(SettingsActivity.this, "Unable to change language", Toast.LENGTH_SHORT)
                        .show();
            }

            Toast.makeText(this, getResources().getText(R.string.settings_saved), Toast.LENGTH_SHORT).show();
            recreate();
        });

        CheckBox forcedNarrationCheckbox = findViewById(R.id.forcedNarrationsCheckbox);
        forcedNarrationCheckbox.setChecked(settingForcedNarration);

        CheckBox showPreliminaryCheckbox = findViewById(R.id.showPreliminaryCheckbox);
        showPreliminaryCheckbox.setChecked(settingShowPreliminary);

        Button saveNarrationBtn = findViewById(R.id.saveNarrationBtn);
        saveNarrationBtn.setOnClickListener((v) -> {
            try {
                this.settingForcedNarration = forcedNarrationCheckbox.isChecked();
                this.settingShowPreliminary = forcedNarrationCheckbox.isChecked();
                SettingsDataManager.putForceNarrations(this, this.settingForcedNarration);
                SettingsDataManager.putShowPreliminary(this, this.settingShowPreliminary);
                Toast.makeText(this, getResources().getText(R.string.settings_saved), Toast.LENGTH_SHORT).show();
            }
            catch (JSONException ex)  {
                Log.e(TAG, "assessment settings change: Could not change assessment settings", ex);
                Toast.makeText(SettingsActivity.this, "Unable to save settings", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        Button deleteAccountBtn = findViewById(R.id.deleteAccountBtn);
        deleteAccountBtn.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_account)
                    .setMessage(R.string.delete_account_confirm)
                    .setPositiveButton("Delete", (v2, e) -> {
                        AlertDialog.Builder deleteAccountPasswordConfirm = new AlertDialog.Builder(this);
                        deleteAccountPasswordConfirm.setTitle(getResources().getText(R.string.delete_account))
                                .setMessage(getResources().getText(R.string.confirm_sign_up_password));

                        final EditText confirmPassInput = new EditText(this);
                        confirmPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        final LinearLayout container = new LinearLayout(this);
                        container.setOrientation(LinearLayout.VERTICAL);

                        int margin = (int) (20 * getResources().getDisplayMetrics().density);
                        container.setPadding(margin, 0, margin, 0);
                        container.addView(confirmPassInput);

                        deleteAccountPasswordConfirm.setView(container);

                        deleteAccountPasswordConfirm.setPositiveButton("OK", (v3, e2) -> {
                            try {
                                String username = SessionDataManager.getSessionUsername(this);
                                if (username == null)
                                    throw new IllegalStateException();
                                String confirmPasswordHash = Sha256Kt.sha256(confirmPassInput.getText().toString());

                                if (!UserDataManager.checkUserPassword(this, username, confirmPasswordHash)) {
                                    Toast.makeText(this, getResources().getText(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                UserDataManager.deleteUserData(this, username, confirmPasswordHash);
                                try {
                                    SessionDataManager.resetSessionData(this);

                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                                catch (IOException ex) {
                                    Toast.makeText(this, "Could not reset stored data.", Toast.LENGTH_SHORT)
                                            .show();
                                    System.exit(0);
                                }

                            }
                            catch (IllegalStateException | JSONException ex) {
                                Log.e(TAG, "onCreate: Could not delete account", ex);
                                Toast.makeText(getApplicationContext(), "Unable to delete account", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }).setNegativeButton("Cancel", (v3, e2) -> Toast.makeText(
                                this,
                                getResources().getText(R.string.delete_account_cancel),
                                Toast.LENGTH_SHORT
                        ).show()).show();
                    })
                    .setNegativeButton("Cancel", (v2, e) -> Toast.makeText(
                            this,
                            getResources().getText(R.string.delete_account_cancel),
                            Toast.LENGTH_SHORT)
                    .show())
                    .show();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                boolean unsavedChanges = false;

                String selectedLanguageSetting = null;
                if (englishRadioBtn.isChecked())
                    selectedLanguageSetting = "en";
                if (filipinoRadioBtn.isChecked())
                    selectedLanguageSetting = "fil";
                if (!Objects.equals(selectedLanguageSetting, settingLanguage))
                    unsavedChanges = true;

                boolean selectedNarrationSetting = forcedNarrationCheckbox.isChecked();
                if (selectedNarrationSetting != settingForcedNarration)
                    unsavedChanges = true;

                boolean selectedPreliminarySetting = showPreliminaryCheckbox.isChecked();
                if (selectedPreliminarySetting != settingShowPreliminary)
                    unsavedChanges = true;

                if (unsavedChanges) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SettingsActivity.this);
                    alertBuilder.setTitle(getResources().getText(R.string.unsaved_changes))
                            .setMessage(getResources().getText(R.string.unsaved_changes_prompt))
                            .setPositiveButton("Exit", (v, e) -> finish())
                            .setNegativeButton("Cancel", (v, e) -> {}).show();
                }
                else {
                    setEnabled(false);
                    finish();
                }
            }
        });
    }

    private void logout() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getResources().getText(R.string.log_out))
                .setMessage(getResources().getText(R.string.logout_confirmation))
                .setPositiveButton("OK", (v, e) -> {
                    try {
                        SessionDataManager.resetSessionData(this);
                    }
                    catch (IOException ex) {
                        Toast.makeText(this, "Could not reset stored data.", Toast.LENGTH_SHORT)
                                .show();
                        System.exit(0);
                    }

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (v, e) -> Toast.makeText(
                        this,
                        getResources().getText(R.string.logout_cancel),
                        Toast.LENGTH_SHORT).show())
                .show();
    }
}