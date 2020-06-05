package ch.epfl.sdp.kandle.storage.caching;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.File;

import ch.epfl.sdp.kandle.entities.user.User;


public interface InternalStorage {

    User getCurrentUser();

    void saveUserAtLoginOrRegister(@NonNull User user);

    void updateUser(User user);

    void deleteUser();

    void saveImageToInternalStorage(Bitmap imageBitMap, String id);

    File getImageFileById(String id);

    void deleteAllPictures();

}
