package ch.epfl.sdp.kandle.dependencies;


public final class DependencyManager {

    private static InternalStorage internalStorage = InternalStorageHandler.getInstance();
    private static Authentication auth = FirebaseAuthentication.getInstance();
    private static Database db = FirestoreDatabase.getInstance();
    private static Storage storage = CloudStorage.getInstance();



    public static void setFreshTestDependencies(Authentication auth, Database db, Storage storage, InternalStorage internalStorage) {
        setAuthSystem(auth);
        setDatabaseSystem(db);
        setStorageSystem(storage);
        setInternalStorageSystem(internalStorage);
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


    private DependencyManager() {

    }

}
