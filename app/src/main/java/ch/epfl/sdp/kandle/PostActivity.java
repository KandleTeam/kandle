package ch.epfl.sdp.kandle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Date;

import ch.epfl.sdp.kandle.ImagePicker.ImagePicker;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

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
                            Post p = new Post(postText, downloadUri.toString(), new Date(), auth.getCurrentUser().getUid());
                            post(p);
                        }
                    }
                });
            }
            else {
                Post p = new Post(postText, null, new Date(), auth.getCurrentUser().getUid());
                post(p);
            }

        });

        mCameraButton.setOnClickListener(v -> Toast.makeText(PostActivity.this, "Doesn't work for now... " , Toast.LENGTH_LONG ).show());


        mGalleryButton.setOnClickListener(v -> postImagePicker.openImage());
    }

    private void post(Post p) {
        database.addPost(auth.getCurrentUser().getUid(), p).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(PostActivity.this, "You have successfully posted : " + p.getDescription(), Toast.LENGTH_LONG ).show();
                finish();
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }else{
                System.out.println(task.getException().getMessage());
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
