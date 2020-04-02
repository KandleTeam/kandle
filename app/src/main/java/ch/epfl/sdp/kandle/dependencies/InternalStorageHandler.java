package ch.epfl.sdp.kandle.dependencies;

import android.content.Context;
import androidx.annotation.NonNull;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import ch.epfl.sdp.kandle.User;

/**
 * @Author Marc Egli
 * This class handles the internal storage accesses
 */
public class InternalStorageHandler implements InternalStorage {

    private final String userDataPath = "userData";
    private Context context;

    public InternalStorageHandler(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context was null");
        }
        this.context = context;
    }

    /**
     * Stores the user locally
     * This is a private method and can only be used by this class to ensure good behavior
     *
     * @param user
     * @throws IllegalArgumentException
     * @Author Marc Egli
     */
    private void storeUser(@NonNull User user) {

        try {
            FileOutputStream file = context.openFileOutput(userDataPath, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(user);
            out.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the user instance from the internal storage
     *
     * @return user
     * @Author Marc Egli
     */
    @Override
    public User getCurrentUser() {

        User user = null;
        try {
            FileInputStream file = context.openFileInput(userDataPath);
            ObjectInputStream in = new ObjectInputStream(file);
            user = (User) in.readObject();
            in.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Stores the user only if there is another user stored already or if there isn't one
     * The condition to save the user relies on short circuit evaluation
     *
     * @param user
     * @throws IllegalArgumentException
     * @Author Marc Egli
     */
    @Override
    public void saveUserAtLoginOrRegister(@NonNull User user) throws IllegalArgumentException {

        if (user == null) {
            throw new IllegalArgumentException();
        }

        User storedUser = getCurrentUser();
        if (storedUser == null) {
            storeUser(user);
        } else if (!storedUser.getId().equals(user.getId())) {
            deleteUser();
            storeUser(user);
        }

    }

    /**
     * Updates the stored user with a new user instance
     * This function overwrites the current
     * @Author Marc Egli
     * @param
     * @throws IllegalArgumentException
     */
/*
    @Override
    public void updateUser(@NonNull User user) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException();
        }
        deleteUser();
        storeUser(user);

    }
    */

    /**
     * Deletes the user entry saved locally by writing an empty string to the file without append mode
     * Therefor the file is cleared, this is done in case many user login on the same app instance.
     *
     * @Author Marc Egli
     */
    @Override
    public void deleteUser() {
        try {
            PrintWriter writer = new PrintWriter(userDataPath);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
