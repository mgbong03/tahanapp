package me.kaylunasa.tahanapp.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.activity.AssessmentViewActivity;
import me.kaylunasa.tahanapp.activity.ChildViewActivity;
import me.kaylunasa.tahanapp.data.Assessment;
import me.kaylunasa.tahanapp.data.ChildProfile;

public class AssessmentCardAdapter extends RecyclerView.Adapter<AssessmentCardAdapter.AssessmentViewHolder> {
    private static final String TAG = AssessmentCardAdapter.class.getSimpleName();

    private final List<Assessment> items;
    private final ChildProfile childProfile;

    public AssessmentCardAdapter(ChildProfile childProfile, List<Assessment> items) {
        this.childProfile = childProfile;
        this.items = items;
    }

    @NonNull
    @Override
    public AssessmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_assessment, parent, false);
        return new AssessmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssessmentViewHolder holder, int pos) {
        Assessment assessment = items.get(pos);
        int painLevel = assessment.getPainLevel();

        Date assessmentTimestamp = assessment.getTimestamp();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy - hh:mm a");
        holder.assessmentDate.setText(sdf.format(assessmentTimestamp));

        String assessmentLevelStr;
        ColorStateList assessmentLevelClr;
        switch (painLevel) {
            case 1:
                assessmentLevelClr = ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.level_1_tint);
                assessmentLevelStr = holder.itemView.getContext().getResources().getText(R.string.score_level_1).toString();
                break;
            case 2:
                assessmentLevelClr = ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.level_2_tint);
                assessmentLevelStr = holder.itemView.getContext().getResources().getText(R.string.score_level_2).toString();
                break;
            case 3:
                assessmentLevelClr = ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.level_3_tint);
                assessmentLevelStr = holder.itemView.getContext().getResources().getText(R.string.score_level_3).toString();
                break;
            default:
                assessmentLevelClr = ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.level_0_tint);
                assessmentLevelStr = holder.itemView.getContext().getResources().getText(R.string.score_level_0).toString();
                break;
        }

        holder.assessmentLevelColor.setBackgroundTintList(assessmentLevelClr);
        holder.assessmentLevelText.setText(assessmentLevelStr);
        holder.assessmentScore.setText(String.format(
                holder.itemView.getContext().getResources().getText(R.string.score_format).toString(),
                assessment.getTotal()
        ));

        holder.view.setOnClickListener((v) -> {
            Log.d(TAG, "onBindViewHolder: attempting to access assessment with timestamp " + assessmentTimestamp.getTime() +
                    " for child '" + childProfile.getName() + "' for current user");
            Intent intent = new Intent(v.getContext(), AssessmentViewActivity.class);
            intent.putExtra("profileName", childProfile.getName());
            intent.putExtra("timestamp", assessmentTimestamp.getTime());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class AssessmentViewHolder extends RecyclerView.ViewHolder {
        View view;

        TextView assessmentDate;
        FrameLayout assessmentLevelColor;
        TextView assessmentLevelText;
        TextView assessmentScore;

        public AssessmentViewHolder(@NonNull View view) {
            super(view);
            this.view = view;

            this.assessmentDate = view.findViewById(R.id.assessmentDate);
            this.assessmentLevelColor = view.findViewById(R.id.assessmentLevelColor);
            this.assessmentLevelText = view.findViewById(R.id.assessmentLevelText);
            this.assessmentScore = view.findViewById(R.id.assessmentScore);
        }
    }
}
