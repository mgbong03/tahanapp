package me.kaylunasa.tahanapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.kaylunasa.tahanapp.R;

public class RFlaccCryFragment extends RFlaccFragment {
    private static final String TAG = RFlaccCryFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_r_flacc_cry, container, false);
    }
}