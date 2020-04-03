package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.location.LocationServices;

public final class DependencyManager {

    private static Authentication auth = FirebaseAuthentication.getInstance();
    private static Database db = FirestoreDatabase.getInstance();
    private static Storage storage = CloudStorage.getInstance();
    private static MyLocationProvider locationProvider = new GoogleLocationServices();


    public static void setFreshTestDependencies(boolean isLoggedIn) {
        setAuthSystem(new MockAuthentication(isLoggedIn));
        setDatabaseSystem(new MockDatabase());
        setStorageSystem(new MockStorage());
        setLocationProvider( new MockLocation());
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

    private DependencyManager() {
    }


}
