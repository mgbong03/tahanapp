package me.kaylunasa.tahanapp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.fragment.AssessmentSummaryFragment;
import me.kaylunasa.tahanapp.fragment.ObservationsFragment;
import me.kaylunasa.tahanapp.fragment.PainLocationFragment;
import me.kaylunasa.tahanapp.fragment.PreliminaryFragment;
import me.kaylunasa.tahanapp.fragment.RFlaccActivityFragment;
import me.kaylunasa.tahanapp.fragment.RFlaccConsolabilityFragment;
import me.kaylunasa.tahanapp.fragment.RFlaccCryFragment;
import me.kaylunasa.tahanapp.fragment.RFlaccFaceFragment;
import me.kaylunasa.tahanapp.fragment.RFlaccLegsFragment;
import me.kaylunasa.tahanapp.util.SettingsDataManager;

public class CreateAssessmentActivity extends TahanAppActivity {
    private static final String TAG = CreateAssessmentActivity.class.getSimpleName();

    private int faceScore = 0;
    private int legsScore = 0;
    private int activityScore = 0;
    private int cryScore = 0;
    private int consolabilityScore = 0;

    private final Map<Class<? extends Fragment>, String> audioAssetFiles = Map.of(
            PreliminaryFragment.class, "rflacc_preliminary.mp3",
            RFlaccFaceFragment.class, "rflacc_face.mp3",
            RFlaccLegsFragment.class, "rflacc_legs.mp3",
            RFlaccActivityFragment.class, "rflacc_activity.mp3",
            RFlaccCryFragment.class, "rflacc_cry.mp3",
            RFlaccConsolabilityFragment.class, "rflacc_consolability.mp3"
    );

    private List<String> painLocations = List.of();
    private String comments = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_assessment);

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
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                TextView titleView = toolbar.findViewById(R.id.toolbarTitle);
                titleView.setText(getResources().getText(R.string.create_assessment));
            }

            toolbar.setNavigationOnClickListener((v) -> getOnBackPressedDispatcher().onBackPressed());

            if (toolbar.getNavigationIcon() != null)
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.black));
        }

        navigateToFragment(new PreliminaryFragment());

        Button nextBtn = findViewById(R.id.nextBtn);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                stopCurrentSound();
                
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (currentFragment instanceof PreliminaryFragment) {
                    setEnabled(false);
                    setResult(RESULT_CANCELED);
                    finish();
                }
                else {
                    getSupportFragmentManager().popBackStackImmediate();
                    updateToolbarTitle(getSupportFragmentManager().findFragmentById(R.id.fragmentContainer));
                    nextBtn.setText(getResources().getText(R.string.next));
                }
            }
        });

        nextBtn.setOnClickListener((v) -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (currentFragment instanceof PreliminaryFragment)
                navigateToFragment(new RFlaccFaceFragment());

            if (currentFragment instanceof RFlaccFaceFragment) {
                this.faceScore = ((RFlaccFaceFragment) currentFragment).getSelectedValue();
                if (this.faceScore == -1) {
                    Toast.makeText(this, getResources().getText(R.string.score_needed), Toast.LENGTH_SHORT).show();
                    return;
                }
                navigateToFragment(new RFlaccLegsFragment());
            }

            if (currentFragment instanceof RFlaccLegsFragment) {
                this.legsScore = ((RFlaccLegsFragment) currentFragment).getSelectedValue();
                if (this.legsScore == -1) {
                    Toast.makeText(this, getResources().getText(R.string.score_needed), Toast.LENGTH_SHORT).show();
                    return;
                }
                navigateToFragment(new RFlaccActivityFragment());
            }

            if (currentFragment instanceof RFlaccActivityFragment) {
                this.activityScore = ((RFlaccActivityFragment) currentFragment).getSelectedValue();
                if (this.activityScore == -1) {
                    Toast.makeText(this, getResources().getText(R.string.score_needed), Toast.LENGTH_SHORT).show();
                    return;
                }
                navigateToFragment(new RFlaccCryFragment());
            }

            if (currentFragment instanceof RFlaccCryFragment) {
                this.cryScore = ((RFlaccCryFragment) currentFragment).getSelectedValue();
                if (this.cryScore == -1) {
                    Toast.makeText(this, getResources().getText(R.string.score_needed), Toast.LENGTH_SHORT).show();
                    return;
                }
                navigateToFragment(new RFlaccConsolabilityFragment());
            }

            if (currentFragment instanceof RFlaccConsolabilityFragment) {
                this.consolabilityScore = ((RFlaccConsolabilityFragment) currentFragment).getSelectedValue();
                if (this.consolabilityScore == -1) {
                    Toast.makeText(this, getResources().getText(R.string.score_needed), Toast.LENGTH_SHORT).show();
                    return;
                }
                navigateToFragment(new PainLocationFragment());
            }

            if (currentFragment instanceof PainLocationFragment) {
                this.painLocations = ((PainLocationFragment) currentFragment).getSelectedAreas();
                navigateToFragment(new ObservationsFragment());
            }

            if (currentFragment instanceof ObservationsFragment) {
                this.comments = ((ObservationsFragment) currentFragment).getContent();
                nextBtn.setText(getResources().getText(R.string.finish));
                navigateToFragment(new AssessmentSummaryFragment(
                        this.faceScore,
                        this.legsScore,
                        this.activityScore,
                        this.cryScore,
                        this.consolabilityScore,
                        this.painLocations,
                        this.comments
                ));
            }

            if (currentFragment instanceof AssessmentSummaryFragment) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getText(R.string.save_summary_title))
                        .setMessage(getResources().getText(R.string.save_summary_prompt))
                        .setPositiveButton("OK", (v2, e) -> {
                            String[] painLocationsArr = new String[this.painLocations.size()];
                            for (int i = 0; i < painLocationsArr.length; i++)
                                painLocationsArr[i] = this.painLocations.get(i);

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("faceScore", this.faceScore);
                            resultIntent.putExtra("legsScore", this.legsScore);
                            resultIntent.putExtra("activityScore", this.activityScore);
                            resultIntent.putExtra("cryScore", this.cryScore);
                            resultIntent.putExtra("consolabilityScore", this.consolabilityScore);
                            resultIntent.putExtra("painLocations", painLocationsArr);
                            resultIntent.putExtra("comments", this.comments);
                            setResult(RESULT_OK, resultIntent);

                            finish();
                        })
                        .setNegativeButton("Cancel", (v2, e) -> {})
                        .show();
            }
        });
    }

    private void navigateToFragment(Fragment fragment) {
        stopCurrentSound();
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        getSupportFragmentManager().executePendingTransactions();
        invalidateMenu();

        updateToolbarTitle(fragment);
    }

    private void updateToolbarTitle(Fragment fragment) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            TextView titleView = toolbar.findViewById(R.id.toolbarTitle);

            if (fragment instanceof PreliminaryFragment)
                titleView.setText(getResources().getText(R.string.preliminary));
            if (fragment instanceof RFlaccFaceFragment)
                titleView.setText(getResources().getText(R.string.create_assessment_step_1));
            else if (fragment instanceof RFlaccLegsFragment)
                titleView.setText(getResources().getText(R.string.create_assessment_step_2));
            else if (fragment instanceof RFlaccActivityFragment)
                titleView.setText(getResources().getText(R.string.create_assessment_step_3));
            else if (fragment instanceof RFlaccCryFragment)
                titleView.setText(getResources().getText(R.string.create_assessment_step_4));
            else if (fragment instanceof RFlaccConsolabilityFragment)
                titleView.setText(getResources().getText(R.string.create_assessment_step_5));
            else if (fragment instanceof PainLocationFragment)
                titleView.setText(getResources().getText(R.string.pain_location));
            else if (fragment instanceof ObservationsFragment)
                titleView.setText(getResources().getText(R.string.observations));
            else if (fragment instanceof AssessmentSummaryFragment)
                titleView.setText(getResources().getText(R.string.assessment_summary));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_has_sound, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem soundItem = menu.findItem(R.id.menu_playSound);

        soundIcon: if (soundItem != null) {
            soundItem.setVisible(false);
            if (!Objects.equals(SettingsDataManager.getLanguage(this), "fil") &&
                    !SettingsDataManager.getForceNarrations(this))
                break soundIcon;

            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (currentFragment == null)
                break soundIcon;
            Class<? extends Fragment> clazz = currentFragment.getClass();

            if (audioAssetFiles.containsKey(clazz))
                soundItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_playSound) {
            attemptPlaySoundForCurrentFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private MediaPlayer mp;

    private void attemptPlaySoundForCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment == null)
            return;
        Class<? extends Fragment> clazz = currentFragment.getClass();
        String assetName = audioAssetFiles.get(clazz);
        if (assetName == null) {
            Log.e(TAG, "attemptPlaySoundForCurrentFragment: Current fragment not associated with any sound file");
            return;
        }

        try (AssetFileDescriptor afd = getAssets().openFd("audio/" + assetName)) {
            stopCurrentSound();
            mp = new MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
            Toast.makeText(this, getResources().getText(R.string.audio_play), Toast.LENGTH_SHORT).show();
            
            mp.setOnCompletionListener((MediaPlayer mp2) -> {
                mp2.release();
                mp = null;
            });
        }
        catch (IOException e) {
            Toast.makeText(this, getResources().getText(R.string.audio_not_playable), Toast.LENGTH_SHORT)
                    .show();
        }
    }
    
    private void stopCurrentSound() {
        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
            mp = null;
        }
    }
}