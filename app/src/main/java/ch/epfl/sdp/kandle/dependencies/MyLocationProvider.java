package ch.epfl.sdp.kandle.dependencies;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.tasks.Task;

public interface MyLocationProvider {

    Task<Location> getLocation(Activity activity);

}
