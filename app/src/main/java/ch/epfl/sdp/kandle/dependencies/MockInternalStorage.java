package ch.epfl.sdp.kandle.dependencies;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.sdp.kandle.User;

/**
 * @Author Marc Egli
 * This class mocks the internal storage of the application for testing
 * by storing a user instance as a static variable
 */
public class MockInternalStorage implements InternalStorage {
    private static boolean userSavedLocally;
    private static User storedUser;
    private Map<String,File> images;

    public MockInternalStorage(boolean userSavedLocally,HashMap<String,File> images) {
        MockInternalStorage.userSavedLocally = userSavedLocally;
        this.images = images;
        if (userSavedLocally) {
            storedUser = new User("loggedInUserId", "LoggedInUser", "loggedInUser@kandle.ch", "nickname", "image");
        }
    }

    public MockInternalStorage(HashMap<String,File> images) {
        this.images = images;
        userSavedLocally = false;
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
    public void updateUser(User user) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException();
        }
        storedUser = user;

    }


    @Override
    public void deleteUser() {
        storedUser = null;
    }

    @Override
    public void saveImageToInternalStorage(Bitmap imageBitMap, String id)  {
        File image = new File("user");
        try {
            FileOutputStream fos = new FileOutputStream(image);
            imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            images.put(id, image);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public File getImageFileById(String id) {
        return images.get(id);
    }
}
