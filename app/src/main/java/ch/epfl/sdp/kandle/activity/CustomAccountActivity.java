package ch.epfl.sdp.kandle.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import ch.epfl.sdp.kandle.MainActivity;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.imagePicker.ProfilePicPicker;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

//TODO: handle case when user leaves activity before saving

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
            ProgressDialog pd = new ProgressDialog(CustomAccountActivity.this);
            pd.setMessage("Finalizing your account");
            pd.show();
            profilePicPicker.setProfilePicture().addOnCompleteListener(task -> {
                String nickname = m_nickname.getText().toString().trim();
                if (nickname.length() > 0) {
                    database.updateNickname(nickname).addOnCompleteListener(task1 -> {
                        pd.dismiss();
                        startMainActivity();;
                    });
                }
                else {
                    pd.dismiss();
                    startMainActivity();
                }
            });
        });
    }

    private void startMainActivity() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        profilePicPicker.handleActivityResult(requestCode, resultCode, data);
        Uri uri = profilePicPicker.getImageUri();

        if (uri != null) {
            profilePic.setTag(PROFILE_PICTURE_TAG);
            profilePic.setImageURI(uri);
        }
    }


}