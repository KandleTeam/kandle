package ch.epfl.sdp.kandle.DependencyInjection;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthentication extends Authentication {

    private FirebaseAuth fAuth;

    public FirebaseAuthentication(FirebaseAuth fAuth) {
        this.fAuth = fAuth;

    }

   /* public FirebaseAuth getfAuth() {
        return fAuth;
    }*/

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
                System.out.println("non null");
                return new FirebaseAuthenticationUser(fAuth);
            }

            else {
                System.out.println("null");
                return null;
            }
    }
}
