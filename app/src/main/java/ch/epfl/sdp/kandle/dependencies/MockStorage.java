package ch.epfl.sdp.kandle.dependencies;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

public class MockStorage implements Storage {

    //TODO pass boolean as argument and return not URL is the boolean is false this will increase code coverage

    @Override
    public Task<Uri> storeAndGetDownloadUrl(String fileExtension, Uri fileUri) {
        TaskCompletionSource<Uri> source = new TaskCompletionSource<>();
        Uri imageUri = Uri.parse("android.resource://ch.epfl.sdp.kandle/drawable/ic_launcher_background.xml");
        source.setResult(imageUri);
        return source.getTask();
    }

    @Override
    public Task<Void> delete(String path) {
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();
    }

}