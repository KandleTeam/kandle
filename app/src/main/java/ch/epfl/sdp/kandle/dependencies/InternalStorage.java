package ch.epfl.sdp.kandle.dependencies;

import androidx.annotation.NonNull;
import ch.epfl.sdp.kandle.User;


public interface InternalStorage {

    User getCurrentUser();

    void saveUserAtLoginOrRegister(@NonNull User user);

    void updateUser(User user);

    void deleteUser();


}
