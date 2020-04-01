package ch.epfl.sdp.kandle.ImagePicker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class ProfilePicPicker extends ImagePicker {

    public ProfilePicPicker(Activity activity) {
        super(activity);
    }

    public ProfilePicPicker(Fragment fragment) {
        super(fragment);
    }

    public Task<Void> setProfilePicture() {
        DependencyManager.getDatabaseSystem().getProfilePicture().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DependencyManager.getStorageSystem().delete(task.getResult());
            }
        });

        return uploadImage().continueWithTask(task -> {
            String sUri = null;
            Uri downloadUri = task.getResult();
            if (downloadUri != null) {
                sUri = downloadUri.toString();
            }

            return DependencyManager.getDatabaseSystem().updateProfilePicture(sUri);
        });
    }
}