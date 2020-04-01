package ch.epfl.sdp.kandle.dependencies;

import android.net.Uri;
import com.google.android.gms.tasks.Task;

public interface Storage {

    Task<Uri> storeAndGetDownloadUrl(String fileExtension, Uri fileUri);

    Task<Void> delete(String path);
}