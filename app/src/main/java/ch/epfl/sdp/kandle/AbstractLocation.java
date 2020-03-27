package ch.epfl.sdp.kandle;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class AbstractLocation {

    private Context context;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;

    public AbstractLocation(Context context, Location mCurrentLocation){
        this.context = context;
        this.mCurrentLocation = mCurrentLocation;
    }

    public void setCurrentLocation(Location mCurrentLocation){
        this.mCurrentLocation = mCurrentLocation;
    }

    public Location getCurrentLocation(){
        return mCurrentLocation;
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void startLocationUpdates(Context context) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(context).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        mCurrentLocation = onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }



    public Location onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return null;
        }

        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
        return location;
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void loadMap(GoogleMap googleMap, Context context) {
        mMap = googleMap;
        if (mMap != null) {
            // Map is ready
            Toast.makeText(context, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            getMyLocation(context);
            startLocationUpdates(context);

        } else {
            Toast.makeText(context, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void getMyLocation(Context context) {
        mMap.setMyLocationEnabled(true);
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(context);
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                        // mMap.animateCamera();
                        onLocationChanged(location);
                    } else {
                        Log.d("Kandle>Location", "Location client returned null location");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("MapDemoActivity", "Error trying to get last GPS location");
                    e.printStackTrace();
                });
    }

}