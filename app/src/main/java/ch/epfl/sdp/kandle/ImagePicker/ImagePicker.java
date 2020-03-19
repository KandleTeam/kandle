package ch.epfl.sdp.kandle.ImagePicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import static android.app.Activity.RESULT_OK;

public abstract class ImagePicker {

    protected Activity activity;
    protected Uri imageUri;

    private static final int IMAGE_REQUEST = 1;

    public ImagePicker(Activity activity) {
        this.activity = activity;
    }

    public void openImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, IMAGE_REQUEST);
    }

    protected String getFileExtension(Uri uri){
        ContentResolver contentResolver = activity.getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public Uri handleActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
        return imageUri;
    }

    protected abstract void uploadImage();
}