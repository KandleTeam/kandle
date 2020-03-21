package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;
import java.util.List;

public class MockAuthentication implements Authentication {


    private List<String> emails = new ArrayList<>();

    private boolean isConnected;

    public MockAuthentication(boolean isConnected) {
        this.isConnected = isConnected;
        emails.add("user1@test.com");
    }

    @Override
    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<AuthResult>();

        if (emails.contains(email)){
            isConnected = false;
            source.setException( new Exception("You already have an account") );
        }
        else {
            emails.add(email);
            isConnected = true;
            source.setResult(null);
        }
        return source.getTask();
    }

    @Override
    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<AuthResult>();

        if (emails.contains(email)) {
            isConnected = true;
            source.setResult(null);

        }

        else {
            isConnected = false;
            source.setException(new Exception ("You do not have an account yet"));
        }

        return source.getTask();

    }

    @Override
    public void signOut() {
        isConnected=false;
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
