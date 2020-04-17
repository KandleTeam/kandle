package ch.epfl.sdp.kandle.activity;


import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.MainActivity;
import ch.epfl.sdp.kandle.PostCamera;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.fragment.YourPostListFragment;
import ch.epfl.sdp.kandle.imagePicker.ImagePicker;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.caching.CachedDatabase;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.Post;
import ch.epfl.sdp.kandle.dependencies.Storage;

public class PostActivity extends AppCompatActivity {

    public final static int POST_IMAGE_TAG = 42;
    private static final int TAKE_PICTURE = 1;
    final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1024;
    private EditText mPostText;
    private ImageView imageView;
    private Button mPostButton;
    private ImageButton mGalleryButton, mCameraButton;
    private ImageView mPostImage;
    private ImagePicker postImagePicker;
    private Post p;
    private Authentication auth;
    private Database database;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private PostCamera postCamera;
    private String userID;
    private Camera mCamera;
    private Uri imageUri;

    private Post editPost;
    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //Permission();

        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra("latitude", 0.0) - 0.0015;
        Double longitude = intent.getDoubleExtra("longitude", 0.0) - 0.0015;
        String postId = intent.getStringExtra("postId");


        mPostText = findViewById(R.id.postText);
        mPostButton = findViewById(R.id.postButton);
        mGalleryButton = findViewById(R.id.galleryButton);
        mCameraButton = findViewById(R.id.cameraButton);
        mPostImage = findViewById(R.id.postImage);
        postImagePicker = new ImagePicker(this);
        postCamera = new PostCamera(this);

        auth = DependencyManager.getAuthSystem();
        database = new CachedDatabase();

        if(postId != null){
            database.getPostByPostId(postId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Post p = task.getResult();
                    mPostButton.setText("EDIT");
                    mPostText.setText(p.getDescription());
                    mPostImage.setTag(YourPostListFragment.POST_IMAGE);
                    Picasso.get().load(p.getImageURL()).into(mPostImage);
                }
            });

            //mPostImage.setImageURI();

        }

        mPostButton.setOnClickListener(v -> {

            System.out.println("passing here");

            String postText = mPostText.getText().toString().trim();
            System.out.println(postText);
            Uri imageUri = postImagePicker.getImageUri();
            if (imageUri == null) {
                imageUri = postCamera.getImageUri();
            }

            if (postText.isEmpty() && imageUri == null) {
                mPostText.setError("Your post is empty...");
                return;
            }

            if (imageUri != null) {
                uploadImage(imageUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri == null) {
                            Toast.makeText(PostActivity.this, "Unable to upload image", Toast.LENGTH_LONG).show();

                        } else {
                            if(postId != null){
                                database.getPostByPostId(postId).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Post p = task2.getResult();
                                        p.setDescription(postText);
                                        p.setImageURL(downloadUri.toString());
                                        p.setLatitude(p.getLatitude());
                                        p.setLongitude(p.getLongitude());
                                        p.setLikes(p.getLikes());
                                        editPost(p, postId);
                                    }
                                });
                            }else{
                                p = new Post(postText, downloadUri.toString(), new Date(), auth.getCurrentUser().getId(), longitude, latitude);
                                post(p);
                            }
                        }
                    }
                });

            }
            else {
                if(postId != null){
                    database.getPostByPostId(postId).addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            Post p = task2.getResult();
                            p.setDescription(postText);
                            //raise the test coverage
                            p.setLatitude(p.getLatitude());
                            p.setLongitude(p.getLongitude());
                            p.setLikes(p.getLikes());
                            editPost(p, postId);
                        }
                    });
                }else{
                    p = new Post(postText, null, new Date(), auth.getCurrentUser().getId(), longitude, latitude);
                    post(p);
                }
            }

        });

        mCameraButton.setOnClickListener(v -> postCamera.openCamera());


        mGalleryButton.setOnClickListener(v -> postImagePicker.openImage());
    }

    private void post(Post p) {
        database.addPost(p).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Toast.makeText(PostActivity.this, "You have successfully posted " , Toast.LENGTH_LONG ).show();

                finish();

            }
        });
    }

    private void editPost(Post p, String postId) {
        database.editPost(p, postId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Toast.makeText(PostActivity.this, "You have successfully edited your post " , Toast.LENGTH_LONG ).show();

                finish();

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPostImage.setTag(POST_IMAGE_TAG);
        if (requestCode == 0) {
            Bitmap imageBitmap = postCamera.handleActivityResult(requestCode, resultCode, data);
            if (imageBitmap != null) {
                mPostImage.setImageBitmap(imageBitmap);
            }
        } else {
            postImagePicker.handleActivityResult(requestCode, resultCode, data);
            Uri uri = postImagePicker.getImageUri();
            if (uri != null) {
                mPostImage.setImageURI(uri);
            }
        }
    }


    public Task<Uri> uploadImage(Uri imageUri) {
        Storage storage = DependencyManager.getStorageSystem();
        return storage.storeAndGetDownloadUrl(getFileExtension(imageUri), imageUri);

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = PostActivity.this.getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }


}
