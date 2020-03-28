package ch.epfl.sdp.kandle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.kandle.ImagePicker.ProfilePicPicker;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;


public class CustomAccountActivity extends AppCompatActivity {

    public final static int PROFILE_PICTURE_TAG = 12;
    private ProfilePicPicker profilePicPicker;

    Button uploadButton;
    Button leaveButton;
    ImageView profilePic;
    EditText m_nickname;

    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_account);

        database = DependencyManager.getDatabaseSystem();

        uploadButton = findViewById(R.id.button);
        leaveButton = findViewById(R.id.startButton);
        profilePic = findViewById(R.id.profilePic);
        m_nickname = findViewById(R.id.nickname);

        profilePicPicker = new ProfilePicPicker(this);

        uploadButton.setOnClickListener(v -> profilePicPicker.openImage());

        leaveButton.setOnClickListener(v -> {
            String nickname = m_nickname.getText().toString();
            if (nickname.trim().length() > 0) {
                database.updateNickname(nickname.trim()).addOnCompleteListener(task -> {
                    startMainActivity();
                });
            } else {
                startMainActivity();
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
        Uri uri = profilePicPicker.handleActivityResult(requestCode, resultCode, data);

        if (uri != null) {
            profilePic.setTag(PROFILE_PICTURE_TAG);
            profilePic.setImageURI(uri);
        }
    }


}