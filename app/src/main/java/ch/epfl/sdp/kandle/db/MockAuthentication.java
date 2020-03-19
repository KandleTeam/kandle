package ch.epfl.sdp.kandle.db;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MockAuthentication implements Authentication {


    private boolean isConnected = false;

    @Override
    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {


        isConnected = true;
        TaskCompletionSource<AuthResult> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();

    }

    @Override
    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {


        TaskCompletionSource<AuthResult> source = new TaskCompletionSource<>();

        if(isConnected) {

            source.setException(new IllegalArgumentException("An user is already logged in"));
        }
        else if(email.toLowerCase().contains("unknown") || password.toLowerCase().contains("bad")) {

            source.setException(new IllegalArgumentException("Bad credentials"));
        } else {

            isConnected = true;
            source.setResult(null);

        }

        return source.getTask();

    }

    @Override
    public void signOut() {
        isConnected = false;
    }

    public AuthenticationUser getCurrentUser() {
        if (isConnected){
            return new MockAuthenticationUser();
        }
        else {
            return null;
        }
    }
}