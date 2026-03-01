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

        try {
            if (SessionDataManager.getSessionUsername(this) != null) {
                Log.d(TAG, "onCreate: Existing session data detected");
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getResources().getString(R.string.notice_title))
                    .setMessage("Malformed session data found. Reset session data?")
                    .setPositiveButton("OK", (dialog, id) -> {
                        try {
                            SessionDataManager.resetSessionData(this);
                        }
                        catch (IOException ex) {
                            Toast.makeText(this, "Could not reset stored data.", Toast.LENGTH_SHORT)
                                    .show();
                            System.exit(0);
                            return;
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> {})
                    .show();
        }

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
            chooseSystemLanguageBuilder.setTitle(getResources().getText(R.string.first_time_language))
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
                    .show();
        }

        final Button loginBtn = findViewById(R.id.login_btn);
        final Button signupBtn = findViewById(R.id.signup_btn);

        final TextInputLayout usernameInput = findViewById(R.id.usernameTextField);
        final TextInputLayout passwordInput = findViewById(R.id.passwordTextField);

        assert usernameInput.getEditText() != null;
        assert passwordInput.getEditText() != null;

        final EditText usernameEditText = usernameInput.getEditText();
        final EditText passwordEditText = passwordInput.getEditText();

        View.OnClickListener listener = (v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false)
                    .setTitle(getResources().getString(R.string.notice_title))
                    .setMessage(getResources().getString(R.string.notice))
                    .setPositiveButton("OK", (dialog, id) -> {
                        String username = usernameEditText.getText().toString();
                        String password = passwordEditText.getText().toString();

                        if (username.length() < 4) {
                            Toast.makeText(getApplicationContext(), getResources().getText(R.string.username_too_short), Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        if (!username.matches("[a-zA-Z0-9_]+")) {
                            Toast.makeText(getApplicationContext(), getResources().getText(R.string.username_wrong_format), Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        if (password.length() < 8) {
                            Toast.makeText(getApplicationContext(), getResources().getText(R.string.password_too_short), Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        String passwordHash = Sha256Kt.sha256(password);

                        if (v.getId() == R.id.signup_btn) {
                            if (UserDataManager.userDataExists(getApplicationContext(), username)) {
                                Toast.makeText(this, getResources().getText(R.string.user_exists), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            AlertDialog.Builder signupPasswordConfirm = new AlertDialog.Builder(this);
                            signupPasswordConfirm.setTitle(getResources().getText(R.string.sign_up))
                                    .setMessage(getResources().getText(R.string.confirm_sign_up_password));

                            final EditText confirmPassInput = new EditText(this);
                            confirmPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                            final LinearLayout container = new LinearLayout(this);
                            container.setOrientation(LinearLayout.VERTICAL);

                            int margin = (int) (20 * getResources().getDisplayMetrics().density);
                            container.setPadding(margin, 0, margin, 0);
                            container.addView(confirmPassInput);

                            signupPasswordConfirm.setView(container);

                            signupPasswordConfirm.setPositiveButton("OK", (v2, e) -> {
                                String confirmPasswordHash = Sha256Kt.sha256(confirmPassInput.getText().toString());
                                if (!confirmPasswordHash.equals(passwordHash)) {
                                    Toast.makeText(this, getResources().getText(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                UserDataManager.createUserData(getApplicationContext(), username, passwordHash);
                                try {
                                    SessionDataManager.putSessionUsername(this, username);
                                    SessionDataManager.putSessionPasswordHash(this, passwordHash);

                                    moveToHomeActivity();
                                }
                                catch (JSONException ex) {
                                    Log.e(TAG, "onCreate: Could not log in", ex);
                                    Toast.makeText(getApplicationContext(), "Unable to log in: JSON error", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }).setNegativeButton("Cancel", (v2, e) ->
                                    Toast.makeText(this, getResources().getText(R.string.signup_cancel), Toast.LENGTH_SHORT)
                                            .show()
                            ).show();
                        }
                        else {
                            if (!UserDataManager.userDataExists(getApplicationContext(), username)) {
                                Toast.makeText(getApplicationContext(), getResources().getText(R.string.username_not_exists), Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                            if (!UserDataManager.checkUserPassword(getApplicationContext(), username, passwordHash)) {
                                Toast.makeText(getApplicationContext(), getResources().getText(R.string.invalid_password), Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }

                            try {
                                SessionDataManager.putSessionUsername(this, username);
                                SessionDataManager.putSessionPasswordHash(this, passwordHash);

                                moveToHomeActivity();
                            }
                            catch (JSONException e) {
                                Log.e(TAG, "onCreate: Could not log in", e);
                                Toast.makeText(getApplicationContext(), "Unable to log in: JSON error", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    })
                    .show();
        };

        loginBtn.setOnClickListener(listener);
        signupBtn.setOnClickListener(listener);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("username"))
                usernameEditText.setText(savedInstanceState.getString("username"));
            if (savedInstanceState.containsKey("password"))
                passwordEditText.setText(savedInstanceState.getString("password"));
        }
    }

    private void moveToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        final TextInputLayout usernameInput = findViewById(R.id.usernameTextField);
        final TextInputLayout passwordInput = findViewById(R.id.passwordTextField);

        assert usernameInput.getEditText() != null;
        assert passwordInput.getEditText() != null;

        outState.putString("username", usernameInput.getEditText().getText().toString());
        outState.putString("password", passwordInput.getEditText().getText().toString());
    }
}