package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;

public class SettingsFragment extends Fragment {

    private LinearLayout mModifyPasswordLayout;
    private LinearLayout mOtherSettingsLayout;
    private LinearLayout mModifyPasswordContent;
    private TextView mOtherSettingsContent;
    private Button mPasswordButton;

    private Authentication auth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        getViews(view);

        mModifyPasswordLayout.setOnClickListener(v ->
        {
            extendOnClick(mModifyPasswordContent);
        });
        mOtherSettingsLayout.setOnClickListener(v -> {
            extendOnClick(mOtherSettingsContent);
        });

        return view;
    }

    private void getViews(View parent) {
        mModifyPasswordLayout = parent.findViewById(R.id.modifyPassword);
        mOtherSettingsLayout = parent.findViewById(R.id.otherSettings);
        mModifyPasswordContent = parent.findViewById(R.id.modifyPasswordContent);
        mOtherSettingsContent = parent.findViewById(R.id.otherSettingsContent);
        mPasswordButton = parent.findViewById(R.id.validatePasswordButton);
    }

    private void extendOnClick(View view) {
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        }
        else {
            view.setVisibility(View.GONE);
        }
    }


}
