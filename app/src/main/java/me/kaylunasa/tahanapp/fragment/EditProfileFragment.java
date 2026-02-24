package me.kaylunasa.tahanapp.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.adapter.FontArrayAdapter;
import me.kaylunasa.tahanapp.data.ChildProfile;
import me.kaylunasa.tahanapp.data.User;
import me.kaylunasa.tahanapp.util.BitmapScaleKt;
import me.kaylunasa.tahanapp.util.SessionDataManager;
import me.kaylunasa.tahanapp.util.UserDataManager;

public class EditProfileFragment extends BottomSheetDialogFragment {
    private static final String TAG = EditProfileFragment.class.getSimpleName();

    private final String oldProfileName;
    private ChildProfile childProfile;
    private final ChildProfile backupChildProfile;
    private Bitmap photo;

    public EditProfileFragment(@NonNull ChildProfile childProfile) {
        oldProfileName = childProfile.getName();
        this.childProfile = childProfile;
        this.backupChildProfile = childProfile;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        final Spinner profileGenderSpinner = view.findViewById(R.id.profileGenderSpinner);
        FontArrayAdapter<String> profileGenderSpinnerAdapter = new FontArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.genders),
                ResourcesCompat.getFont(requireActivity(), R.font.frutiger)
        );
        profileGenderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileGenderSpinner.setAdapter(profileGenderSpinnerAdapter);

        final TextInputLayout profileNameInputLayout = view.findViewById(R.id.profileNameInput);
        final TextInputLayout profileAgeInputLayout = view.findViewById(R.id.profileAgeInput);
        final TextInputLayout profileDiagnosisInputLayout = view.findViewById(R.id.profileDiagnosisInput);

        assert profileNameInputLayout.getEditText() != null;
        assert profileAgeInputLayout.getEditText() != null;
        assert profileDiagnosisInputLayout.getEditText() != null;

        final EditText profileNameEditText = profileNameInputLayout.getEditText();
        final EditText profileAgeEditText = profileAgeInputLayout.getEditText();
        final EditText profileDiagnosisEditText = profileDiagnosisInputLayout.getEditText();

        final ShapeableImageView profileImage = view.findViewById(R.id.profileImage);

        profileNameEditText.setText(childProfile.getName());
        profileAgeEditText.setText(Integer.toString(childProfile.getAge()));
        for (int i = 0; i < profileGenderSpinnerAdapter.getCount(); i++)
            if (Objects.equals(profileGenderSpinnerAdapter.getItem(i), childProfile.getGender())) {
                profileGenderSpinner.setSelection(i);
                break;
            }
        profileDiagnosisEditText.setText(childProfile.getDiagnosis());
        this.photo = childProfile.getImage();
        profileImage.setImageBitmap(photo);

        final Button closeBtn = view.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener((v) -> dismiss());

        final Button submitBtn = view.findViewById(R.id.saveBtn);
        submitBtn.setOnClickListener((v) -> {
            ChildProfile.Draft draft = new ChildProfile.Draft(childProfile);

            final String profileName = profileNameEditText.getText().toString().trim();
            final String profileAgeString = profileAgeEditText.getText().toString();
            final String profileGender = profileGenderSpinner.getSelectedItem().toString();
            final String profileDiagnosis = profileDiagnosisEditText.getText().toString();

            if (profileName.isEmpty()) {
                Toast.makeText(requireContext(), getResources().getText(R.string.profile_name_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            if (profileName.length() > 50) {
                Toast.makeText(requireContext(), getResources().getText(R.string.profile_name_too_long), Toast.LENGTH_SHORT).show();
                return;
            }
            if (profileAgeString.isEmpty()) {
                Toast.makeText(requireContext(), getResources().getText(R.string.profile_age_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            int profileAge;
            try {
                profileAge = Integer.parseInt(profileAgeString);
            }
            catch (NumberFormatException e) {
                Log.e(TAG, "onViewCreated: Integer conversion error", e);
                Toast.makeText(requireContext(), "An input error occurred: Invalid integer", Toast.LENGTH_SHORT).show();
                return;
            }
            if (profileAge < 0 || profileAge > 200) {
                Toast.makeText(requireContext(), getResources().getText(R.string.profile_age_outside_range), Toast.LENGTH_SHORT).show();
                return;
            }
            if (profileGender.isEmpty()) {
                Toast.makeText(requireContext(), "An input error occurred: Invalid gender input state", Toast.LENGTH_SHORT).show();
                return;
            }
            if (profileDiagnosis.trim().isEmpty()) {
                Toast.makeText(requireContext(), getResources().getText(R.string.profile_diagnosis_required), Toast.LENGTH_SHORT).show();
                return;
            }

            draft.setName(profileName);
            draft.setAge(profileAge);
            draft.setGender(profileGender);
            draft.setDiagnosis(profileDiagnosis);
            if (photo != null)
                draft.setImage(photo);
            ChildProfile childProfile = draft.finalizeDraft();

            try {
                String username = SessionDataManager.getSessionUsername(requireContext());
                String passwordHash = SessionDataManager.getSessionPasswordHash(requireContext());

                if (username == null || passwordHash == null ||
                    !UserDataManager.checkUserPassword(requireContext(), username, passwordHash))
                    throw new IllegalStateException();

                User user = User.fromJsonData(UserDataManager.getUserData(requireContext(), username, passwordHash));
                User.Draft editUser = new User.Draft(user);

                editUser.removeChildProfile(oldProfileName);
                if (!editUser.addChildProfile(childProfile)) {
                    Toast.makeText(requireContext(), getResources().getText(R.string.profile_name_exists), Toast.LENGTH_SHORT).show();
                    editUser.addChildProfile(backupChildProfile);
                    return;
                }
                user = editUser.finalizeDraft();
                UserDataManager.putUserData(
                        requireContext(),
                        username,
                        passwordHash,
                        user.toJsonData()
                );
                Toast.makeText(requireContext(), getResources().getString(R.string.profile_edit_success), Toast.LENGTH_SHORT).show();
                if (getActivity() != null)
                    requireActivity().finish();
            }
            catch (IllegalStateException | JSONException e) {
                Toast.makeText(requireContext(), getResources().getText(R.string.logged_out_force), Toast.LENGTH_SHORT).show();
                try {
                    SessionDataManager.resetSessionData(requireContext());
                }
                catch (IOException ex) {
                    Toast.makeText(requireContext(), "Could not reset stored data.", Toast.LENGTH_SHORT)
                            .show();
                    System.exit(0);
                    return;
                }

                if (getActivity() != null)
                    requireActivity().finish();
                return;
            }

            dismiss();

            if (getActivity() != null)
                requireActivity().recreate();
        });

        ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    Bitmap fetchedBitmap = null;
                    if (uri != null) {
                        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
                            fetchedBitmap = BitmapFactory.decodeStream(inputStream);
                        }
                        catch (IOException e) {
                            Log.e(TAG, "onViewCreated: Could not access file at " + uri, e);
                        }
                    }
                    if (fetchedBitmap == null) {
                        return;
                    }

                    Bitmap scaledBitmap = BitmapScaleKt.getScaledBitmap(fetchedBitmap, 500);
                    if (scaledBitmap == null) {
                        Log.w(TAG, "onViewCreated: Error scaling bitmap at " + uri);
                        Toast.makeText(requireContext(), getResources().getText(R.string.image_not_scalable), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    this.photo = scaledBitmap;
                    profileImage.setImageBitmap(photo);
                }
        );

        Button uploadPhotoBtn = view.findViewById(R.id.uploadPhotoBtn);
        uploadPhotoBtn.setOnClickListener((v) -> imagePickerLauncher.launch("image/*"));

        view.findViewById(R.id.profileNameInput).requestFocus(View.FOCUS_DOWN);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null)
            return;
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null)
            return;
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setFitToContents(true);
        behavior.setDraggable(true);
    }
}
