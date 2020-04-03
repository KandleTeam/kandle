package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import ch.epfl.sdp.kandle.User;

public interface Authentication {

    Task<User> createUserWithEmailAndPassword(String username, String email, String password);

    Task<User> signInWithEmailAndPassword(String email, String password);

    Task<Void> reauthenticate(String password);

    Task<Void> updatePassword(String password);

    void signOut();

    boolean userCurrentlyLoggedIn();

}
