package me.kaylunasa.tahanapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.adapter.AssessmentCardAdapter;
import me.kaylunasa.tahanapp.data.Assessment;
import me.kaylunasa.tahanapp.data.ChildProfile;
import me.kaylunasa.tahanapp.data.User;
import me.kaylunasa.tahanapp.util.SessionDataManager;
import me.kaylunasa.tahanapp.util.UserDataManager;

public class AssessmentHistoryActivity extends TahanAppActivity {
    private static final String TAG = AssessmentHistoryActivity.class.getSimpleName();

    private String profileName;
    private ChildProfile childProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(AssessmentHistoryActivity.this);
            builder.setTitle("Error")
                    .setMessage("A fatal error has occurred.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        System.exit(0);
                    });
        }
        if (this.childProfile == null) {
            Toast.makeText(this, "Could not access profile", Toast.LENGTH_SHORT).show();
            finish();
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assessment_history);

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

        RecyclerView assessmentView = findViewById(R.id.assessmentsView);
        assessmentView.setLayoutManager(new LinearLayoutManager(this));
        List<Assessment> assessments = new ArrayList<>(childProfile.getAssessmentHistory());
        assessments.sort(Comparator.comparing(Assessment::getTimestamp).reversed());
        if (assessments.isEmpty()) {
            findViewById(R.id.noAssessmentsFound).setVisibility(View.VISIBLE);
            findViewById(R.id.trendsContainer).setVisibility(View.GONE);
        }
        else {
            AssessmentCardAdapter assessmentCardAdapter = new AssessmentCardAdapter(childProfile, assessments);
            assessmentView.setAdapter(assessmentCardAdapter);
            renderAssessmentChart();
        }
    }

    private void renderAssessmentChart() {
        LineChart lineChart = findViewById(R.id.trendsChart);
        long minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;

        List<Pair<Date, Integer>> chartData = new ArrayList<>();
        for (Assessment assessment : childProfile.getAssessmentHistory()) {
            chartData.add(new Pair<>(assessment.getTimestamp(), assessment.getTotal()));
            if (minTimestamp > assessment.getTimestamp().getTime())
                minTimestamp = assessment.getTimestamp().getTime();
            if (maxTimestamp < assessment.getTimestamp().getTime())
                maxTimestamp = assessment.getTimestamp().getTime();
        }

        long cutoff = maxTimestamp - 604800000L;
        if (minTimestamp < cutoff)
            minTimestamp = cutoff;

        float granularity = (float) (maxTimestamp - minTimestamp) / 3.1f;

        List<Entry> entries = new ArrayList<>();
        for (Pair<Date, Integer> pair : chartData) if (pair.first.getTime() > cutoff)
            entries.add(new Entry(pair.first.getTime(), pair.second));

        LineDataSet dataSet = new LineDataSet(entries, getResources().getText(R.string.pain_trends).toString());
        dataSet.setLineWidth(2f);
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MMM d h:mm a");
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(granularity);
        lineChart.getXAxis().setAxisMinimum(minTimestamp);
        lineChart.getXAxis().setAxisMaximum(maxTimestamp);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(10f);
        lineChart.getAxisLeft().setGranularity(1f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        lineChart.invalidate();
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