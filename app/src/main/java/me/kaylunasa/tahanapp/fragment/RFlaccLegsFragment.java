package me.kaylunasa.tahanapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.kaylunasa.tahanapp.R;

public class RFlaccLegsFragment extends RFlaccFragment {
    private static final String TAG = RFlaccLegsFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_r_flacc_legs, container, false);
    }
}