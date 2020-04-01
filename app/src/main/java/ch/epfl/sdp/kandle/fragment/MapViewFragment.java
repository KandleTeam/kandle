package ch.epfl.sdp.kandle.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import androidx.fragment.app.FragmentTransaction;
import ch.epfl.sdp.kandle.CustomMarker.ClusterManagerRenderer;
import ch.epfl.sdp.kandle.CustomMarker.CustomMarkerItem;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.PostActivity;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;


public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    Location currentLocation;
    LatLng latLng;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;

    private GoogleMap gmap;
    private SupportMapFragment innerMapFragment;

    private ClusterManager clusterManager;
    private ClusterManagerRenderer clusterManagerRenderer;
    //private CustomMarker customMarker;
    private Database database;
    private Authentication authentication;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        getLocation();

        database = DependencyManager.getDatabaseSystem();
        authentication = DependencyManager.getAuthSystem();

        //innerMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.inner_map_fragment);
        //innerMapFragment.getMapAsync(this);

        ImageButton mNewPostButton = view.findViewById(R.id.newPostButton);
        //mNewPostButton.setOnClickListener(v -> startActivity(new Intent(getContext(), PostActivity.class)));
        mNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( getContext(), PostActivity.class);
                intent.putExtra("latitude", latLng.latitude);
                intent.putExtra("longitude", latLng.longitude);
                startActivity(intent);
            }
        });
        return view;
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    currentLocation = task.getResult();
                    innerMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.inner_map_fragment);
                    innerMapFragment.getMapAsync(MapViewFragment.this::onMapReady);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gmap = googleMap;

        this.latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());


        /*gmap.addMarker(new MarkerOptions()
                .position(new LatLng(46.522636, 6.635391))
                .title("Lausanne Cathedral")).setTag(0);

         */
       // gmap.animateCamera(CameraUpdateFactory.newLatLng(this.latLng));
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        addUserMarker();
        addPostsMarkers();
    }

    private void addPostsMarkers() {

        database.getNearbyPosts(latLng.longitude, latLng.latitude, 2000).addOnCompleteListener(new OnCompleteListener<List<Post>>() {
            @Override
            public void onComplete(@NonNull Task<List<Post>> task) {
                if (task.isSuccessful()){
                    for (Post post : task.getResult()){
                        CustomMarkerItem customMarkerItem = new CustomMarkerItem( new LatLng(post.getLatitude(), post.getLongitude()),
                                                                                    "A post", post.getDate().toString(), null, CustomMarkerItem.Type.POST);
                        clusterManager.addItem(customMarkerItem);


                    }
                    clusterManager.cluster();
                }
                else {
                    //TODO handle case when user is offline (get posts from cache)
                }
            }
        });

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


            database.getProfilePicture().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful()){
                        String imageUrl = task.getResult();

                        database.getNickname().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (task.isSuccessful()){
                                    String title = task.getResult();
                                    CustomMarkerItem customMarker = new CustomMarkerItem( latLng, title, snippet, imageUrl, CustomMarkerItem.Type.USER );
                                    System.out.println("item");
                                    clusterManager.addItem(customMarker);
                                    clusterManager.cluster();
                                }
                                else {
                                    //TODO handle case when user is offline (get nickname from cache)
                                }
                            }
                        });

                    }
                    else {
                        //TODO handle case when user is offline (get picture from cache)
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String [] permissions, @NonNull int [] grantResults){
        switch (requestCode){
            case REQUEST_CODE :
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    //FragmentTransaction ft = getFragmentManager().beginTransaction();
                    //ft.detach(this).attach(this).commit();
                }
                break;
        }
    }
}