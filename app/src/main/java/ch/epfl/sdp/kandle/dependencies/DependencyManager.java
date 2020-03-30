package ch.epfl.sdp.kandle.dependencies;

import android.content.Context;

import java.util.HashMap;

import ch.epfl.sdp.kandle.User;

public final class DependencyManager {

    private static Authentication auth = FirebaseAuthentication.getInstance();
    private static Database db = FirestoreDatabase.getInstance();
    private static Storage storage = CloudStorage.getInstance();
    private static InternalStorage internalStorage = null;


    public static void setFreshTestDependencies(boolean isLoggedIn) {
        setAuthSystem(new MockAuthentication(isLoggedIn));
        setDatabaseSystem(new MockDatabase());
        setStorageSystem(new MockStorage());
        setInternalStorageSystem(new MockInternalStorage(isLoggedIn));
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

    public static InternalStorage getInternalStorageSystem(Context context) {
        if(internalStorage == null) {
            internalStorage = new InternalStorageHandler(context);
        }
        return internalStorage;
    }

    public static void setInternalStorageSystem(InternalStorage internalStorage) {
        DependencyManager.internalStorage = internalStorage;
    }

    private DependencyManager() {
    }


}
