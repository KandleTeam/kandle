package ch.epfl.sdp.kandle.location;

import android.app.Activity;
import android.location.Location;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

public class GoogleLocationServices implements MyLocationProvider {

    @Override
    public Task<Location> getLocation(Activity activity) {
        return LocationServices.getFusedLocationProviderClient(activity).getLastLocation();
    }
}
