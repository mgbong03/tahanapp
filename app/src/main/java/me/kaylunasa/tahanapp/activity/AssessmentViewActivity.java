package me.kaylunasa.tahanapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.data.Assessment;
import me.kaylunasa.tahanapp.data.ChildProfile;
import me.kaylunasa.tahanapp.data.User;
import me.kaylunasa.tahanapp.util.SessionDataManager;
import me.kaylunasa.tahanapp.util.SettingsDataManager;
import me.kaylunasa.tahanapp.util.UserDataManager;

public class AssessmentViewActivity extends TahanAppActivity {
    private static final String TAG = AssessmentViewActivity.class.getSimpleName();

    private String profileName;
    private ChildProfile childProfile;
    private long timestamp;
    private Assessment assessment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assessment_view);

        Intent incomingIntent = getIntent();

        String intentProfileName = incomingIntent.getStringExtra("profileName");
        long intentTimestamp = incomingIntent.getLongExtra("timestamp", -1);

        if (intentProfileName != null)
            this.profileName = intentProfileName;
        this.timestamp = intentTimestamp;

        if (this.profileName == null) {
            Toast.makeText(this, "Profile required", Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            this.childProfile = User.fromJsonData(UserDataManager.getUserData(
                    this,
                    Objects.requireNonNull(SessionDataManager.getSessionUsername(this)),
                    Objects.requireNonNull(SessionDataManager.getSessionPasswordHash(this))
            )).getChildProfile(profileName);
        }
        catch (JSONException | NullPointerException e) {
            Log.e(TAG, "onCreate: Could not fetch session data", e);
            AlertDialog.Builder builder = new AlertDialog.Builder(AssessmentViewActivity.this);
            builder.setTitle("Error")
                    .setMessage("A fatal error has occurred.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        System.exit(0);
                    });
        }

        if (this.childProfile == null) {
            Toast.makeText(this, getResources().getText(R.string.profile_not_exists), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        this.assessment = this.childProfile.getAssessment(this.timestamp);
        if (this.assessment == null) {
            for (Assessment prevAssessment : childProfile.getAssessmentHistory())
                Log.d(TAG, "Assessment debug JSON: " + prevAssessment.toJsonData().toString());
            Toast.makeText(this, getResources().getText(R.string.assessment_not_exists), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
                title.setText(getResources().getText(R.string.assessment_history));

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            toolbar.setNavigationOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());

            if (toolbar.getNavigationIcon() != null)
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.black));
        }

        final int totalScore = assessment.getTotal();
        TextView totalScoreView = findViewById(R.id.totalScore);
        totalScoreView.setText(String.format(
                getResources().getText(R.string.score_format).toString(),
                totalScore
        ));

        final int scoreLevel = assessment.getPainLevel();
        TextView scoreLevelView = findViewById(R.id.scoreLevel);
        TextView interventionsView = findViewById(R.id.interventionsView);
        switch (scoreLevel) {
            case 1:
                scoreLevelView.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.level_1_tint)
                );
                scoreLevelView.setText(getResources().getText(R.string.score_level_1));
                interventionsView.setText(getResources().getText(R.string.interventions_level_1));
                break;
            case 2:
                scoreLevelView.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.level_2_tint)
                );
                scoreLevelView.setText(getResources().getText(R.string.score_level_2));
                interventionsView.setText(getResources().getText(R.string.interventions_level_2));
                break;
            case 3:
                scoreLevelView.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.level_3_tint)
                );
                scoreLevelView.setText(getResources().getText(R.string.score_level_3));
                interventionsView.setText(getResources().getText(R.string.interventions_level_3));
                break;
            default:
                scoreLevelView.setBackgroundTintList(
                        ContextCompat.getColorStateList(this, R.color.level_0_tint)
                );
                scoreLevelView.setText(getResources().getText(R.string.score_level_0));
                interventionsView.setText(getResources().getText(R.string.interventions_level_0));
        }

        TextView dateView = findViewById(R.id.assessmentDateTime);
        Date assessmentTimestamp = assessment.getTimestamp();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy - hh:mm a");
        dateView.setText(sdf.format(assessmentTimestamp));

        final int faceScore = assessment.getFaceScore();
        final int legsScore = assessment.getLegsScore();
        final int activityScore = assessment.getActivityScore();
        final int cryScore = assessment.getCryScore();
        final int consolabilityScore = assessment.getConsolabilityScore();

        TextView faceScoreView = findViewById(R.id.faceScore);
        faceScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                faceScore
        ));

        TextView legsScoreView = findViewById(R.id.legsScore);
        legsScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                legsScore
        ));

        TextView activityScoreView = findViewById(R.id.activityScore);
        activityScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                activityScore
        ));

        TextView cryScoreView = findViewById(R.id.cryScore);
        cryScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                cryScore
        ));

        TextView consolabilityScoreView = findViewById(R.id.consolabilityScore);
        consolabilityScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                consolabilityScore
        ));

        final List<String> painLocations = assessment.getPainLocations();
        final StringJoiner sj = new StringJoiner(", ");
        for (String painLocation : painLocations)
            sj.add(painLocation);
        final String painLocationsStr = sj.toString().isEmpty() ? "None" : sj.toString();

        TextView painLocationsView = findViewById(R.id.painLocationsView);
        painLocationsView.setText(painLocationsStr);

        final String observations = assessment.getComments();
        TextView observationsView = findViewById(R.id.observationsView);
        if (observations != null && !observations.isBlank())
            observationsView.setText(observations);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("profileName", this.profileName);
        outState.putLong("timestamp", this.timestamp);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.profileName = savedInstanceState.getString("profileName");
        this.timestamp = savedInstanceState.getLong("timestamp", -1);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void printForm(Locale locale) {
        String language = locale.getLanguage();
        Log.d(TAG, "printForm: locale is " + language);

        Toast.makeText(this, getResources().getText(R.string.print_started), Toast.LENGTH_SHORT).show();
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        if (Objects.equals(language, "fil"))
            webView.loadUrl("file:///android_asset/html/rflacc_form_fil.html");
        else
            webView.loadUrl("file:///android_asset/html/rflacc_form.html");

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                Date assessmentTimestamp = assessment.getTimestamp();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy - hh:mm a");
                setHTMLContent(view, "assessmentDateTime", sdf.format(assessmentTimestamp));

                setHTMLContent(view, "nameOfChild", childProfile.getName());
                setHTMLContent(view, "ageOfChild", Integer.toString(childProfile.getAge()));
                setHTMLContent(view, "genderOfChild", childProfile.getGender());
                setHTMLContent(view, "diagnosisOfChild", childProfile.getDiagnosis());

                setHTMLContent(view, "faceResult", Integer.toString(assessment.getFaceScore()));
                setHTMLContent(view, "legsResult", Integer.toString(assessment.getLegsScore()));
                setHTMLContent(view, "activityResult", Integer.toString(assessment.getActivityScore()));
                setHTMLContent(view, "cryResult", Integer.toString(assessment.getCryScore()));
                setHTMLContent(view, "consolabilityResult", Integer.toString(assessment.getConsolabilityScore()));
                setHTMLContent(view, "totalResult", Integer.toString(assessment.getTotal()));

                String scoreDesc = "N/A";
                switch (assessment.getPainLevel()) {
                    case 0:
                        scoreDesc = getResources().getText(R.string.score_level_0).toString();
                        break;
                    case 1:
                        scoreDesc = getResources().getText(R.string.score_level_1).toString();
                        break;
                    case 2:
                        scoreDesc = getResources().getText(R.string.score_level_2).toString();
                        break;
                    case 3:
                        scoreDesc = getResources().getText(R.string.score_level_3).toString();
                        break;
                }
                setHTMLContent(view, "scoreLevelDesc", scoreDesc);

                final List<String> painLocations = assessment.getPainLocations();
                final StringJoiner sj = new StringJoiner(", ");
                for (String painLocation : painLocations)
                    sj.add(painLocation);
                final String painLocationsStr = sj.toString().isEmpty() ? "None" : sj.toString();
                setHTMLContent(view, "painLocations", painLocationsStr);
                if (assessment.getComments() != null && !assessment.getComments().isBlank())
                    setHTMLContent(view, "observations", assessment.getComments());

                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("rFLACC_PDF_" + assessmentTimestamp.getTime());
                PrintAttributes attributes = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(new PrintAttributes.Resolution("high_res", "print_service", 600, 600))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build();

                String jobName = getExternalFilesDir(null) + "/rFLACC_PDF_" + assessmentTimestamp.getTime() + ".pdf";
                printManager.print(
                        jobName,
                        printAdapter,
                        attributes
                );
            }
        });
    }

    private void setHTMLContent(WebView webView, String id, String innerHTML) {
        innerHTML = Jsoup.clean(innerHTML, Safelist.simpleText());
        innerHTML = innerHTML.replaceAll("'", "\\\\'");

        String js = "javascript:(function() {" +
                "document.getElementById('" + id + "').innerHTML = '" + innerHTML + "';" +
                "})()";

        webView.evaluateJavascript(js, value -> {});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_printable, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_print) {
            String language = SettingsDataManager.getLanguage(this);
            if (language != null)
                printForm(Locale.forLanguageTag(language));
            else
                printForm(Locale.getDefault());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}