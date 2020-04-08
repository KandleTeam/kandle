package ch.epfl.sdp.kandle.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.maps.android.clustering.ClusterManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
//import com.mapbox.mapboxsdk.maps.Style;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
//import ch.epfl.sdp.kandle.CustomMarker.ClusterManagerRenderer;
//import ch.epfl.sdp.kandle.CustomMarker.CustomMarkerItem;
import ch.epfl.sdp.kandle.MainActivity;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.PostActivity;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MyLocationProvider;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MapViewFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    //private GoogleMap gmap;
   // private SupportMapFragment innerMapFragment;
   // private LatLng latLng;

    private static final int RADIUS = 2000;
    private boolean isLocationGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

   // private ClusterManager clusterManager;
    //private ClusterManagerRenderer clusterManagerRenderer;
    //private CustomMarker customMarker;
    private Database database;
    private Authentication authentication;

    private MyLocationProvider locationProvider;
    private Location currentLocation;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationEngineCallback<LocationEngineResult> callback ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {


        callback = new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                if (result.getLastLocation() != null){

                    currentLocation = result.getLastLocation();
                    mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }

            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        };
        // Inflate the layout for this fragment
        Mapbox.getInstance(this.getContext(), getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        askForLocationPermission();

        locationProvider = DependencyManager.getLocationProvider();
        database = DependencyManager.getDatabaseSystem();
        authentication = DependencyManager.getAuthSystem();

        ImageButton mNewPostButton = view.findViewById(R.id.newPostButton);
        mNewPostButton.setOnClickListener(v -> {
            Intent intent = new Intent( getContext(), PostActivity.class);
            if (currentLocation!=null){
                intent.putExtra("latitude", currentLocation.getLatitude());
                intent.putExtra("longitude", currentLocation.getLongitude());
            }
            startActivity(intent);
        });


        //innerMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.inner_map_fragment);
        //innerMapFragment.getMapAsync(this);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap=mapboxMap;


        locationProvider.getLocation(this.getActivity()).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                currentLocation=location;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                            Drawable drawable = ResourcesCompat.getDrawable(MapViewFragment.this.getResources(), R.drawable.ic_whatshot_24dp, null);
                            Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                            //style.addImage(MARKER_IMAGE, mBitmap);
                            IconFactory iconFactory = IconFactory.getInstance(MapViewFragment.this.getActivity());
                            Icon icon = iconFactory.fromBitmap(mBitmap);
                            enableLocationComponent(style);
                            //addPostMarkers(style);
                            database.getNearbyPosts(currentLocation.getLongitude(), currentLocation.getLatitude(), RADIUS).addOnSuccessListener(new OnSuccessListener<List<Post>>() {
                                @Override
                                public void onSuccess(List<Post> posts) {
                                    for (Post p : posts){
                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(p.getLatitude(), p.getLongitude()))
                                                .title ("A post !")
                                                .icon(icon));
                                    }
                                }
                            });

                    }
                });
            }
        });


    }

    /*private void addPostMarkers(Style loadedMapStyle) {
        List<Feature> features = new ArrayList<>();
        database.getNearbyPosts(currentLocation.getLongitude(), currentLocation.getLatitude(), RADIUS).addOnSuccessListener(new OnSuccessListener<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) {
                for (Post p : posts){
                    features.add(Feature.fromGeometry(Point.fromLngLat(p.getLongitude(), p.getLatitude())));
                }
                // Source: A data source specifies the geographic coordinate where the image marker gets placed.

                loadedMapStyle.addSource(new GeoJsonSource(MARKER_SOURCE, FeatureCollection.fromFeatures(features)));

                // Style layer: A style layer ties together the source and image and specifies how they are displayed on the map.
                loadedMapStyle.addLayer(new SymbolLayer(MARKER_STYLE_LAYER, MARKER_SOURCE)
                        .withProperties(
                                PropertyFactory.iconAllowOverlap(true),
                                PropertyFactory.iconIgnorePlacement(true),
                                PropertyFactory.iconImage(MARKER_IMAGE),
                                // Adjust the second number of the Float array based on the height of your marker image.
                                // This is because the bottom of the marker should be anchored to the coordinate point, rather
                                // than the middle of the marker being the anchor point on the map.
                                PropertyFactory.iconOffset(new Float[] {0f, -52f})
                        ));

            }
        });

    }

     */


    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this.getContext())) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this.getContext())
                    .elevation(5)
                    .accuracyAlpha(.6f)
                    .accuracyColor(Color.RED)
                    .foregroundDrawable(R.drawable.ic_profil_24dp)
                    .build();
            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this.getContext(), loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);


            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this.getActivity());
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this.getContext());
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, this.getActivity().getMainLooper());
        locationEngine.getLastLocation(callback);
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
        /*switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    isLocationGranted = true;
                }
                break;
        }

         */
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //toast to explain permision
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(style -> enableLocationComponent(style));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

}