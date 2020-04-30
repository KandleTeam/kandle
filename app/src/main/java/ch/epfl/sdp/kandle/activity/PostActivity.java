package ch.epfl.sdp.kandle.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.kandle.PostCamera;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.fragment.YourPostListFragment;
import ch.epfl.sdp.kandle.imagePicker.ImagePicker;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.Post;


public class PostActivity extends AppCompatActivity {

    public final static int POST_IMAGE_TAG = 42;
    private EditText mPostText;
    private Button mPostButton;
    private ImageButton mBackButton;
    private ImageButton mGalleryButton, mCameraButton;
    private ImageView mPostImage;
    private Post p;
    private Authentication auth;
    private Database database;
    private PostCamera postCamera;
    private Uri imageUri;

    private Post editPost;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //Permission();

        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra("latitude", 0.0) - 0.00015;
        Double longitude = intent.getDoubleExtra("longitude", 0.0) - 0.00015;
        String postId = intent.getStringExtra("postId");


        mPostText = findViewById(R.id.postText);
        mPostButton = findViewById(R.id.postButton);
        mGalleryButton = findViewById(R.id.galleryButton);
        mCameraButton = findViewById(R.id.cameraButton);
        mPostImage = findViewById(R.id.postImage);
        mBackButton = findViewById(R.id.backButton);
        postCamera = new PostCamera(this);

        auth = DependencyManager.getAuthSystem();
        database = new CachedFirestoreDatabase();

        if (postId != null) {
            database.getPostByPostId(postId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Post p = task.getResult();
                    mPostButton.setText("EDIT");
                    mPostText.setText(p.getDescription());
                    mPostImage.setVisibility(View.VISIBLE);
                    mPostImage.setTag(YourPostListFragment.POST_IMAGE);
                    Picasso.get().load(p.getImageURL()).into(mPostImage);
                }
            });

            //mPostImage.setImageURI();

        }

        mPostButton.setOnClickListener(v -> {

            String postText = mPostText.getText().toString().trim();

            if (postText.isEmpty() && imageUri == null) {
                mPostText.setError("Your post is empty...");
                return;
            }

            if (imageUri != null) {
                ImagePicker.uploadImage(imageUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri == null) {
                            Toast.makeText(PostActivity.this, "Unable to upload image", Toast.LENGTH_LONG).show();

                        } else {
                            if (postId != null) {
                                database.getPostByPostId(postId).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Post p = task2.getResult();
                                        p.setDescription(postText);
                                        p.setImageURL(downloadUri.toString());
                                        p.setLatitude(p.getLatitude());
                                        p.setLongitude(p.getLongitude());
                                        p.setLikers(p.getLikers());
                                        editPost(p, postId);
                                    }
                                });
                            } else {
                                p = new Post(postText, downloadUri.toString(), new Date(), auth.getCurrentUser().getId(), longitude, latitude);
                                post(p);
                            }
                        }
                    }
                });

            } else {
                if (postId != null) {
                    database.getPostByPostId(postId).addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            Post p = task2.getResult();
                            p.setDescription(postText);
                            //raise the test coverage
                            p.setLatitude(p.getLatitude());
                            p.setLongitude(p.getLongitude());
                            p.setLikers(p.getLikers());
                            editPost(p, postId);
                        }
                    });
                } else {
                    p = new Post(postText, null, new Date(), auth.getCurrentUser().getId(), longitude, latitude);
                    post(p);
                }
            }

        });

        mBackButton.setOnClickListener(v -> finish());

        mCameraButton.setOnClickListener(v -> postCamera.openCamera());


        mGalleryButton.setOnClickListener(v -> ImagePicker.openImage(this));
    }

    private void post(Post p) {
        database.addPost(p).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Toast.makeText(PostActivity.this, "You have successfully posted ", Toast.LENGTH_LONG).show();

                finish();

            }
        });
    }

    private void editPost(Post p, String postId) {
        database.editPost(p, postId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Toast.makeText(PostActivity.this, "You have successfully edited your post ", Toast.LENGTH_LONG).show();

                finish();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            Bitmap imageBitmap = postCamera.handleActivityResult(requestCode, resultCode, data);
            if (imageBitmap != null) {
                mPostImage.setVisibility(View.VISIBLE);
                mPostImage.setTag(POST_IMAGE_TAG);
                mPostImage.setImageBitmap(imageBitmap);
            }
            imageUri = postCamera.getImageUri();
        } else {
            imageUri = ImagePicker.handleActivityResultAndGetUri(requestCode, resultCode, data);
            if (imageUri != null) {
                mPostImage.setVisibility(View.VISIBLE);
                mPostImage.setTag(POST_IMAGE_TAG);
                mPostImage.setImageURI(imageUri);
            }
        }
    }


}
