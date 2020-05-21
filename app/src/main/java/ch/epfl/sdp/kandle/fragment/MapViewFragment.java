package ch.epfl.sdp.kandle.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
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

import java.util.Date;
import java.util.List;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.activity.OfflineGameActivity;
import ch.epfl.sdp.kandle.activity.PostActivity;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.MyLocationProvider;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class MapViewFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private static final int RADIUS = 2000;
    // Default map position, zoomed out over California
    private static final CameraPosition defaultCameraPosition = new CameraPosition.Builder().zoom(3).target(new LatLng(37.144579, -121.905870)).build();


    private static Icon postIcon, landmarkIcon;
    private int numMarkers;

    private CachedFirestoreDatabase database;
    private Authentication authentication;
    private MyLocationProvider locationProvider;
    private Location currentLocation;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationEngineCallback<LocationEngineResult> onLocationUpdateCallback;

    private ImageButton mGameButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {


        onLocationUpdateCallback = new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                if (result.getLastLocation() != null) {

                    Log.i("LocationEngine", "Location updated");

                    currentLocation = result.getLastLocation();
                    populateWithMarkers();
                    mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w("LocationEngine", "Location update failed");
            }
        };
        // Inflate the layout for this fragment
        Mapbox.getInstance(this.getContext(), getString(R.string.mapbox_access_token));
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Load icons
        IconFactory iconFactory = IconFactory.getInstance(getActivity());
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_whatshot_24dp, null);
        Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
        postIcon = iconFactory.fromBitmap(mBitmap);

        Drawable drawableLandMark = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_place_red_50dp, null);
        Bitmap mBitmapLandmark = BitmapUtils.getBitmapFromDrawable(drawableLandMark);
        landmarkIcon = iconFactory.fromBitmap(mBitmapLandmark);

        locationProvider = DependencyManager.getLocationProvider();
        database = new CachedFirestoreDatabase();
        authentication = DependencyManager.getAuthSystem();

        mGameButton = view.findViewById(R.id.startOfflineGameConnectedButton);

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

        if (!DependencyManager.getNetworkStateSystem().isConnected()) {
            mGameButton.setVisibility(View.VISIBLE);
            mGameButton.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), OfflineGameActivity.class));
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

        mapboxMap.setStyle(Style.MAPBOX_STREETS, this::enableLocationComponent);

    }


    private void populateWithMarkers() {


        database.getNearbyPosts(currentLocation.getLatitude(), currentLocation.getLongitude(), RADIUS).addOnSuccessListener(posts -> {
            numMarkers = 0;
            for (Marker marker : mapboxMap.getMarkers()) {
                marker.remove();
            }
            for (Post p : posts) {
                database.userCloseFollowersList(p.getUserId()).addOnSuccessListener(closeFollowers -> {
                    boolean isCloseFollower = false;
                    if (LoggedInUser.getInstance() != null) {
                        if (p.getUserId().equals(LoggedInUser.getInstance().getId())) {
                            isCloseFollower = true;
                        } else {
                            for (User user : closeFollowers) {
                                if (user.getId().equals(LoggedInUser.getInstance().getId()))
                                    isCloseFollower = true;
                            }
                        }
                    }

                    if ((isCloseFollower || p.getIsForCloseFollowers() == null || (p.getIsForCloseFollowers() != null && p.getIsForCloseFollowers().equals(Post.NOT_CLOSE_FOLLOWER))) && (p.getType() == null || !p.equals(Post.EVENT) || p.getDate().getTime() < new Date().getTime())) {
                        numMarkers++;
                        mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(p.getLatitude(), p.getLongitude()))
                                .title("A post !")
                                .icon(postIcon))
                                .setSnippet(p.getPostId());
                    }
                });
            }
            Log.i("Map view", "Updated " + posts.size() + " posts markers");
        });

        mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(46.5190, 6.5667))
                .title("EPFL")
                .icon(landmarkIcon))
                .setSnippet("EPFL Landmark");

        mapboxMap.setOnMarkerClickListener(marker -> {
            if (marker.getSnippet().equals("EPFL Landmark")) {
                goToEpflLandmarkFragment();
                return true;
            } else {
                goToPostFragment(marker.getSnippet(), currentLocation);
                return true;
            }
        });
    }

    public void goToEpflLandmarkFragment() {
        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
        database.getNearbyPosts(6.5667, 46.5190, 1000).addOnSuccessListener(posts -> {
            fragmentManager.beginTransaction()
                    .replace(R.id.flContent, new LandmarkFragment("EPFL", "image", posts))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
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
            locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            locationProvider.getLocation(getActivity()).addOnSuccessListener(firstLocation -> {
                currentLocation = firstLocation;
                populateWithMarkers();
                initLocationEngine();
            });

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

        locationEngine.requestLocationUpdates(request, onLocationUpdateCallback, this.getActivity().getMainLooper());
        locationEngine.getLastLocation(onLocationUpdateCallback);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this.getActivity(), R.string.locationPermissionExplanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(getContext(), R.string.locationPermissionDeniedToastMsg, Toast.LENGTH_LONG).show();
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
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
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
            locationEngine.removeLocationUpdates(onLocationUpdateCallback);
        }
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }
}