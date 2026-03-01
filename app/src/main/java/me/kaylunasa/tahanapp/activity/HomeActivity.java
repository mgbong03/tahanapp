package me.kaylunasa.tahanapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.adapter.ChildProfileCardAdapter;
import me.kaylunasa.tahanapp.data.ChildProfile;
import me.kaylunasa.tahanapp.data.User;
import me.kaylunasa.tahanapp.fragment.AddProfileFragment;
import me.kaylunasa.tahanapp.util.SessionDataManager;
import me.kaylunasa.tahanapp.util.UserDataManager;

public class HomeActivity extends TahanAppActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private boolean shouldRecreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final RecyclerView profilesContainer = findViewById(R.id.profiles_container);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                this, RecyclerView.VERTICAL, false
        );
        profilesContainer.setLayoutManager(linearLayoutManager);

        try {
            List<ChildProfile> childProfiles = User.fromJsonData(UserDataManager.getUserData(
                    this,
                    Objects.requireNonNull(SessionDataManager.getSessionUsername(this)),
                    Objects.requireNonNull(SessionDataManager.getSessionPasswordHash(this))
            )).getChildProfiles();
            ChildProfileCardAdapter cardAdapter = new ChildProfileCardAdapter(childProfiles, () -> this.shouldRecreate = true);
            profilesContainer.setAdapter(cardAdapter);
        }
        catch (JSONException | NullPointerException e) {
            Log.e(TAG, "onCreate: Could not fetch session data", e);
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("Error")
                    .setMessage("A fatal error has occurred.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        System.exit(0);
                    });
        }

        if (profilesContainer.getAdapter() == null || profilesContainer.getAdapter().getItemCount() == 0) {
            profilesContainer.setVisibility(View.GONE);
            findViewById(R.id.profiles_no_content).setVisibility(View.VISIBLE);
        }

        Button howToUseBtn = findViewById(R.id.howToUseBtn);
        howToUseBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, HowToUseActivity.class);
            startActivity(intent);
        });

        Button faqsBtn = findViewById(R.id.faqsBtn);
        faqsBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, FaqsActivity.class);
            startActivity(intent);
        });

        Button aboutBtn = findViewById(R.id.aboutBtn);
        aboutBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });

        Button asdButton = findViewById(R.id.asdBtn);
        asdButton.setOnClickListener((v) -> {
            Intent intent = new Intent(this, AsdActivity.class);
            startActivity(intent);
        });

        Button settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        FloatingActionButton addButton = findViewById(R.id.addButtonFloating);
        addButton.setOnClickListener((v) -> {
            AddProfileFragment profileFragment = new AddProfileFragment();
            profileFragment.show(getSupportFragmentManager(), "AddProfileFragment");
        });

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            recreate();
            swipeRefreshLayout.setRefreshing(false);
        });
        swipeRefreshLayout.setOnChildScrollUpCallback((parent, child) ->
                profilesContainer.canScrollVertically(-1));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldRecreate) {
            shouldRecreate = false;
            recreate();
        }
    }
}