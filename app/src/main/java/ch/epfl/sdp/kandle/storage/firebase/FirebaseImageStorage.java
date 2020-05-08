package ch.epfl.sdp.kandle.storage.firebase;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import ch.epfl.sdp.kandle.dependencies.ImageStorage;

public class FirebaseImageStorage implements ImageStorage {


    private static final StorageReference STORAGE_REFERENCE = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl("gs://kandle-1b646.appspot.com");
    private static final FirebaseImageStorage INSTANCE = new FirebaseImageStorage();

    public static FirebaseImageStorage getInstance() {
        return INSTANCE;
    }


    @Override
    public Task<Uri> storeAndGetDownloadUrl(String fileExtension, Uri fileUri) {
        String path = System.currentTimeMillis() + "." + fileExtension;
        final StorageReference fileReference = STORAGE_REFERENCE.child(path);
        UploadTask uploadTask = fileReference.putFile(fileUri);
        return uploadTask.continueWithTask(task -> fileReference.getDownloadUrl());
    }

    @Override
    public Task<Void> delete(String path) {
        final StorageReference reference = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(path);
        return reference.delete();
    }
}