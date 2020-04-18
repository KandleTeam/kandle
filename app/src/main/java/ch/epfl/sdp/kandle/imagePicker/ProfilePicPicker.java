package ch.epfl.sdp.kandle.imagePicker;

import android.app.Activity;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class ProfilePicPicker extends ImagePicker {

    public static Task<Void> setProfilePicture(Uri imageUri) {
        DependencyManager.getDatabaseSystem().getProfilePicture().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DependencyManager.getStorageSystem().delete(task.getResult());
            }
        });

        return uploadImage(imageUri).continueWithTask(task -> {
            String sUri = null;
            Uri downloadUri = task.getResult();
            if (downloadUri != null) {
                sUri = downloadUri.toString();
            }

            return DependencyManager.getDatabaseSystem().updateProfilePicture(sUri);
        });
    }
}