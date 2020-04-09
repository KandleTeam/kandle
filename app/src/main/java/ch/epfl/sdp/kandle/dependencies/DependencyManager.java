package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.location.LocationServices;
import android.util.Pair;

import ch.epfl.sdp.kandle.NetworkState;

public final class DependencyManager {


    private static Database db = FirestoreDatabase.getInstance();
    private static Storage storage = CloudStorage.getInstance();
    private static Authentication auth = FirebaseAuthentication.getInstance();
    private static MyLocationProvider locationProvider = new GoogleLocationServices();
    private static NetworkState networkState = UserNetworkStatus.getInstance();
    //private static InternalStorage internalStorage = null;
    public static void setFreshTestDependencies(Authentication auth, Database db, Storage storage) {
    private static NetworkState networkState = UserNetworkStatus.getInstance();



    public static void setFreshTestDependencies(Authentication auth, Database db, Storage storage, InternalStorage internalStorage,NetworkState networkState) {
        setAuthSystem(auth);
        setDatabaseSystem(db);
        setStorageSystem(storage);
        setLocationProvider( new MockLocation());
        //setInternalStorageSystem(new MockInternalStorage(isLoggedIn));
        setInternalStorageSystem(internalStorage);
        setNetworkStateSystem(networkState);

    }

    public static MyLocationProvider getLocationProvider() {
        return locationProvider;
    }

    public static void setLocationProvider(MyLocationProvider locationProvider) {
        DependencyManager.locationProvider = locationProvider;
    }

    public static Authentication getAuthSystem() {
        return auth;
    }

    public static void setAuthSystem(Authentication auth) {
        DependencyManager.auth = auth;
    }

    public static Database getDatabaseSystem() {
        return db;
    }

    public static void setDatabaseSystem(Database db) {
        DependencyManager.db = db;
    }

    public static Storage getStorageSystem() {
        return storage;
    }

    public static void setStorageSystem(Storage storage) {
        DependencyManager.storage = storage;
    }

    public static InternalStorage getInternalStorageSystem() {
        return internalStorage;
    }

    public static void setInternalStorageSystem(InternalStorage internalStorage) {
        DependencyManager.internalStorage = internalStorage;
    }
    public static NetworkState getNetworkStateSystem() {
        return networkState;
    }

    public static void setNetworkStateSystem(NetworkState networkState) {
        DependencyManager.networkState = networkState;
    }


    private DependencyManager() {

    }

}
