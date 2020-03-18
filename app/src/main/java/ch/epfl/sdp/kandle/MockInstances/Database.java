package ch.epfl.sdp.kandle.MockInstances;

import com.google.android.gms.tasks.Task;

import java.util.List;

import ch.epfl.sdp.kandle.User;

public abstract class Database {


    private static Database databaseSystem =FirestoreDatabase.getInstance();




    public static void setDatabaseSystem (Database database){
        databaseSystem=database;
    }

    public static Database getDatabaseSystem() {
        return databaseSystem;
    }

    /**
     * Asynchronously retrieves a User from the Database from its unique username. If such a user does
     * not exist, the task fails.
     * @param username the user's username
     * @return a Task for the resulting User.
     */
    public abstract Task<User> getUserByName(String username);

    /**
     * Asynchronously retrieves a User from the Database from its unique userId. If such a user does
     * not exist, the task fails.
     * @param userId the user's userId
     * @return
     */
    public abstract Task<User> getUserById(String userId);


    /**
     * Asynchronously attempts to create a User in the Database. The task can fail if:
     * - the client cannot access the database
     * - there is already a user with this userId
     * - there is already a user with this name
     * @param user the user to be created in the database
     * @return an empty Task, signifying the outcome
     */
    public abstract Task<Void> createUser(User user);

    public abstract Task<List<User>> searchUsers(final String prefix, int maxNumber);


    public abstract Task<Void> follow(final User userFollowing, final User userFollowed);

    public abstract Task<Void> unfollow(final User userUnFollowing, final User userUnFollowed);

    public abstract Task<Boolean> isFollwing(final User userFollowing, final User userFollowed);




}
