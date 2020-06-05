package ch.epfl.sdp.kandle.storage;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

public interface ImageStorage {

    Task<Uri> storeAndGetDownloadUrl(String fileExtension, Uri fileUri);

    Task<Void> delete(String path);
}