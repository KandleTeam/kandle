package ch.epfl.sdp.kandle.fragment;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import ch.epfl.sdp.kandle.R;

public class TextDialogFragment extends DialogFragment {
    public static final String TAG = TextDialogFragment.class.getSimpleName();
    public static final String EXTRA_COLOR_CODE = "extra_color_code";
    private EditText mAddTextEditText;
    private TextView mAddTextDoneTextView;
    private InputMethodManager mInputMethodManager;
    private TextEditor mTextEditor;
    private int mColorCode;

    public static TextDialogFragment show(@NonNull AppCompatActivity appCompatActivity, @ColorInt int colorCode) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_COLOR_CODE, colorCode);
        TextDialogFragment fragment = new TextDialogFragment();
        fragment.setArguments(args);
        fragment.show(appCompatActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text);
        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mAddTextDoneTextView = view.findViewById(R.id.add_text_done_tv);
        mColorCode = getArguments().getInt(EXTRA_COLOR_CODE);
        mAddTextEditText.setTextColor(mColorCode);
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        mAddTextDoneTextView.setOnClickListener(view1 -> {
            mInputMethodManager.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            dismiss();
            String inputText = mAddTextEditText.getText().toString();
            if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                mTextEditor.onDone(inputText);
            }
        });

    }

    public void setOnTextEditorListener(TextEditor textEditor) {
        mTextEditor = textEditor;
    }

    public interface TextEditor {
        void onDone(String inputText);
    }
}
