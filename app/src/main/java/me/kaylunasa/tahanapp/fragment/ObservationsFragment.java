package me.kaylunasa.tahanapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import me.kaylunasa.tahanapp.R;

public class ObservationsFragment extends Fragment {
    private EditText editText;

    public String getContent() {
        if (editText != null)
            return editText.getText().toString();
        return "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_observations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.editText = view.findViewById(R.id.observationInput);
    }
}
