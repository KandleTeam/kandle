package ch.epfl.sdp.kandle.imagePicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.Storage;

import static android.app.Activity.RESULT_OK;

public class ImagePicker {

    //private Activity activity;
    //private Fragment fragment;
    //private Uri imageUri;

    private static final int IMAGE_REQUEST = 1;
    /*public void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (activity != null) {
            activity.startActivityForResult(intent, IMAGE_REQUEST);
        }
        if (fragment != null) {
            fragment.startActivityForResult(intent, IMAGE_REQUEST);
        }
    }*/

    public static void openImage(Activity activity) {
        Intent intent = pickerIntent();
        activity.startActivityForResult(intent, IMAGE_REQUEST);
    }

    public static void openImage(Fragment fragment) {
        Intent intent = pickerIntent();
        fragment.startActivityForResult(intent, IMAGE_REQUEST);
    }

    private static Intent pickerIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return intent;
    }

    /*private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = Kandle.getContext().getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }*/

    public static String getFileExtension(Uri uri) {
        ContentResolver contentResolver = Kandle.getContext().getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public static Uri handleActivityResultAndGetUri(int requestCode, int resultCode, Intent data) {

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            return data.getData();
        }

        return null;
    }

    /*public Uri getImageUri() {
        return imageUri;
    }*/

    public static Task<Uri> uploadImage(Uri imageUri) {
        Storage storage = DependencyManager.getStorageSystem();
        return storage.storeAndGetDownloadUrl(getFileExtension(imageUri), imageUri);
    }
}