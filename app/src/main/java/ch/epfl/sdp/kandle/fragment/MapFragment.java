package ch.epfl.sdp.kandle.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import ch.epfl.sdp.kandle.R;

public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    public SupportMapFragment mapFragment = null;


//    public MapFragment() {
//        // Required empty public constructor
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);


        if (mapFragment != null) {
            mapFragment.getMapAsync(map -> {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(10, 10))
                        .title("Hello world")).setTag(0);
                //map.setOnMarkerClickListener(this);


            });
        } else {
            Toast.makeText(this.getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

        return v;
    }


    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getContext());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            return false;

        }
    }



    @Override
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void onResume() {
        super.onResume();

        // Display the connection status

        if (abstractLocation.getCurrentLocation() != null) {
            Toast.makeText(this.getContext(), "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(abstractLocation.getCurrentLocation().getLatitude(), abstractLocation.getCurrentLocation().getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            //mMap.animateCamera(cameraUpdate);
        } else {
            Toast.makeText(this.getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        abstractLocation.startLocationUpdates(this.getContext());
    }





    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}