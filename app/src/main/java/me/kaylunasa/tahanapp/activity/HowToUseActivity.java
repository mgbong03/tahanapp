package me.kaylunasa.tahanapp.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import me.kaylunasa.tahanapp.R;

public class HowToUseActivity extends TahanAppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_how_to_use);

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
                title.setText(getResources().getText(R.string.how_to_use));

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            toolbar.setNavigationOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());

            if (toolbar.getNavigationIcon() != null)
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.black));
        }
    }
}