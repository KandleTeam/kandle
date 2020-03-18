package ch.epfl.sdp.kandle.db;

public final class DatabaseManager {

    private static Database db = FirestoreDatabase.getInstance();

    public static Database databaseSystem = FirestoreDatabase.getInstance();

    public static void setDatabaseSystem (Database database){
        databaseSystem = database;
    }

    public static Database getDatabaseSystem() {
        return databaseSystem;
    }

}
