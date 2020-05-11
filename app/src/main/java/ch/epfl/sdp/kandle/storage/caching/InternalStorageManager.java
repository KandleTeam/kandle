package ch.epfl.sdp.kandle.storage.caching;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.InternalStorage;

/**
 * @Author Marc Egli
 * This class handles the internal storage accesses
 */

public class InternalStorageManager implements InternalStorage {

    private static final InternalStorageManager INSTANCE = new InternalStorageManager();
    private final String USER_DATA_PATH = "localUserDir";
    private final String IMAGE_DATA_PATH = "imagesDir";
    private final Context context;

    public InternalStorageManager() {
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
     * @Author Marc Egli
     */
    private void storeUser(@NonNull User user) {

        try {
            File localUserDirectory = context.getDir(USER_DATA_PATH,Context.MODE_PRIVATE);
            File localUserPath = new File(localUserDirectory,"localUser");
            FileOutputStream file = new FileOutputStream(localUserPath);
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
            File localUserDirectory = context.getDir(USER_DATA_PATH,Context.MODE_PRIVATE);
            File localUserPath = new File(localUserDirectory,"localUser");
            FileInputStream file = new FileInputStream(localUserPath);
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
     * @Author Marc Egli
     */
    @Override
    public void saveUserAtLoginOrRegister(@NonNull User user)  {

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
    public void updateUser(@NonNull User user)  {
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
        File localUserDirectory = context.getDir(USER_DATA_PATH,Context.MODE_PRIVATE);
        File localUserPath = new File(localUserDirectory,"localUser");
        localUserPath.delete();

    }



    public void saveImageToInternalStorage(Bitmap imageBitMap,String id) {
        File imageDirectory = context.getDir(IMAGE_DATA_PATH,Context.MODE_PRIVATE);
        File imagePath = new File(imageDirectory,id);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public File getImageFileById(String id) {
        File imageDirectory = context.getDir(IMAGE_DATA_PATH, Context.MODE_PRIVATE);
        File imageFile = new File(imageDirectory, id);
        return imageFile.length() == 0 ? null : imageFile;
    }

}
