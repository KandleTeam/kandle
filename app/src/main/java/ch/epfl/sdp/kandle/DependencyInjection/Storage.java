package ch.epfl.sdp.kandle.DependencyInjection;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

public abstract class Storage {

    private static Storage storageSystem = CloudStorage.getInstance();

    public static void setStorageSystem(Storage storage) {
        storageSystem = storage;
    }

    public static Storage getStorageSystem() {
        return storageSystem;
    }

    public abstract Task<Uri> storeAndGetDownloadUrl(String fileExtension, Uri fileUri);
}