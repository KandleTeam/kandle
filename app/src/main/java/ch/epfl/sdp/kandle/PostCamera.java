package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class PostCamera{

    protected Activity activity;
    protected Uri imageUri;
    protected static final int PERMISSIONS_REQUEST_CODE = 42;
    ImageView imageView;
    protected static final int PHOTO_REQUEST = 0;
    ContentValues values;

    public PostCamera(Activity activity) {
        this.activity = activity;
        imageView = new ImageView(activity);
        values = new ContentValues();
    }

    public  void openCamera(){
        if(allPermissionsGranted(getRequiredPermissions())){
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = activity.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            activity.startActivityForResult(intent, PHOTO_REQUEST);
        }
        else {
            makePermissionRequest();
        }
    }

    protected void uploadImage(){

    }

    private void makePermissionRequest() {
        ActivityCompat.requestPermissions(activity, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
    }
    /** Returns true if all the necessary permissions have been granted already. */
    private boolean allPermissionsGranted(String[] permissionsTab) {
        for (String permission : permissionsTab) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Tries to acquire all the necessary permissions through a dialog. */
    private String[] getRequiredPermissions() {
        PackageInfo info;

        try {
            info =
                    activity.getPackageManager()
                            .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException exception) {
            Log.e(TAG, "Failed to obtain all required permissions.", exception);
            return new String[0];
        }
        String[] finalPermissions = info.requestedPermissions;
        String[] permissions = new String[2];
        int j = 0;
        for(int i = 0; i < finalPermissions.length; i++){
            String s = finalPermissions[i];
            if((s.equals("android.permission.CAMERA") ||  s.equals("android.permission.WRITE_EXTERNAL_STORAGE")) && j < 2){
                permissions[j] = s;
                j++;
            }
        }
        if (permissions != null && permissions.length > 0) {
            return permissions;
        } else {
            return new String[0];
        }
    }

    public Bitmap handleActivityResult(int requestCode, int resultCode, Intent data){
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

