package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.content.ContentResolver;
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

import java.io.File;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class PostCamera {
    protected Activity activity;
    protected Uri imageUri;
    private static final int PERMISSIONS_REQUEST_CODE = 42;
    ImageView imageView;
    private static final int PHOTO_REQUEST = 0;


    public PostCamera(Activity activity) {
        this.activity = activity;
        imageView = new ImageView(activity);
    }

    public void openCamera(){
        if(allPermissionsGranted(getRequiredPermissions())){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activity.startActivityForResult(intent, PHOTO_REQUEST);
        }
        else {
            makePermissionRequest();
        }
    }

    public Uri handleActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
        return imageUri;
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

}

