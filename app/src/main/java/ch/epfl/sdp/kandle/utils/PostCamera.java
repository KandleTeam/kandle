package ch.epfl.sdp.kandle.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.app.Activity.RESULT_OK;

public class PostCamera {

    public static final int PHOTO_REQUEST = 0;
    protected static final int PERMISSIONS_REQUEST_CODE = 42;
    protected Activity activity;
    protected Uri imageUri;
    ImageView imageView;
    ContentValues values;
    private String[] permissions = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    public PostCamera(Activity activity) {
        this.activity = activity;
        imageView = new ImageView(activity);
        values = new ContentValues();
    }

    public void openCamera() {
        if (allPermissionsGranted(permissions)) {
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = activity.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            activity.startActivityForResult(intent, PHOTO_REQUEST);
        } else {
            makePermissionRequest();
        }
    }

    protected void uploadImage() {
    }

    private void makePermissionRequest() {
        ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST_CODE);
    }

    /**
     * Returns true if all the necessary permissions have been granted already.
     */
    private boolean allPermissionsGranted(String[] permissionsTab) {
        for (String permission : permissionsTab) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public Bitmap handleActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap imageBitmap = null;
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(
                        activity.getContentResolver(), imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            uploadImage();
        }
        return imageBitmap;
    }
}

