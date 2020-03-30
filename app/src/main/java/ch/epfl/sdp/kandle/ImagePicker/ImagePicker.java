package ch.epfl.sdp.kandle.ImagePicker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.Storage;

import static android.app.Activity.RESULT_OK;

public class ImagePicker {

    private Activity activity;
    protected Fragment fragment;
    private Uri imageUri;

    private static final int IMAGE_REQUEST = 1;

    public ImagePicker(Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    public ImagePicker(Fragment fragment) {
        this.fragment = fragment;
        this.activity = null;
    }

    public void openImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (activity != null) {
            activity.startActivityForResult(intent, IMAGE_REQUEST);
        }
        if (fragment != null) {
            fragment.startActivityForResult(intent, IMAGE_REQUEST);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = activity != null? activity.getContentResolver() : fragment.getContext().getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public Task<Uri> uploadImage() {
        if (imageUri != null) {
            Storage storage = DependencyManager.getStorageSystem();
            return storage.storeAndGetDownloadUrl(getFileExtension(imageUri), imageUri);
        }
        TaskCompletionSource<Uri> result = new TaskCompletionSource<>();
        result.setResult(null);
        return result.getTask();
    };
}