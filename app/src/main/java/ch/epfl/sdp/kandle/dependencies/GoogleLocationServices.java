package ch.epfl.sdp.kandle.dependencies;

import android.app.Activity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class GoogleLocationServices implements MyLocationProvider {
    @Override
    public FusedLocationProviderClient getFusedLocationProviderClient(Activity activity) {
        return LocationServices.getFusedLocationProviderClient(activity);
    }
}
