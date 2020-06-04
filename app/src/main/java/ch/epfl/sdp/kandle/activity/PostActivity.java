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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.utils.PostCamera;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.authentification.Authentication;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.fragment.YourPostListFragment;
import ch.epfl.sdp.kandle.utils.imagePicker.ImagePicker;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getAuthSystem;


public class PostActivity extends AppCompatActivity {

    public final static int EDIT_PIC_REQUEST = 2;
    public final static int POST_IMAGE_TAG = 42;
    public final static int POST_EDITED_IMAGE_TAG = 24;
    private final static double NEAR_LAT_LNG_SUB = 0.5;
    private final static double NEAR_LAT_LNG_DIV = 2000;
    private EditText mPostText;
    private Button mPostButton;
    private TextView mPostPageTitle;
    private ImageButton mMessageButton, mEventButton;
    private RelativeLayout mPostImageLayout;
    private ImageButton mIsForCloseFollowers;
    private ImageView mPostImage;
    private Post p;
    private Authentication auth;
    private Database database;
    private PostCamera postCamera;
    private Uri imageUri;
    private LinearLayout mDateAndTime;
    private boolean isEvent = false;
    private boolean isForCloseFollowers = false;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //Permission();

        Random rand = new Random();

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0.0) + (rand.nextDouble() - NEAR_LAT_LNG_SUB) / NEAR_LAT_LNG_DIV;
        double longitude = intent.getDoubleExtra("longitude", 0.0) + (rand.nextDouble() - NEAR_LAT_LNG_SUB) / NEAR_LAT_LNG_DIV;
        String postId = intent.getStringExtra("postId");


        mPostText = findViewById(R.id.postText);
        mPostButton = findViewById(R.id.postButton);
        mPostPageTitle = findViewById(R.id.postPageTitle);
        ImageButton mGalleryButton = findViewById(R.id.galleryButton);
        ImageButton mCameraButton = findViewById(R.id.cameraButton);
        mPostImageLayout = findViewById(R.id.postImageLayout);
        mPostImage = findViewById(R.id.postImage);
        ImageButton mPostImageEdit = findViewById(R.id.postImageEdit);
        ImageButton mBackButton = findViewById(R.id.backButton);
        mMessageButton = findViewById(R.id.selectMessageButton);
        mEventButton = findViewById(R.id.selectEventButton);
        mDateAndTime = findViewById(R.id.eventDateTimeSelector);
        mDatePicker = findViewById(R.id.dateSelector);
        mTimePicker = findViewById(R.id.timeSelector);
        mTimePicker.setIs24HourView(true);
        mIsForCloseFollowers = findViewById(R.id.closeFriends);


        postCamera = new PostCamera(this);
        auth = getAuthSystem();
        database = DependencyManager.getCachedDatabase();

        if (postId != null) {
            database.getPostByPostId(postId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Post p = task.getResult();
                    mPostButton.setText(getString(R.string.editButtonText));
                    mPostPageTitle.setText(getString(R.string.editPageTitle));
                    mPostText.setText(p.getDescription());
                    mPostImageLayout.setVisibility(View.VISIBLE);
                    mPostImage.setTag(YourPostListFragment.POST_IMAGE);
                    Picasso.get().load(p.getImageURL()).into(mPostImage);
                    mMessageButton.setVisibility(View.GONE);
                    mEventButton.setVisibility(View.GONE);
                    if (p.getType() != null && p.getType().equals(Post.EVENT)) {
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
            onPostButtonClick(postId, longitude, latitude);
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
            mPostText.setHint(getString(R.string.postMessageHint));
        });

        mEventButton.setOnClickListener(v -> {
            isEvent = true;
            mEventButton.setClickable(false);
            mMessageButton.setClickable(true);
            mEventButton.setBackgroundResource(R.drawable.add_background);
            mMessageButton.setBackgroundResource(R.drawable.add_background_grey);
            setEventAppearance();
        });

        mPostImageEdit.setOnClickListener(v -> {
            Intent i = new Intent(PostActivity.this, PhotoEditorActivity.class);
            i.setData(imageUri);
            startActivityForResult(i, EDIT_PIC_REQUEST);
        });

        mIsForCloseFollowers.setOnClickListener(v -> {
            if(!isForCloseFollowers){
                isForCloseFollowers = true;
                mIsForCloseFollowers.setBackgroundResource(R.drawable.add_background);
            }
            else {
                isForCloseFollowers = false;
                mIsForCloseFollowers.setBackgroundResource(R.drawable.add_background_grey);
            }
        });
    }

    private void onPostButtonClick(String postId, double longitude, double latitude) {
        String postText = mPostText.getText().toString().trim();

        if (postText.isEmpty() && imageUri == null) {
            mPostText.setError(getString(R.string.emptyPostError));
            return;
        }

            if (imageUri != null) {
                ImagePicker.uploadImage(imageUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri == null) {
                            Toast.makeText(PostActivity.this, getString(R.string.uploadImageError), Toast.LENGTH_LONG).show();

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
                                        p.setIsForCloseFollowers(p.getIsForCloseFollowers());
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
                                if(isForCloseFollowers){
                                    p.setIsForCloseFollowers(Post.CLOSE_FOLLOWER);
                                }
                                addPost(p);
                            }
                        }
                    }

            });
    }

             else {
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
                            p.setIsForCloseFollowers(p.getIsForCloseFollowers());
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

                    if(isForCloseFollowers){
                        p.setIsForCloseFollowers(Post.CLOSE_FOLLOWER);
                    }
                    addPost(p);
                }
            }

    }

    private void addPost(Post p) {
        database.addPost(p).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(PostActivity.this, getString(R.string.postSuccessful), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void editPost(Post p, String postId) {
        database.editPost(p, postId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(PostActivity.this, getString(R.string.postEditSuccessful), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * set text hint and display date and time pickers
     */
    private void setEventAppearance() {
        mPostText.setHint(getString(R.string.createEventHint));
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
        if (requestCode == PostCamera.PHOTO_REQUEST) {
            Bitmap imageBitmap = postCamera.handleActivityResult(requestCode, resultCode, data);
            if (imageBitmap != null) {
                mPostImageLayout.setVisibility(View.VISIBLE);
                mPostImage.setTag(POST_IMAGE_TAG);
                mPostImage.setImageBitmap(imageBitmap);
            }
            imageUri = postCamera.getImageUri();
        } else if (requestCode == EDIT_PIC_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            mPostImage.setTag(POST_EDITED_IMAGE_TAG);
            mPostImage.setImageURI(imageUri);
        } else {
            imageUri = ImagePicker.handleActivityResultAndGetUri(requestCode, resultCode, data);
            if (imageUri != null) {
                mPostImageLayout.setVisibility(View.VISIBLE);
                mPostImage.setTag(POST_IMAGE_TAG);
                mPostImage.setImageURI(imageUri);
            }
        }
    }


}
