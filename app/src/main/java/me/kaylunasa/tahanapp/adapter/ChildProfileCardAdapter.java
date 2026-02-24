package me.kaylunasa.tahanapp.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.activity.ChildViewActivity;
import me.kaylunasa.tahanapp.data.Assessment;
import me.kaylunasa.tahanapp.data.ChildProfile;

public class ChildProfileCardAdapter extends RecyclerView.Adapter<ChildProfileCardAdapter.ChildProfileViewHolder> {
    private static final String TAG = ChildProfileCardAdapter.class.getSimpleName();

    private final List<ChildProfile> items;
    private final Runnable callback;

    public ChildProfileCardAdapter(List<ChildProfile> items, Runnable callback) {
        this.items = items;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ChildProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_profile, parent, false);
        return new ChildProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildProfileViewHolder holder, int pos) {
        ChildProfile childProfile = items.get(pos);

        if (childProfile.getImage() != null)
            holder.imageDisplay.setImageBitmap(childProfile.getImage());
        holder.nameDisplay.setText(childProfile.getName());
        holder.ageDisplay.setText(String.format(
                holder.itemView.getContext().getResources().getText(R.string.years_old).toString(),
                childProfile.getAge()
        ));
        Date lastAssessmentDate = null;
        for (Assessment assessment : childProfile.getAssessmentHistory()) if (lastAssessmentDate == null || lastAssessmentDate.getTime() < assessment.getTimestamp().getTime())
            lastAssessmentDate = assessment.getTimestamp();
        String lastAssessmentStr = holder.itemView.getContext().getResources().getText(R.string.never_assessed).toString();
        if (lastAssessmentDate != null) {
            lastAssessmentStr = DateUtils.getRelativeTimeSpanString(
                    lastAssessmentDate.getTime(),
                    System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS
            ).toString();
        }

        holder.lastAssessmentDisplay.setText(String.format(
                holder.itemView.getContext().getResources().getText(R.string.last_assessment_format).toString(),
                lastAssessmentStr
        ));
        holder.view.setOnClickListener((v) -> {
            Log.d(TAG, "onBindViewHolder: attempting to access child '" + childProfile.getName() + "' at index " + pos + " for current user");
            Intent intent = new Intent(v.getContext(), ChildViewActivity.class);
            intent.putExtra("profileName", childProfile.getName());
            v.getContext().startActivity(intent);
            if (this.callback != null)
                this.callback.run();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ChildProfileViewHolder extends RecyclerView.ViewHolder {
        View view;

        ShapeableImageView imageDisplay;
        TextView nameDisplay;
        TextView ageDisplay;
        TextView lastAssessmentDisplay;

        public ChildProfileViewHolder(@NonNull View view) {
            super(view);
            this.view = view;

            this.imageDisplay = view.findViewById(R.id.imageView);
            this.nameDisplay = view.findViewById(R.id.childName);
            this.ageDisplay = view.findViewById(R.id.childAge);
            this.lastAssessmentDisplay = view.findViewById(R.id.lastAssessment);
        }
    }
}
