package ch.epfl.sdp.kandle.dependencies;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import androidx.annotation.NonNull;

public class MockFusedLocationProvider extends FusedLocationProviderClient {
    public MockFusedLocationProvider(@NonNull Context context) {
        super(context);
    }

    public MockFusedLocationProvider(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public Task<Location> getLastLocation() {
        TaskCompletionSource source = new TaskCompletionSource<Location>();

        source.setResult( new Location("mock") );

        return source.getTask();
    }
}
