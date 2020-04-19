package ch.epfl.sdp.kandle.imagePicker;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class ProfilePicPicker extends ImagePicker {


    /**
     * Replaces the profile picture of the connected user in the database and deletes the previous profile picture from the storage system
     * @param imageUri the uri of the new profile picture
     * @return a task finishing when the new profile picture has been stored and set in the database
     */
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