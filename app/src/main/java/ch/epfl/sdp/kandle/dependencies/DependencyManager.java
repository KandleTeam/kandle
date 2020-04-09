package ch.epfl.sdp.kandle.dependencies;


import ch.epfl.sdp.kandle.NetworkState;

public final class DependencyManager {

    private static InternalStorage internalStorage = InternalStorageHandler.getInstance();
    private static Database db = FirestoreDatabase.getInstance();
    private static Authentication auth = FirebaseAuthentication.getInstance();
    private static Storage storage = CloudStorage.getInstance();
    private static NetworkState networkState = UserNetworkStatus.getInstance();



    public static void setFreshTestDependencies(Authentication auth, Database db, Storage storage, InternalStorage internalStorage,NetworkState networkState) {
        setAuthSystem(auth);
        setDatabaseSystem(db);
        setStorageSystem(storage);
        setInternalStorageSystem(internalStorage);
        setNetworkStateSystem(networkState);

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
