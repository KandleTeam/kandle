package ch.epfl.sdp.kandle.db;

public final class DependencyManager {

    private static Database db = FirestoreDatabase.getInstance();

    private static Authentication auth = FirebaseAuthentication.getInstance();

    public static void setDatabaseSystem (Database database){
        db = database;
    }

    public static Database getDatabaseSystem() {
        return db;
    }

    public static void setAuthSystem (Authentication authentication){
        auth = authentication;
    }

    public static Authentication getAuthSystem() {
        return auth;
    }

}
