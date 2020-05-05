package ch.epfl.sdp.kandle.dependencies;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;

import ch.epfl.sdp.kandle.User;


public interface InternalStorage {

    User getCurrentUser();

    void saveUserAtLoginOrRegister(@NonNull User user);

    void updateUser(User user);

    void deleteUser();

    void saveImageToInternalStorage(Bitmap imageBitMap,String id);

    File getImageFileById(String id) ;

}
