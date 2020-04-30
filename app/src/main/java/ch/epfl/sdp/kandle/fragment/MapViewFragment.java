package ch.epfl.sdp.kandle.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.maps.android.SphericalUtil;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.List;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.activity.PostActivity;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MyLocationProvider;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private static final String MARKER_SOURCE = "markers-source";
    private static final String MARKER_STYLE_LAYER = "markers-style-layer";
    private static final String MARKER_IMAGE = "custom-marker";
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private static final int RADIUS = 2000;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public int numMarkers;
    private CachedFirestoreDatabase database;
    private Authentication authentication;

    private MyLocationProvider locationProvider;
    private Location currentLocation;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationEngineCallback<LocationEngineResult> callback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {


        callback = new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                if (result.getLastLocation() != null) {

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

        locationProvider = DependencyManager.getLocationProvider();
        database = new CachedFirestoreDatabase();
        authentication = DependencyManager.getAuthSystem();

        ImageButton mNewPostButton = view.findViewById(R.id.newPostButton);
        if (LoggedInUser.isGuestMode()) {
            mNewPostButton.setVisibility(View.GONE);
        } else {
            mNewPostButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), PostActivity.class);
                if (currentLocation != null) {
                    intent.putExtra("latitude", currentLocation.getLatitude());
                    intent.putExtra("longitude", currentLocation.getLongitude());
                }
                startActivity(intent);
            });
        }


        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;


        locationProvider.getLocation(this.getActivity()).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                if (task.getResult() != null) {


                    mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {

                        Drawable drawable = ResourcesCompat.getDrawable(MapViewFragment.this.getResources(), R.drawable.ic_whatshot_24dp, null);
                        Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                        //style.addImage(MARKER_IMAGE, mBitmap);
                        IconFactory iconFactory = IconFactory.getInstance(MapViewFragment.this.getActivity());
                        Icon icon = iconFactory.fromBitmap(mBitmap);
                        enableLocationComponent(style);
                        currentLocation = task.getResult();
                        //addPostMarkers(style);
                        numMarkers = 0;
                        database.getNearbyPosts(currentLocation.getLongitude(), currentLocation.getLatitude(), RADIUS).addOnSuccessListener(posts -> {
                            for (Post p : posts) {
                                numMarkers++;
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(p.getLatitude(), p.getLongitude()))
                                        .title("A post !")
                                        .icon(icon))
                                        .setSnippet(p.getPostId());
                            }
                        });
                    });

                    mapboxMap.setOnMarkerClickListener(marker -> {
                        goToPostFragment(marker.getSnippet(), task.getResult());
                        return true;

                    });

                    mapView.setContentDescription("MAP READY");
                } else {
                    Toast.makeText(this.getActivity(), "Enable GPS", Toast.LENGTH_LONG).show();
                }
            } else {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this.getActivity());
                //Toast.makeText(this.getActivity(), "An error has occurred : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    public void goToPostFragment(String postId, Location location) {
        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
        database.getPostByPostId(postId).addOnSuccessListener(post -> database.getUserById(post.getUserId()).addOnSuccessListener(
                user -> fragmentManager.beginTransaction()
                        .replace(R.id.flContent, PostFragment.newInstance(post, location, user
                                , computeDistance(location.getLatitude(), location.getLongitude(), post.getLatitude(), post.getLongitude())))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit()));
    }

    private int computeDistance(double userLatitude, double userLongitude, double postLatitude, double postLongitude) {

        com.google.android.gms.maps.model.LatLng startLatLng = new com.google.android.gms.maps.model.LatLng(userLatitude, userLongitude);
        com.google.android.gms.maps.model.LatLng endLatLng = new com.google.android.gms.maps.model.LatLng(postLatitude, postLongitude);
        return (int) SphericalUtil.computeDistanceBetween(startLatLng, endLatLng);

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
    @SuppressWarnings({"MissingPermission"})
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this.getActivity(), "You have to grant location permission to see nearby posts", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
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