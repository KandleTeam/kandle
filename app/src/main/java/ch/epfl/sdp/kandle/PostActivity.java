package ch.epfl.sdp.kandle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.concurrent.ExecutionException;

import ch.epfl.sdp.kandle.CameraXActivity;
import ch.epfl.sdp.kandle.ImagePicker.PostImagePicker;

public class PostActivity extends AppCompatActivity implements LifecycleOwner {


    EditText mPostText;
    private ImageCapture imageCapture;
    final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1024;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Button mPostButton;
    ImageButton mGalleryButton, mCameraButton;
    ImageView mPostImage;
    private PostImagePicker postImagePicker;
    public final static int POST_IMAGE_TAG = 42;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    private Camera mCamera;
    private Uri imageUri;
    private static final int TAKE_PICTURE = 1;



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

        fStore = FirebaseFirestore.getInstance();

        fAuth = FirebaseAuth.getInstance();

        mPostButton.setOnClickListener(v -> {
            String postText  = mPostText.getText().toString().trim();

            if(postText.isEmpty()){
                mPostText.setError("Your post is empty...");
                return;
            }

            Toast.makeText(PostActivity.this, "You have successfully posted : " + postText, Toast.LENGTH_LONG ).show();
            finish();

        });

        mCameraButton.setOnClickListener(v -> {


            /*PreviewView previewView = findViewById(R.id.previewView);
            cameraProviderFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }, ContextCompat.getMainExecutor(this));

            ListenableFuture cameraProviderFuture =
                    ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    // Camera provider is now guaranteed to be available
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Set up the view finder use case to display camera preview
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());

                    // Set up the capture use case to allow users to take photos
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build();

                    OrientationEventListener orientationEventListener = new OrientationEventListener((Context)this) {
                        @Override
                        public void onOrientationChanged(int orientation) {
                            int rotation;

                            // Monitors orientation values to determine the target rotation value
                            if (orientation >= 45 && orientation < 135) {
                                rotation = Surface.ROTATION_270;
                            } else if (orientation >= 135 && orientation < 225) {
                                rotation = Surface.ROTATION_180;
                            } else if (orientation >= 225 && orientation < 315) {
                                rotation = Surface.ROTATION_90;
                            } else {
                                rotation = Surface.ROTATION_0;
                            }

                            imageCapture.setTargetRotation(rotation);
                        }
                    };

                    orientationEventListener.enable();


                    // Choose the camera by requiring a lens facing
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build();

                    // Attach use cases to the camera with the same lifecycle owner
                    cameraProvider.bindToLifecycle(
                            ((LifecycleOwner) this),
                            cameraSelector,
                            preview,
                            imageCapture);
                } catch (InterruptedException | ExecutionException e) {
                    // Currently no exceptions thrown. cameraProviderFuture.get() should
                    // not block since the listener is being called, so no need to
                    // handle InterruptedException.
                }
            }, ContextCompat.getMainExecutor(this));*/

            Intent intent = new Intent(this, CameraXActivity.class);
            File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
            /*intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photo));
            imageUri = Uri.fromFile(photo);*/
            startActivity(intent);
            //CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE
        });


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
    /*void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }*/




}
/*
 @Override
    public void onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

     private void startCamera(){

    }

    private void updateTransform(){

    }

if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                ivPreview.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        else {*/
