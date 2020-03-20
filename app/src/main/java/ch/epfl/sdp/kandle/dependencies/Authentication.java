package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public interface Authentication {


    Task<AuthResult> createUserWithEmailAndPassword(String email, String password) ;

    Task<AuthResult> signInWithEmailAndPassword(String email, String password);

    void signOut();

    AuthenticationUser getCurrentUser();

}
