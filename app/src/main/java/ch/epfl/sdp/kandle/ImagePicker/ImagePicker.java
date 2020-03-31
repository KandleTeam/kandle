package ch.epfl.sdp.kandle.ImagePicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public abstract class ImagePicker {

    protected Activity activity;
    protected Fragment fragment;
    protected Uri imageUri;

    public static final int IMAGE_REQUEST = 1;

    public ImagePicker(Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    public ImagePicker(Fragment fragment) {
        this.fragment = fragment;
        this.activity = null;
    }

    public void openImage() {
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

    protected String getFileExtension(Uri uri) {
        ContentResolver contentResolver = activity != null ? activity.getContentResolver() : fragment.getContext().getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public Uri handleActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
        return imageUri;
    }

    protected abstract void uploadImage();
}