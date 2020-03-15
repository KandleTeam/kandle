package ch.epfl.sdp.kandle;

import com.google.android.gms.tasks.Task;

public interface Database {

    /**
     * Asynchronously retrieves a User from the Database from its unique username. If such a user does
     * not exist, the task fails.
     * @param username the user's username
     * @return a Task for the resulting User.
     */
    public Task<User> getUserByName(String username);

    /**
     * Asynchronously retrieves a User from the Database from its unique userId. If such a user does
     * not exist, the task fails.
     * @param userId the user's userId
     * @return
     */
    public Task<User> getUserById(String userId);


    /**
     * Asynchronously attempts to create a User in the Database. The task can fail if:
     * - the client cannot access the database
     * - there is already a user with this userId
     * - there is already a user with this name
     * @param user the user to be created in the database
     * @return an empty Task, signifying the outcome
     */
    public Task<Void> createUser(User user);


}
