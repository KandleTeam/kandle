package ch.epfl.sdp.kandle.ImagePicker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.widget.Toast;

import ch.epfl.sdp.kandle.DependencyInjection.Database;
import ch.epfl.sdp.kandle.DependencyInjection.Storage;

public class ProfilePicPicker extends ImagePicker {

    public ProfilePicPicker(Activity activity) {
        super(activity);
    }

    @Override
    protected void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(activity);
        pd.setMessage("Uploading");
        pd.show();

        Storage storage = Storage.getStorageSystem();
        storage.storeAndGetDownloadUrl(getFileExtension(imageUri), imageUri).addOnCompleteListener(task -> {
            //if (task.isSuccessful() && task.getResult()!=null) {
                Uri downloadUri = task.getResult();
                String sUri = downloadUri.toString();

                Database.getDatabaseSystem().updateProfilePicture(sUri);

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