package ch.epfl.sdp.kandle.MockInstances;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;

public class MockAuthentication extends Authentication {


    private boolean isConnected = false;

    @Override
    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {


        isConnected = true;
        System.out.println("done");
        TaskCompletionSource source = new TaskCompletionSource<AuthResult>();
        source.setResult(null);
        return source.getTask();

    }

    @Override
    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {

        isConnected = true;

        System.out.println("done");
        TaskCompletionSource source = new TaskCompletionSource<AuthResult>();
        source.setResult(null);
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
