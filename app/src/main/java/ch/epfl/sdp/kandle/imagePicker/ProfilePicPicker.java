package ch.epfl.sdp.kandle.imagePicker;

import android.app.Activity;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;

import ch.epfl.sdp.kandle.Storage.caching.CachedFirestoreDatabase;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class ProfilePicPicker extends ImagePicker {

    public ProfilePicPicker(Activity activity) {
        super(activity);
    }

    public ProfilePicPicker(Fragment fragment) {
        super(fragment);
    }

    public Task<Void> setProfilePicture() {
        CachedFirestoreDatabase database = new CachedFirestoreDatabase();
        Authentication auth = DependencyManager.getAuthSystem();
        String url = auth.getCurrentUser().getImageURL();
            if (url != null) {
                DependencyManager.getStorageSystem().delete(url);
            }


        return uploadImage().continueWithTask(task -> {
            String sUri = null;
            Uri downloadUri = task.getResult();
            if (downloadUri != null) {
                sUri = downloadUri.toString();
            }

            return database.updateProfilePicture(sUri);
        });
    }
}