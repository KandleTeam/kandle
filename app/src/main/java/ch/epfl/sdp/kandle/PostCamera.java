package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.app.Activity.RESULT_OK;

public class PostCamera {

    public static final int PERMISSIONS_REQUEST_CODE = 42;
    public static final int PHOTO_REQUEST = 0;
    private Activity activity;
    private Uri imageUri;
    private ContentValues values;
    private static final String[] permissions = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    /**
     * This creates a Cmaera
     * @param activity
     */
    public PostCamera(Activity activity) {
        this.activity = activity;
        values = new ContentValues();
    }

    /**
     * Opens the camera
     */
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

    /**
     * Gets the Uri of the photo taken
     * @return
     */
    public Uri getImageUri() {
        return imageUri;
    }

    /**
     * When the Photo is taken, returns the image
     * @param requestCode
     * @param resultCode
     * @return
     */
    public Bitmap handleActivityResult(int requestCode, int resultCode) {
        Bitmap imageBitmap = null;
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(
                        activity.getContentResolver(), imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imageBitmap;
    }
}

