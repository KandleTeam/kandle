package ch.epfl.sdp.kandle.imagePicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.ImageStorage;

import static android.app.Activity.RESULT_OK;

public class ImagePicker {

    private static final int IMAGE_REQUEST = 1;

    /**
     * Starts picking an image from the gallery
     *
     * @param activity the activity where to return after picking an image
     */
    public static void openImage(Activity activity) {
        Intent intent = pickerIntent();
        activity.startActivityForResult(intent, IMAGE_REQUEST);
    }

    /**
     * Starts picking an image from the gallery
     *
     * @param fragment the fragment where to return after picking an image
     */
    public static void openImage(Fragment fragment) {
        Intent intent = pickerIntent();
        fragment.startActivityForResult(intent, IMAGE_REQUEST);
    }

    private static Intent pickerIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return intent;
    }

    protected static String getFileExtension(Uri uri) {
        ContentResolver contentResolver = Kandle.getContext().getContentResolver();
        String tmp = contentResolver.getType(uri);
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * Checks the result received from an activity and returns an image uri if the result corresponds to a pick in the gallery
     *
     * @param requestCode requestCode specified when starting the activity
     * @param resultCode  resultCode received in the result
     * @param data        data received in the result
     * @return the uri of the image selected if the activity corresponds to a pick in the gallery, else null
     */
    public static Uri handleActivityResultAndGetUri(int requestCode, int resultCode, Intent data) {

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            return data.getData();
        }

        return null;
    }

    /**
     * Uploads an image in the storage system and returns a download url
     *
     * @param imageUri the uri of the image to store
     * @return a download url to get the image from the storage system
     */
    public static Task<Uri> uploadImage(Uri imageUri) {
        ImageStorage imageStorage = DependencyManager.getStorageSystem();
        return imageStorage.storeAndGetDownloadUrl(getFileExtension(imageUri), imageUri);
    }
}