package me.kaylunasa.tahanapp.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import me.kaylunasa.tahanapp.R;

public class RFlaccFaceFragment extends RFlaccFragment {
    private static final String TAG = RFlaccFaceFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_r_flacc_face, container, false);
    }
}