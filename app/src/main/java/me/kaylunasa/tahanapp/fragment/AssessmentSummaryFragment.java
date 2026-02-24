package me.kaylunasa.tahanapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.List;

import me.kaylunasa.tahanapp.R;

public class AssessmentSummaryFragment extends Fragment {
    private final int faceScore;
    private final int legsScore;
    private final int activityScore;
    private final int cryScore;
    private final int consolabilityScore;

    private final List<String> painLocations;
    private final String comments;

    public AssessmentSummaryFragment(
            int faceScore, int legsScore, int activityScore,
            int cryScore, int consolabilityScore,
            List<String> painLocations, String comments
    ) {
        this.faceScore = faceScore;
        this.legsScore = legsScore;
        this.activityScore = activityScore;
        this.cryScore = cryScore;
        this.consolabilityScore = consolabilityScore;

        this.painLocations = List.copyOf(painLocations);
        this.comments = comments;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assessment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        final int totalScore = this.faceScore + this.legsScore + this.activityScore + this.cryScore + this.consolabilityScore;

        TextView totalScoreView = view.findViewById(R.id.totalScore);
        totalScoreView.setText(String.format(
                getResources().getText(R.string.score_format).toString(),
                totalScore
        ));

        TextView scoreLevelView = view.findViewById(R.id.scoreLevel);
        TextView interventionsView = view.findViewById(R.id.interventionsView);
        if (totalScore == 0) {
            scoreLevelView.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.level_0_tint)
            );
            scoreLevelView.setText(getResources().getText(R.string.score_level_0));
            interventionsView.setText(getResources().getText(R.string.interventions_level_0));
        }
        else if (totalScore < 4) {
            scoreLevelView.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.level_1_tint)
            );
            scoreLevelView.setText(getResources().getText(R.string.score_level_1));
            interventionsView.setText(getResources().getText(R.string.interventions_level_1));
        }
        else if (totalScore < 7) {
            scoreLevelView.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.level_2_tint)
            );
            scoreLevelView.setText(getResources().getText(R.string.score_level_2));
            interventionsView.setText(getResources().getText(R.string.interventions_level_2));
        }
        else {
            scoreLevelView.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.level_3_tint)
            );
            scoreLevelView.setText(getResources().getText(R.string.score_level_3));
            interventionsView.setText(getResources().getText(R.string.interventions_level_3));
        }

        TextView faceScoreView = view.findViewById(R.id.faceScore);
        faceScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                faceScore
        ));

        TextView legsScoreView = view.findViewById(R.id.legsScore);
        legsScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                legsScore
        ));

        TextView activityScoreView = view.findViewById(R.id.activityScore);
        activityScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                activityScore
        ));

        TextView cryScoreView = view.findViewById(R.id.cryScore);
        cryScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                cryScore
        ));

        TextView consolabilityScoreView = view.findViewById(R.id.consolabilityScore);
        consolabilityScoreView.setText(String.format(
                getResources().getText(R.string.score_format_sub).toString(),
                consolabilityScore
        ));
    }
}
