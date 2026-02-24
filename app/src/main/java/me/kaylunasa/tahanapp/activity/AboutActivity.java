package me.kaylunasa.tahanapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import me.kaylunasa.tahanapp.BuildConfig;
import me.kaylunasa.tahanapp.R;

public class AboutActivity extends TahanAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

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
                title.setText(getResources().getText(R.string.about_title));

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            toolbar.setNavigationOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());

            if (toolbar.getNavigationIcon() != null)
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.black));
        }

        TextView versionNumberView = findViewById(R.id.versionNumberDisplay);
        versionNumberView.setText(String.format(
                getResources().getText(R.string.version_format).toString(),
                BuildConfig.VERSION_NAME
        ));

        View cnphView = findViewById(R.id.contact_cnph);
        cnphView.setOnClickListener((v) -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:09814101224"));
            startActivity(dialIntent);
        });

        View daetMdrrmoView = findViewById(R.id.contact_daetMdrrmo);
        daetMdrrmoView.setOnClickListener((v) -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:09128555551"));
            startActivity(dialIntent);
        });

        View cnPdrrmoView1 = findViewById(R.id.contact_cnPdrrmo1);
        cnPdrrmoView1.setOnClickListener((v) -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:09985615388"));
            startActivity(dialIntent);
        });

        View cnPdrrmoView2 = findViewById(R.id.contact_cnPdrrmo2);
        cnPdrrmoView2.setOnClickListener((v) -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:09175839147"));
            startActivity(dialIntent);
        });
    }
}