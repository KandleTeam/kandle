package ch.epfl.sdp.kandle.dependencies;

import android.app.Activity;

import com.google.android.gms.location.FusedLocationProviderClient;

public class MockLocation implements MyLocationProvider {
    @Override
    public FusedLocationProviderClient getFusedLocationProviderClient(Activity activity) {
        return new MockFusedLocationProvider(activity);
    }
}
