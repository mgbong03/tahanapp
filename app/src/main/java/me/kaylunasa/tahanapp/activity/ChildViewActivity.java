package me.kaylunasa.tahanapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.adapter.AssessmentCardAdapter;
import me.kaylunasa.tahanapp.data.Assessment;
import me.kaylunasa.tahanapp.data.ChildProfile;
import me.kaylunasa.tahanapp.data.User;
import me.kaylunasa.tahanapp.fragment.AddProfileFragment;
import me.kaylunasa.tahanapp.fragment.EditProfileFragment;
import me.kaylunasa.tahanapp.notification.NotificationWorker;
import me.kaylunasa.tahanapp.util.SessionDataManager;
import me.kaylunasa.tahanapp.util.UserDataManager;

public class ChildViewActivity extends TahanAppActivity {
    private static final String TAG = ChildViewActivity.class.getSimpleName();

    private String profileName;
    private ChildProfile childProfile;

    private ActivityResultLauncher<Intent> assessmentFormLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent incomingIntent = getIntent();

        String intentProfileName = incomingIntent.getStringExtra("profileName");
        if (intentProfileName != null)
            this.profileName = intentProfileName;

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
            AlertDialog.Builder builder = new AlertDialog.Builder(ChildViewActivity.this);
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

        this.assessmentFormLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null)
                            return;

                        Log.d(TAG, "assessmentFormLauncher: Received data from assessment");

                        int faceScore = data.getIntExtra("faceScore", 0);
                        int legsScore = data.getIntExtra("legsScore", 0);
                        int activityScore = data.getIntExtra("activityScore", 0);
                        int cryScore = data.getIntExtra("cryScore", 0);
                        int consolabilityScore = data.getIntExtra("consolabilityScore", 0);
                        boolean isNotify = data.getBooleanExtra("isNotify", false);

                        List<String> painLocations = List.of(Objects.requireNonNullElse(data.getStringArrayExtra("painLocations"), new String[]{}));
                        String comments = data.getStringExtra("comments");

                        Assessment.Draft assessmentDraft = new Assessment.Draft();
                        assessmentDraft.setFaceScore(faceScore);
                        assessmentDraft.setLegsScore(legsScore);
                        assessmentDraft.setActivityScore(activityScore);
                        assessmentDraft.setCryScore(cryScore);
                        assessmentDraft.setConsolabilityScore(consolabilityScore);
                        for (String painLocation : painLocations)
                            assessmentDraft.addPainLocation(painLocation);
                        assessmentDraft.setComments(comments);

                        Assessment assessment = assessmentDraft.finalizeDraft();

                        ChildProfile.Draft profileDraft = new ChildProfile.Draft(childProfile);
                        profileDraft.addAssessment(assessment);
                        childProfile = profileDraft.finalizeDraft();

                        try {
                            String username = SessionDataManager.getSessionUsername(this);
                            String passwordHash = SessionDataManager.getSessionPasswordHash(this);

                            assert username != null;
                            assert passwordHash != null;
                            User user = User.fromJsonData(UserDataManager.getUserData(this, username, passwordHash));
                            User.Draft editUser = new User.Draft(user);
                            editUser.removeChildProfile(childProfile.getName());
                            editUser.addChildProfile(childProfile);
                            user = editUser.finalizeDraft();
                            UserDataManager.putUserData(
                                    this,
                                    username,
                                    passwordHash,
                                    user.toJsonData()
                            );
                            Toast.makeText(this, getResources().getText(R.string.assessment_saved), Toast.LENGTH_SHORT).show();

                            if (isNotify) {
                                Log.d(TAG, "onCreate: Scheduling notification");
                                Data workData = new Data.Builder()
                                        .putString("childName", childProfile.getName())
                                        .putInt("notifId", childProfile.getName().hashCode())
                                        .build();
                                OneTimeWorkRequest notifWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                                        .setInitialDelay(8, TimeUnit.HOURS)
                                        .setInputData(workData)
                                        .build();

                                WorkManager.getInstance(this).enqueue(notifWork);
                            }

                            recreate();
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
                    }
                }
        );

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_view);

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
                title.setText(getResources().getText(R.string.profile));

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            toolbar.setNavigationOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());

            if (toolbar.getNavigationIcon() != null)
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.black));
        }

        ShapeableImageView profileImageView = findViewById(R.id.profileImage);
        if (childProfile.getImage() != null)
            profileImageView.setImageBitmap(childProfile.getImage());

        TextView profileName = findViewById(R.id.childName);
        profileName.setText(childProfile.getName());

        TextView profileAge = findViewById(R.id.childAge);
        profileAge.setText(String.format(
                getResources().getText(R.string.years_old).toString(),
                childProfile.getAge()
        ));

        TextView profileGender = findViewById(R.id.childGender);
        profileGender.setText(String.format(
                getResources().getText(R.string.gender_display).toString(),
                childProfile.getGender()
        ));

        TextView profileDiagnosis = findViewById(R.id.childDiagnosis);
        profileDiagnosis.setText(String.format(
                getResources().getText(R.string.diagnosis_display).toString(),
                childProfile.getDiagnosis()
        ));

        RecyclerView assessmentView = findViewById(R.id.latestAssessments);
        assessmentView.setLayoutManager(new LinearLayoutManager(this));
        List<Assessment> assessments = new ArrayList<>(childProfile.getAssessmentHistory());
        assessments.sort(Comparator.comparing(Assessment::getTimestamp).reversed());
        if (assessments.isEmpty()) {
            findViewById(R.id.noAssessmentsFound).setVisibility(View.VISIBLE);
        }
        else {
            List<Assessment> latestAssessments = assessments.subList(0, Math.min(3, assessments.size()));
            AssessmentCardAdapter assessmentCardAdapter = new AssessmentCardAdapter(childProfile, latestAssessments);
            assessmentView.setAdapter(assessmentCardAdapter);
        }
        TextView historyCounter = findViewById(R.id.historyCounter);
        historyCounter.setText(String.format(
                getResources().getText(R.string.assessment_count).toString(),
                assessments.size()
        ));

        Button startAssessmentBtn = findViewById(R.id.startAssessmentBtn);
        startAssessmentBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, CreateAssessmentActivity.class);
            assessmentFormLauncher.launch(intent);
            recreate();
        });

        Button editProfileBtn = findViewById(R.id.editProfileBtn);
        editProfileBtn.setOnClickListener((v) -> {
            EditProfileFragment profileFragment = new EditProfileFragment(childProfile);
            profileFragment.show(getSupportFragmentManager(), "EditProfileFragment");
        });

        Button deleteProfileBtn = findViewById(R.id.deleteProfileBtn);
        deleteProfileBtn.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getText(R.string.delete_profile))
                    .setMessage(getResources().getText(R.string.delete_profile_confirm))
                    .setPositiveButton("Delete", (v2, e) -> {
                        try {
                            String username = SessionDataManager.getSessionUsername(this);
                            String passwordHash = SessionDataManager.getSessionPasswordHash(this);

                            if (username == null || passwordHash == null ||
                                    !UserDataManager.checkUserPassword(this, username, passwordHash))
                                throw new IllegalStateException();

                            User user = User.fromJsonData(UserDataManager.getUserData(this, username, passwordHash));
                            User.Draft editUser = new User.Draft(user);
                            editUser.removeChildProfile(childProfile.getName());
                            user = editUser.finalizeDraft();
                            UserDataManager.putUserData(
                                    this,
                                    username,
                                    passwordHash,
                                    user.toJsonData()
                            );
                            Toast.makeText(this, getResources().getString(R.string.profile_remove_success), Toast.LENGTH_SHORT).show();
                        }
                        catch (IllegalStateException | JSONException ex) {
                            Toast.makeText(this, getResources().getText(R.string.logged_out_force), Toast.LENGTH_SHORT).show();
                            try {
                                SessionDataManager.resetSessionData(this);
                            }
                            catch (IOException exc) {
                                Toast.makeText(this, "Could not reset stored data.", Toast.LENGTH_SHORT)
                                        .show();
                                System.exit(0);
                                return;
                            }

                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            return;
                        }

                        finish();
                    })
                    .setNegativeButton("Cancel", (v2, e) -> Toast.makeText(
                            this,
                            getResources().getText(R.string.delete_profile_cancel),
                            Toast.LENGTH_SHORT
                    ).show())
                    .show();
        });

        CardView historyBtn = findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, AssessmentHistoryActivity.class);
            intent.putExtra("profileName", this.profileName);
            startActivity(intent);
        });
        CardView faqsBtn = findViewById(R.id.faqsBtn);
        faqsBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, FaqsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("profileName", this.profileName);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.profileName = savedInstanceState.getString("profileName");
    }
}