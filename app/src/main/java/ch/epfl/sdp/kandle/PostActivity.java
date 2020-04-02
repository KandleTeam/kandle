package ch.epfl.sdp.kandle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ch.epfl.sdp.kandle.ImagePicker.PostImagePicker;

public class PostActivity extends AppCompatActivity implements LifecycleOwner {


    EditText mPostText;
    private ImageView imageView;
    final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1024;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Button mPostButton;
    ImageButton mGalleryButton, mCameraButton;
    ImageView mPostImage;
    private PostImagePicker postImagePicker;
    public final static int POST_IMAGE_TAG = 42;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private PostCamera postCamera;
    String userID;
    private Camera mCamera;
    private Uri imageUri;
    private static final int TAKE_PICTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mPostText = findViewById(R.id.postText);
        mPostButton = findViewById(R.id.postButton);
        mGalleryButton = findViewById(R.id.galleryButton);
        mCameraButton = findViewById(R.id.cameraButton);
        mPostImage = findViewById(R.id.postImage);
        postImagePicker = new PostImagePicker(this);
        postCamera = new PostCamera(this);

        fStore = FirebaseFirestore.getInstance();

        fAuth = FirebaseAuth.getInstance();

        mPostButton.setOnClickListener(v -> {
            String postText = mPostText.getText().toString().trim();

            if (postText.isEmpty()) {
                mPostText.setError("Your post is empty...");
                return;
            }

            Toast.makeText(PostActivity.this, "You have successfully posted : " + postText, Toast.LENGTH_LONG).show();
            finish();

        });

        mCameraButton.setOnClickListener(v -> {
                postCamera.openCamera();

        });


        mGalleryButton.setOnClickListener(v -> postImagePicker.openImage());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            Bitmap imageBitmap = postCamera.handleActivityResult(requestCode, resultCode, data);
            mPostImage.setTag(POST_IMAGE_TAG);
            if (imageBitmap != null) {
                mPostImage.setImageBitmap(imageBitmap);
            }
        } else {
            Uri uri = postImagePicker.handleActivityResult(requestCode, resultCode, data);
            mPostImage.setTag(POST_IMAGE_TAG);
            if (uri != null) {
                //mPostImage.setTag(POST_IMAGE_TAG);
                mPostImage.setImageURI(uri);

            }
        }
    }
}
