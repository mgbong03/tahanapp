package me.kaylunasa.tahanapp.fragment;

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

import me.kaylunasa.tahanapp.R;
import me.kaylunasa.tahanapp.adapter.FontArrayAdapter;
import me.kaylunasa.tahanapp.data.ChildProfile;
import me.kaylunasa.tahanapp.data.User;
import me.kaylunasa.tahanapp.util.BitmapScaleKt;
import me.kaylunasa.tahanapp.util.SessionDataManager;
import me.kaylunasa.tahanapp.util.Sha256Kt;
import me.kaylunasa.tahanapp.util.UserDataManager;

public class ChangePasswordFragment extends BottomSheetDialogFragment {
    private static final String TAG = ChangePasswordFragment.class.getSimpleName();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        final TextInputLayout oldPasswordInput = view.findViewById(R.id.oldPasswordInput);
        final TextInputLayout newPasswordInput = view.findViewById(R.id.newPasswordInput);
        final TextInputLayout confirmNewPasswordInput = view.findViewById(R.id.confirmNewPasswordInput);

        assert oldPasswordInput.getEditText() != null;
        assert newPasswordInput.getEditText() != null;
        assert confirmNewPasswordInput.getEditText() != null;

        final EditText oldPasswordEditText = oldPasswordInput.getEditText();
        final EditText newPasswordEditText = newPasswordInput.getEditText();
        final EditText confirmNewPasswordEditText = confirmNewPasswordInput.getEditText();

        final Button closeBtn = view.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener((v) -> dismiss());
        
        Button saveBtn = view.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener((v) -> {
            try {
                final String username = SessionDataManager.getSessionUsername(requireContext());
                if (username == null)
                    throw new IllegalStateException();
                
                final String oldPassword = oldPasswordEditText.getText().toString();
                final String newPassword = newPasswordEditText.getText().toString();
                final String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

                final String oldPasswordHash = Sha256Kt.sha256(oldPassword);

                if (newPassword.length() < 8) {
                    Toast.makeText(requireContext(), getResources().getText(R.string.password_too_short), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (!newPassword.equals(confirmNewPassword)) {
                    Toast.makeText(requireContext(), getResources().getText(R.string.passwords_do_not_match), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (!UserDataManager.checkUserPassword(requireContext(), username, oldPasswordHash)) {
                    Toast.makeText(requireContext(), getResources().getText(R.string.invalid_password), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                
                User.Draft userDraft = new User.Draft(User.fromJsonData(UserDataManager.getUserData(requireContext(), username, oldPasswordHash)));
                userDraft.setPassword(newPassword);
                SessionDataManager.putSessionPasswordHash(requireContext(), Sha256Kt.sha256(newPassword));
                UserDataManager.putUserData(requireContext(), username, oldPasswordHash, userDraft.finalizeDraft().toJsonData());

                Toast.makeText(requireContext(), getResources().getText(R.string.password_change_success), Toast.LENGTH_SHORT).show();
                dismiss();
            }
            catch (IllegalStateException | JSONException e) {
                Log.e(TAG, "onCreate: Could not change password", e);
                Toast.makeText(requireContext(), "Unable to change password: JSON error", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        oldPasswordInput.requestFocus(View.FOCUS_DOWN);
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
