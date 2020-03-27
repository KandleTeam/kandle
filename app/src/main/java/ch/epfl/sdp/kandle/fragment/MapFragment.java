package ch.epfl.sdp.kandle.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import ch.epfl.sdp.kandle.AbstractLocation;
import ch.epfl.sdp.kandle.PostActivity;
import ch.epfl.sdp.kandle.R;

public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    public SupportMapFragment mapFragment = null;
    //Location mCurrentLocation;
    //private ImageButton mCreatePost;
    public AbstractLocation abstractLocation = new AbstractLocation(this.getContext(), null);


    private final static String KEY_LOCATION = "location";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public MapFragment() {
        // Required empty public constructor
    }


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

        if(isGooglePlayServicesAvailable()) {
            mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_support);
        }


//        mCreatePost = v.findViewById(R.id.createPostBtn);
//        mCreatePost.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MapFragment.super.onSaveInstanceState(savedInstanceState);
//                startActivity(new Intent(getActivity().getApplicationContext(), PostActivity.class));
//            }
//        });


        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    abstractLocation.loadMap(map,getContext());
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(10, 10))
                            .title("Hello world")).setTag(0);
                    //map.setOnMarkerClickListener(this);


                }
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



    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, abstractLocation.getCurrentLocation());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();
        System.out.println("Hey");
        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this.getContext(),
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }








}