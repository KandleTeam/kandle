package ch.epfl.sdp.kandle.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.activity.LoginActivity;
import ch.epfl.sdp.kandle.activity.MainActivity;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class SettingsFragment extends Fragment {

    private LinearLayout mModifyPasswordLayout;
    private LinearLayout mOtherSettingsLayout;
    private LinearLayout mModifyPasswordContent;
    private TextView mOtherSettingsContent;
    private TextView mOldPassword, mNewPassword, mNewPasswordConfirm;
    private ImageView mExpandPassword, mExpandOtherSettings;
    private Button mPasswordButton;
    private Button mDeleteAccount;
    private Button mClearCache;

    private Authentication auth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        getViews(view);

        auth = DependencyManager.getAuthSystem();

        mModifyPasswordLayout.setOnClickListener(v ->
                extendOnClick(mModifyPasswordContent, mExpandPassword)
        );
        if (LoggedInUser.isGuestMode()) {
            mModifyPasswordLayout.setVisibility(View.GONE);
            mDeleteAccount.setVisibility(View.GONE);
        }


        mOtherSettingsLayout.setOnClickListener(v -> {
            extendOnClick(mOtherSettingsContent, mExpandOtherSettings);
        });

        mPasswordButton.setOnClickListener(v -> {
            String oldPassword = mOldPassword.getText().toString();
            ProgressDialog pd = new ProgressDialog(getContext());
            pd.setMessage("Updating password");
            pd.show();
            auth.reAuthenticate(oldPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String newPassword = mNewPassword.getText().toString();
                    String newPasswordConfirm = mNewPasswordConfirm.getText().toString();
                    if (newPassword.length() < 8) {
                        mNewPassword.setError("Please choose a password of more than 8 characters !");
                        pd.dismiss();
                    } else if (!newPassword.equals(newPasswordConfirm)) {
                        mNewPasswordConfirm.setError("Your passwords do not match !");
                        pd.dismiss();
                    } else {
                        auth.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                mOldPassword.setText("");
                                mNewPassword.setText("");
                                mNewPasswordConfirm.setText("");
                                Toast.makeText(getContext(), "Your password has been successfully updated", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        });
                    }
                } else {
                    mOldPassword.setError("Unable to authenticate, please check that your password is correct");
                    pd.dismiss();
                }
            });
        });


        mDeleteAccount.setOnClickListener(v -> {
            auth.deleteUser();
            startActivity(new Intent(this.getContext().getApplicationContext(), LoginActivity.class));
            getFragmentManager().popBackStack();
        });

        mClearCache.setOnClickListener(v -> {
            DependencyManager.getLocalDatabase().clearAllTables();
            DependencyManager.getLocalDatabase().userDao().insertUser(LoggedInUser.getInstance());
            DependencyManager.getInternalStorageSystem().deleteAllPictures();
        });



        return view;
    }

    private void getViews(View parent) {
        mModifyPasswordLayout = parent.findViewById(R.id.modifyPassword);
        mOtherSettingsLayout = parent.findViewById(R.id.otherSettings);
        mModifyPasswordContent = parent.findViewById(R.id.modifyPasswordContent);
        mOtherSettingsContent = parent.findViewById(R.id.otherSettingsContent);
        mPasswordButton = parent.findViewById(R.id.validatePasswordButton);
        mOldPassword = parent.findViewById(R.id.oldPassword);
        mNewPassword = parent.findViewById(R.id.newPassword);
        mNewPasswordConfirm = parent.findViewById(R.id.newPasswordConfirm);
        mExpandPassword = parent.findViewById(R.id.expandPassword);
        mExpandOtherSettings = parent.findViewById(R.id.expandOtherSettings);
        mDeleteAccount = parent.findViewById(R.id.deleteAccountButton);
        mClearCache = parent.findViewById(R.id.clearCacheButton);
    }

    private void extendOnClick(View view, ImageView iv) {
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
            iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
        } else {
            view.setVisibility(View.GONE);
            iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
        }
    }



}
