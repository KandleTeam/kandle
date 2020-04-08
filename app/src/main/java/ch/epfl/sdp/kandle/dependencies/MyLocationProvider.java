package ch.epfl.sdp.kandle.dependencies;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;

public interface MyLocationProvider {

    public Task<Location> getLocation(Activity activity);

}
