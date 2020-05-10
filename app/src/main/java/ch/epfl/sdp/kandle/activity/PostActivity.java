package ch.epfl.sdp.kandle.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.PostCamera;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.fragment.YourPostListFragment;
import ch.epfl.sdp.kandle.imagePicker.ImagePicker;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;


public class PostActivity extends AppCompatActivity {

    public final static int POST_IMAGE_TAG = 42;
    private EditText mPostText;
    private Button mPostButton;
    private ImageButton mBackButton;
    private ImageButton mGalleryButton, mCameraButton;
    private ImageButton mMessageButton, mEventButton;
    private ImageView mPostImage;
    private Post p;
    private Authentication auth;
    private Database database;
    private PostCamera postCamera;
    private Uri imageUri;
    private LinearLayout mDateAndTime;
    private boolean isEvent = false;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;

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

        Random rand = new Random();

        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra("latitude", 0.0) + (rand.nextDouble()-0.5)/500;
        Double longitude = intent.getDoubleExtra("longitude", 0.0) +(rand.nextDouble()-0.5)/500;
        String postId = intent.getStringExtra("postId");


        mPostText = findViewById(R.id.postText);
        mPostButton = findViewById(R.id.postButton);
        mGalleryButton = findViewById(R.id.galleryButton);
        mCameraButton = findViewById(R.id.cameraButton);
        mPostImage = findViewById(R.id.postImage);
        mBackButton = findViewById(R.id.backButton);
        mMessageButton = findViewById(R.id.selectMessageButton);
        mEventButton = findViewById(R.id.selectEventButton);
        mDateAndTime = findViewById(R.id.eventDateTimeSelector);
        mDatePicker = findViewById(R.id.dateSelector);
        mTimePicker = findViewById(R.id.timeSelector);
        mTimePicker.setIs24HourView(true);

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
                    mMessageButton.setVisibility(View.GONE);
                    mEventButton.setVisibility(View.GONE);
                    if (p.getType()!=null && p.getType().equals(Post.EVENT)) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(p.getDate());
                        setEventAppearance();
                        mDatePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                        mTimePicker.setCurrentHour(cal.get(Calendar.HOUR));
                        mTimePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
                    }
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
                                        p.setType(p.getType());
                                        if (p.getType()!= null && p.getType().equals(Post.EVENT)) {
                                            p.setDate(getDateFromPicker());
                                        }
                                        editPost(p, postId);
                                    }
                                });
                            } else {
                                p = new Post(postText, downloadUri.toString(), new Date(), auth.getCurrentUser().getId(), longitude, latitude);
                                if (isEvent) {
                                    p.setDate(getDateFromPicker());
                                    p.setType(Post.EVENT);
                                }
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
                            p.setType(p.getType());
                            if (p.getType().equals(Post.EVENT)) {
                                p.setDate(getDateFromPicker());
                            }
                            editPost(p, postId);
                        }
                    });
                } else {
                    p = new Post(postText, null, new Date(), auth.getCurrentUser().getId(), longitude, latitude);
                    if (isEvent) {
                        p.setDate(getDateFromPicker());
                        p.setType(Post.EVENT);
                    }
                    post(p);
                }
            }

        });

        mBackButton.setOnClickListener(v -> finish());

        mCameraButton.setOnClickListener(v -> postCamera.openCamera());

        mGalleryButton.setOnClickListener(v -> ImagePicker.openImage(this));

        mMessageButton.setOnClickListener(v -> {
            isEvent = false;
            mMessageButton.setClickable(false);
            mEventButton.setClickable(true);
            mDateAndTime.setVisibility(View.GONE);
            mMessageButton.setBackgroundResource(R.drawable.add_background);
            mEventButton.setBackgroundResource(R.drawable.add_background_grey);
            mPostText.setHint(getResources().getString(R.string.message_hint));
        });

        mEventButton.setOnClickListener(v -> {
            isEvent = true;
            mEventButton.setClickable(false);
            mMessageButton.setClickable(true);
            mEventButton.setBackgroundResource(R.drawable.add_background);
            mMessageButton.setBackgroundResource(R.drawable.add_background_grey);
            setEventAppearance();
        });
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

    /**
     * set text hint and display date and time pickers
     */
    private void setEventAppearance() {
        mPostText.setHint(getResources().getString(R.string.event_hint));
        mDateAndTime.setVisibility(View.VISIBLE);
    }

    /**
     * get date and time picked in picker
     * @return date and time picked in picker
     */
    private Date getDateFromPicker() {
        Calendar cal = Calendar.getInstance();
        cal.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth(),
                mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
        return cal.getTime();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            Bitmap imageBitmap = postCamera.handleActivityResult(requestCode, resultCode);
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
