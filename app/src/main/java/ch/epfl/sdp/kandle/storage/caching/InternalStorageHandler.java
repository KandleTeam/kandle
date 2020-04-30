package ch.epfl.sdp.kandle.storage.caching;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.InternalStorage;

/**
 * @Author Marc Egli
 * This class handles the internal storage accesses
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class InternalStorageHandler implements InternalStorage {

    private static final InternalStorageHandler INSTANCE = new InternalStorageHandler();
    private final String USER_DATA_PATH = "userData";
    private final Context context;

    public InternalStorageHandler() {
        this.context = Kandle.getContext();
    }

    public static InternalStorage getInstance() {
        return INSTANCE;
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
            FileOutputStream file = context.openFileOutput(USER_DATA_PATH, Context.MODE_PRIVATE);
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
            FileInputStream file = context.openFileInput(USER_DATA_PATH);
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
     *
     * @param
     * @throws IllegalArgumentException
     * @Author Marc Egli
     */

    @Override
    public void updateUser(@NonNull User user) throws IllegalArgumentException {
        deleteUser();
        storeUser(user);

    }


    /**
     * Deletes the user entry saved locally by writing an empty string to the file without append mode
     * Therefor the file is cleared, this is done in case many user login on the same app instance.
     *
     * @Author Marc Egli
     */
    @Override
    public void deleteUser() {
        File user = context.getFileStreamPath(USER_DATA_PATH);
        user.delete();

    }

}
