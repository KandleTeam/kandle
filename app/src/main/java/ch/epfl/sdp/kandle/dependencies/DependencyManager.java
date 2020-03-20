package ch.epfl.sdp.kandle.dependencies;

public class DependencyManager {

    private static Authentication auth = FirebaseAuthentication.getInstance();
    private static Database db = FirestoreDatabase.getInstance();
    private static Storage storage = CloudStorage.getInstance();

    public static void setFreshTestDependencies(boolean isLoggedIn) {
        setAuthSystem(new MockAuthentication(isLoggedIn));
        setDatabaseSystem(new MockDatabase());
        setStorageSystem(new MockStorage());
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


}
