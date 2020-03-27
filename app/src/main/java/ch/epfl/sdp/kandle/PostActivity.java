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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import ch.epfl.sdp.kandle.ImagePicker.PostImagePicker;
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
    private PostImagePicker postImagePicker;
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
        postImagePicker = new PostImagePicker(this);

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();

        mPostButton.setOnClickListener(v -> {

            String postText  = mPostText.getText().toString().trim();

            if(postText.isEmpty()){
                mPostText.setError("Your post is empty...");
                return;
            }

            p = new Post("text", postText, new Date(), auth.getCurrentUser().getUid());
            database.addPost(auth.getCurrentUser().getUid(), p).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(PostActivity.this, "You have successfully posted : " + postText, Toast.LENGTH_LONG ).show();
                    finish();
                }else{
                    System.out.println(task.getException().getMessage());
                }
            });
        });

        mCameraButton.setOnClickListener(v -> Toast.makeText(PostActivity.this, "Doesn't work for now... " , Toast.LENGTH_LONG ).show());


        mGalleryButton.setOnClickListener(v -> postImagePicker.openImage());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = postImagePicker.handleActivityResult(requestCode, resultCode, data);

        if (uri != null) {
            mPostImage.setTag(POST_IMAGE_TAG);
            mPostImage.setImageURI(uri);
        }
    }

}
