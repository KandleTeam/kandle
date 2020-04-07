package ch.epfl.sdp.kandle.dependencies;

import androidx.annotation.NonNull;
import ch.epfl.sdp.kandle.User;

/**
 * @Author Marc Egli
 * This class mocks the internal storage of the application for testing
 * by storing a user instance as a static variable
 */
public class MockInternalStorage implements InternalStorage {
    private static boolean userSavedLocally;
    private static User storedUser;

    public MockInternalStorage(boolean userSavedLocally) {
        this.userSavedLocally = userSavedLocally;
    }

    public MockInternalStorage() {
        this.userSavedLocally = false;
    }

    @Override
    public User getCurrentUser() {
        if (userSavedLocally) {
            return storedUser;
        } else {
            return null;
        }
    }

    @Override
    public void saveUserAtLoginOrRegister(@NonNull User user) {
        if (user == null) {
            throw new IllegalArgumentException();
        }
        if (!(userSavedLocally && user.getId().equals(storedUser.getId()))) {
            storedUser = user;
            userSavedLocally = !userSavedLocally;
        }
    }

    @Override
    public void updateUser(User user) throws IllegalArgumentException{
        if(user == null) {
            throw new IllegalArgumentException();
        }
        storedUser = user;

    }


    @Override
    public void deleteUser() {
        storedUser = null;
    }
}
