package ch.epfl.sdp.kandle.dependencies;

import android.app.Activity;

import com.google.android.gms.location.FusedLocationProviderClient;

public interface MyLocationProvider {

    public FusedLocationProviderClient getFusedLocationProviderClient (Activity activity);

}
