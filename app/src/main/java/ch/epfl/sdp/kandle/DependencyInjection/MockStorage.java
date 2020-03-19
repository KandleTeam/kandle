package ch.epfl.sdp.kandle.DependencyInjection;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

public class MockStorage extends Storage {

    @Override
    public Task<Uri> storeAndGetDownloadUrl(String fileExtension, Uri fileUri) {
        TaskCompletionSource<Uri> source = new TaskCompletionSource<>();
        source.setResult(Uri.EMPTY);
        return source.getTask();
    }

}