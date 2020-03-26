package ch.epfl.sdp.kandle.ImagePicker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.Storage;

public class ProfilePicPicker extends ImagePicker {

    public ProfilePicPicker(Activity activity) {
        super(activity);
    }
    public ProfilePicPicker(Fragment fragment) {super(fragment); }

    @Override
    protected void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(activity  != null? activity : fragment.getContext());
        pd.setMessage("Uploading");
        pd.show();

        Storage storage = DependencyManager.getStorageSystem();
        storage.storeAndGetDownloadUrl(getFileExtension(imageUri), imageUri).addOnCompleteListener(task -> {
            //if (task.isSuccessful() && task.getResult()!=null) {
                Uri downloadUri = task.getResult();
                String sUri = downloadUri.toString();

                DependencyManager.getDatabaseSystem().updateProfilePicture(sUri);

            /*} else {
                Toast.makeText(activity, "Failed!", Toast.LENGTH_SHORT).show();
            }
            pd.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();*/
            pd.dismiss();
        });


    }
}