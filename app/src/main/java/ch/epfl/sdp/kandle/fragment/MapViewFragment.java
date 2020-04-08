package ch.epfl.sdp.kandle.fragment;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import ch.epfl.sdp.kandle.CustomMarker.ClusterManagerRenderer;
import ch.epfl.sdp.kandle.CustomMarker.CustomMarkerItem;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.PostActivity;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MyLocationProvider;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap gmap;
    private SupportMapFragment innerMapFragment;
    private LatLng latLng;

    private static final int radius = 2000;
    private boolean isLocationGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private ClusterManager clusterManager;
    private ClusterManagerRenderer clusterManagerRenderer;
    //private CustomMarker customMarker;
    private Database database;
    private Authentication authentication;

    private MyLocationProvider locationProvider;
    private Location currentLocation;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        askForLocationPermission();

        locationProvider = DependencyManager.getLocationProvider();
        database = DependencyManager.getDatabaseSystem();
        authentication = DependencyManager.getAuthSystem();


        innerMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.inner_map_fragment);
        innerMapFragment.getMapAsync(this);

        ImageButton mNewPostButton = view.findViewById(R.id.newPostButton);
        mNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getContext(), PostActivity.class);
                if (latLng!=null) {
                    intent.putExtra("latitude", latLng.latitude);
                    intent.putExtra("longitude", latLng.longitude);
                }
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gmap = googleMap;

        gmap.getUiSettings().setMapToolbarEnabled(false);

        locationProvider.getLocation(this.getActivity()).addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                currentLocation = task.getResult();
                latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                addUserMarker();
                addPostsMarkers();
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            }
        });

        //gmap.setMyLocationEnabled(true);
        gmap.getUiSettings().setMapToolbarEnabled(false);


        gmap.addMarker(new MarkerOptions()
                .position(new LatLng(46.522636, 6.635391))
                .title("Lausanne Cathedral")).setTag(0);


    }
    private void addUserMarker() {
        if(gmap!=null){

            if (clusterManager == null){
                clusterManager = new ClusterManager<CustomMarkerItem>( getActivity().getApplicationContext(), gmap);
            }

            if (clusterManagerRenderer == null){
                clusterManagerRenderer = new ClusterManagerRenderer(getActivity(), gmap, clusterManager);
                clusterManager.setRenderer(clusterManagerRenderer);
            }

            String snippet = "You !";


            database.getProfilePicture().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    String imageUrl = task.getResult();

                    database.getNickname().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()){
                            String title = task1.getResult();
                            CustomMarkerItem customMarker = new CustomMarkerItem( latLng, title, snippet, imageUrl, CustomMarkerItem.Type.USER );
                            clusterManager.addItem(customMarker);
                            clusterManager.cluster();
                        }
                        else {
                            //TODO handle case when user is offline (get nickname from cache)
                        }
                    });

                }
                else {
                    //TODO handle case when user is offline (get picture from cache)
                }
            });
        }
    }



    private void addPostsMarkers() {

        database.getNearbyPosts(latLng.longitude, latLng.latitude, radius).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (Post post : task.getResult()){
                    CustomMarkerItem customMarkerItem = new CustomMarkerItem( new LatLng(post.getLatitude(), post.getLongitude()),
                            post.getDescription(), post.getDate().toString(), null, CustomMarkerItem.Type.POST);
                    clusterManager.addItem(customMarkerItem);


                }
                clusterManager.cluster();
            }
            else {
                //TODO handle case when user is offline (get posts from cache)
            }
        });

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