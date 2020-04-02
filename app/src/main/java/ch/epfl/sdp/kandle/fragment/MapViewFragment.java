package ch.epfl.sdp.kandle.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ch.epfl.sdp.kandle.PostActivity;
import ch.epfl.sdp.kandle.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap gmap;
    private SupportMapFragment innerMapFragment;

    private boolean isLocationGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        askForLocationPermission();

        innerMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.inner_map_fragment);
        innerMapFragment.getMapAsync(this);

        ImageButton mNewPostButton = view.findViewById(R.id.newPostButton);
        mNewPostButton.setOnClickListener(v -> startActivity(new Intent(getContext(), PostActivity.class)));

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gmap = googleMap;
        gmap.setMyLocationEnabled(true);
        gmap.getUiSettings().setMapToolbarEnabled(false);

        gmap.addMarker(new MarkerOptions()
                .position(new LatLng(46.522636, 6.635391))
                .title("Lausanne Cathedral")).setTag(0);
    }

    private void askForLocationPermission() {

        if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            isLocationGranted = false;
        } else {
            isLocationGranted = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    isLocationGranted = true;
                }
                break;
        }
    }
}