package me.kaylunasa.tahanapp.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import me.kaylunasa.tahanapp.R;

public class PainLocationFragment extends Fragment {
    private static final String TAG = PainLocationFragment.class.getSimpleName();
    private final List<String> selectedAreas = new ArrayList<>();

    private static class Region {
        String name;
        int x1, y1, x2, y2;

        Region (String name, int x1, int y1, int x2, int y2) {
            this.name = name;
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        boolean isInRegion(float x, float y) {
            return (x >= x1 && x <= x2 && y >= y1 && y <= y2);
        }
    }

    private static final List<Region> frontRegions = List.of(
            new Region("Head (Front) / Ulo (Harap)", 150, 190, 298, 368),
            new Region("Chest / Dibdib", 154, 446, 302, 560),
            new Region("Shoulders / Balikat", 112, 394, 353, 440),
            new Region("Abdomen / Tiyan", 146, 586, 330, 713),
            new Region("Arms / Braso", 15, 450, 117, 848),
            new Region("Arms / Braso", 323, 450, 459, 848),
            new Region("Legs / Binti", 118, 721, 366, 1341)
    );
    private static final List<Region> backRegions = List.of(
            new Region("Head (Back) / Ulo (Likod)", 190, 192, 358, 366),
            new Region("Shoulders / Balikat", 133, 394, 399, 473),
            new Region("Back / Ulo", 205, 433, 346, 674),
            new Region("Hind / Puwet", 164, 691, 387, 826),
            new Region("Legs / Binti", 148, 832, 408, 1339),
            new Region("Arms / Braso", 42, 420, 158, 880),
            new Region("Arms / Braso", 364, 420, 495, 880)
    );

    private ImageView frontView;
    private ImageView backView;
    private TextView locationsDisplay;

    private Vibrator vibrator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pain_location, container, false);
    }

    private void toggleArea(String area) {
        if (selectedAreas.contains(area))
            selectedAreas.remove(area);
        else
            selectedAreas.add(area);
    }

    private String getAreaNamesCoerced() {
        if (selectedAreas.isEmpty())
            return "None";

        StringJoiner sj = new StringJoiner(", ");
        for (String s : selectedAreas)
            sj.add(s);
        return sj.toString();
    }

    private void updateLocationDisplay() {
        if (this.locationsDisplay == null)
            return;
        this.locationsDisplay.setText(String.format(
                getResources().getText(R.string.selected_locations).toString(),
                getAreaNamesCoerced()
        ));
    }

    public List<String> getSelectedAreas() {
        return this.selectedAreas;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.locationsDisplay = view.findViewById(R.id.selectedLocations);
        updateLocationDisplay();

        fetchVibrator: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager == null) break fetchVibrator;
            this.vibrator = vibratorManager.getDefaultVibrator();
        }
        else
            this.vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        this.frontView = view.findViewById(R.id.bodySelectorFront);
        this.backView = view.findViewById(R.id.bodySelectorBack);

        Button resetBtn = view.findViewById(R.id.resetBtn);
        resetBtn.setOnClickListener((v) -> {
            if (this.selectedAreas.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        requireContext().getResources().getText(R.string.reset_selection_empty),
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(requireContext().getResources().getText(R.string.reset_selection))
                    .setMessage(requireContext().getResources().getText(R.string.reset_selection_prompt))
                    .setPositiveButton("OK", (v2, e) -> {
                        this.selectedAreas.clear();
                        updateLocationDisplay();
                        Toast.makeText(
                                requireContext(),
                                requireContext().getResources().getText(R.string.reset_selection_success),
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (v2, e) ->
                        Toast.makeText(
                                requireContext(),
                                requireContext().getResources().getText(R.string.reset_cancelled),
                                Toast.LENGTH_SHORT).show()
                    )
                    .show();
        });

        Button frontBtn = view.findViewById(R.id.btnFront);
        Button backBtn = view.findViewById(R.id.btnBack);

        frontBtn.setOnClickListener((v) -> {
            this.frontView.setVisibility(View.VISIBLE);
            this.backView.setVisibility(View.GONE);
        });

        backBtn.setOnClickListener((v) -> {
            this.backView.setVisibility(View.VISIBLE);
            this.frontView.setVisibility(View.GONE);
        });

        frontView.setOnTouchListener(getPainLocationMapper(frontRegions));
        backView.setOnTouchListener(getPainLocationMapper(backRegions));
    }

    private View.OnTouchListener getPainLocationMapper(List<Region> regions) {
        return (v, e) -> {
            if (!(v instanceof ImageView))
                return false;

            ImageView attachedView = (ImageView) v;

            float touchX = e.getX();
            float touchY = e.getY();

            if (e.getAction() != MotionEvent.ACTION_UP)
                return true;

            int imgWidth = attachedView.getDrawable().getIntrinsicWidth();
            int imgHeight = attachedView.getDrawable().getIntrinsicHeight();

            int viewWidth = attachedView.getWidth();
            int viewHeight = attachedView.getHeight();

            float scaleX = (float) imgWidth / viewWidth;
            float scaleY = (float) imgHeight / viewHeight;

            float imageX = touchX * scaleX;
            float imageY = touchY * scaleY;

            for (Region region : regions) if (region.isInRegion(imageX, imageY)) {
                toggleArea(region.name);
                updateLocationDisplay();
                vibrate();
                break;
            }

            v.performClick();
            return true;
        };
    }

    private void vibrate() {
        if (vibrator == null) {
            Log.d(TAG, "vibrate: vibrator not available");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            vibrator.vibrate(100);
    }
}
