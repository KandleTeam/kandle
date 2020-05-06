package ch.epfl.sdp.kandle.dependencies;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

public class MockLocation implements MyLocationProvider {


    @Override
    public Task<Location> getLocation(Activity activity) {
        TaskCompletionSource source = new TaskCompletionSource<Location>();

        source.setResult(new Location("mock"));

        return source.getTask();
    }
}
