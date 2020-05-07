package ch.epfl.sdp.kandle.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.imagePicker.ProfilePicPicker;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

//TODO: handle case when user leaves activity before saving

public class CustomAccountActivity extends AppCompatActivity {

    public final static int PROFILE_PICTURE_TAG = 12;

    private ImageView profilePic;
    private EditText mNickname;

    private Database database;

    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_account);

        database = new CachedFirestoreDatabase();

        Button uploadButton = findViewById(R.id.button);
        Button leaveButton = findViewById(R.id.startButton);
        profilePic = findViewById(R.id.profilePic);
        mNickname = findViewById(R.id.nickname);
        uploadButton.setOnClickListener(v -> ProfilePicPicker.openImage(this));

        leaveButton.setOnClickListener(v -> {
            String nickname = mNickname.getText().toString().trim();
            if (imageUri == null && nickname.length() == 0) {
                startMainActivity();
            } else {
                ProgressDialog pd = new ProgressDialog(CustomAccountActivity.this);
                pd.setMessage(getString(R.string.customAccountFinalization));
                pd.show();
                Task<Void> task;
                if (imageUri != null) {
                    task = ProfilePicPicker.setProfilePicture(imageUri);
                    if (nickname.length() > 0) {
                        task.continueWith(t -> database.updateNickname(nickname));
                    }
                } else {
                    task = database.updateNickname(nickname);
                }

                task.addOnCompleteListener(t -> {
                    pd.dismiss();
                    startMainActivity();
                });
            }

        });
    }

    private void startMainActivity() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUri = ProfilePicPicker.handleActivityResultAndGetUri(requestCode, resultCode, data);

        if (imageUri != null) {
            profilePic.setTag(PROFILE_PICTURE_TAG);
            profilePic.setImageURI(imageUri);
        }
    }


}