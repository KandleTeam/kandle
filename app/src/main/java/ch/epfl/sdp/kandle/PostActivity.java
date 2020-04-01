package ch.epfl.sdp.kandle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.Date;
import java.util.Random;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import ch.epfl.sdp.kandle.ImagePicker.ImagePicker;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.fragment.MapViewFragment;

public class PostActivity extends AppCompatActivity {


    private Post p;

    private Authentication auth;
    private Database database;

    private EditText mPostText;
    private Button mPostButton;
    private ImageButton mGalleryButton, mCameraButton;
    private ImageView mPostImage;
    private ImagePicker postImagePicker;
    public final static int POST_IMAGE_TAG = 42;


    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //Permission();

        Random rand = new Random();

        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra("latitude", 0.0) + 0.01;
        Double longitude = intent.getDoubleExtra("longitude", 0.0) + 0.01;


        System.out.println(latitude);
        System.out.println(longitude);

        mPostText = findViewById(R.id.postText);
        mPostButton =findViewById(R.id.postButton);
        mGalleryButton =findViewById(R.id.galleryButton);
        mCameraButton =findViewById(R.id.cameraButton);
        mPostImage =findViewById(R.id.postImage);
        postImagePicker = new ImagePicker(this);

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();

        mPostButton.setOnClickListener(v -> {



            String postText  = mPostText.getText().toString().trim();
            Uri imageUri = postImagePicker.getImageUri();

            if(postText.isEmpty() && imageUri == null){
                mPostText.setError("Your post is empty...");
                return;
            }

            if (imageUri != null) {
                postImagePicker.uploadImage().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri == null) {
                            Toast.makeText(PostActivity.this, "Unable to upload image", Toast.LENGTH_LONG).show();
                        }
                        else {
                            p = new Post(postText, downloadUri.toString(), new Date(), auth.getCurrentUser().getUid(), longitude, latitude);
                            post(p);
                        }
                    }
                });
            }
            else {
                p = new Post(postText, null, new Date(), auth.getCurrentUser().getUid(), longitude, latitude);
                post(p);
            }

        });

        mCameraButton.setOnClickListener(v -> Toast.makeText(PostActivity.this, "Doesn't work for now... " , Toast.LENGTH_LONG ).show());


        mGalleryButton.setOnClickListener(v -> postImagePicker.openImage());
    }

    private void post(Post p) {
        database.addPost(auth.getCurrentUser().getUid(), p).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(PostActivity.this, "You have successfully posted " , Toast.LENGTH_LONG ).show();
                finish();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        postImagePicker.handleActivityResult(requestCode, resultCode, data);
        Uri uri = postImagePicker.getImageUri();
        if (uri != null) {
            mPostImage.setTag(POST_IMAGE_TAG);
            mPostImage.setImageURI(uri);
        }
    }


}
