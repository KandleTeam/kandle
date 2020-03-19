package ch.epfl.sdp.kandle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import ch.epfl.sdp.kandle.ImagePicker.ProfilePicPicker;


public class CustomAccountActivity extends AppCompatActivity {

    public final static int PROFILE_PICTURE_TAG = 12;
    private ProfilePicPicker profilePicPicker;

    Button uploadButton;
    Button leaveButton;
    ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_account);

        uploadButton = findViewById(R.id.button);
        leaveButton = findViewById(R.id.startButton);
        profilePic = findViewById(R.id.profilePic);

        profilePicPicker = new ProfilePicPicker(this);

        uploadButton.setOnClickListener(v -> profilePicPicker.openImage());

        leaveButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });
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