package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthentication implements Authentication {

    private static FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private static FirebaseAuthentication auth = new FirebaseAuthentication();

    private FirebaseAuthentication() {
    }

    public static FirebaseAuthentication getInstance() {
        return auth;
    }


    @Override
    public Task<AuthResult> createUserWithEmailAndPassword(final  String email, final String password)  {

        return fAuth.createUserWithEmailAndPassword(email, password);

    }

    @Override
    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {

        return fAuth.signInWithEmailAndPassword(email, password);

    }

    @Override
    public void signOut() {
        fAuth.signOut();
    }

    public AuthenticationUser getCurrentUser() {
            if (fAuth.getCurrentUser() != null){
                return new FirebaseAuthenticationUser(fAuth);
            }

            else {
                return null;
            }
    }
}
