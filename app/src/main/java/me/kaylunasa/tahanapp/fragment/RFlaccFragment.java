package me.kaylunasa.tahanapp.fragment;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import me.kaylunasa.tahanapp.R;

public abstract class RFlaccFragment extends Fragment {
    protected int selectedValue = -1;

    protected LinearLayout score0View;
    protected LinearLayout score1View;
    protected LinearLayout score2View;

    public int getSelectedValue() {
        return this.selectedValue;
    }

    protected void setSelectedValue(int selectedValue) {
        this.selectedValue = selectedValue;
        updateRespectiveViews();
    }

    private void setSelected(LinearLayout view) {
        view.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.layout_bg_selected, requireContext().getTheme()));
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.lightinformatic_tint)
        ));
        int padding = Math.round(10 * requireContext().getResources().getDisplayMetrics().density);
        view.setPadding(padding, padding, padding, padding);
    }

    private void setUnselected(LinearLayout view) {
        view.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.layout_bg, requireContext().getTheme()));
        ViewCompat.setBackgroundTintList(view, null);
        view.setPadding(
                view.getPaddingLeft(),
                view.getPaddingTop(),
                view.getPaddingRight(),
                view.getPaddingBottom()
        );
        int padding = Math.round(10 * requireContext().getResources().getDisplayMetrics().density);
        view.setPadding(padding, padding, padding, padding);
    }

    protected void updateRespectiveViews() {
        if (getContext() == null)
            return;

        setUnselected(score0View);
        setUnselected(score1View);
        setUnselected(score2View);

        switch (this.selectedValue) {
            case 0:
                setSelected(score0View);
                break;
            case 1:
                setSelected(score1View);
                break;
            case 2:
                setSelected(score2View);
                break;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.score0View = view.findViewById(R.id.select_score_0);
        this.score1View = view.findViewById(R.id.select_score_1);
        this.score2View = view.findViewById(R.id.select_score_2);

        updateRespectiveViews();

        score0View.setOnClickListener((v) -> setSelectedValue(0));
        score1View.setOnClickListener((v) -> setSelectedValue(1));
        score2View.setOnClickListener((v) -> setSelectedValue(2));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedValue", this.selectedValue);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            this.selectedValue = savedInstanceState.getInt("selectedValue", -1);
    }
}
