package me.kaylunasa.tahanapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import me.kaylunasa.tahanapp.R;

public class LearningHubModule5Activity extends TahanAppActivity {
    private static final String TAG = LearningHubModule5Activity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_learning_hub_module_5);

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
                title.setText(getResources().getText(R.string.asd_full));

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            toolbar.setNavigationOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());

            if (toolbar.getNavigationIcon() != null)
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.black));
        }

        findViewById(R.id.autismPh_phone).setOnClickListener((v) -> {
            String number = getResources().getText(R.string.lhub_m5_s2_d3_data).toString().replaceAll(" ", "");
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        });
        findViewById(R.id.autismPh_email).setOnClickListener((v) -> {
            String email = getResources().getText(R.string.lhub_m5_s2_d4_data).toString();
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            startActivity(intent);
        });
        findViewById(R.id.autismPh_url).setOnClickListener((v) -> {
            String url = getResources().getText(R.string.lhub_m5_s2_d5_data).toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        findViewById(R.id.secondaryAspLink).setOnClickListener((v) -> {
            String url = getResources().getText(R.string.lhub_m5_s3_d2_uri).toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        findViewById(R.id.secondaryAPinoyLink).setOnClickListener((v) -> {
            String url = getResources().getText(R.string.lhub_m5_s3_d4_uri).toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        findViewById(R.id.fb_link_1).setOnClickListener((v) -> {
            String url = getResources().getText(R.string.lhub_m5_s4_d2_uri).toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        findViewById(R.id.fb_link_2).setOnClickListener((v) -> {
            String url = getResources().getText(R.string.lhub_m5_s4_d3_uri).toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }
}