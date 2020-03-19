package ch.epfl.sdp.kandle.DependencyInjection;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public abstract class Authentication {



    private static Authentication authenticationSystem = new FirebaseAuthentication(FirebaseAuth.getInstance());

    public static void setAuthenticationSystem (Authentication auth){
        authenticationSystem=auth;
    }

    public static Authentication getAuthenticationSystem() {
        return authenticationSystem;
    }




    public abstract Task<AuthResult> createUserWithEmailAndPassword(String email, String password) ;

    public abstract Task<AuthResult> signInWithEmailAndPassword(String email, String password);

    public abstract void signOut();

    public abstract AuthenticationUser getCurrentUser();

}
