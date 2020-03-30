package ch.epfl.sdp.kandle.dependencies;

import android.net.Uri;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.internal.firebase_auth.zzff;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.zzy;
import com.google.firebase.auth.zzz;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.User;

public class MockAuthentication implements Authentication {


    private List<String> emails = new ArrayList<>();
    private MockDatabase database;
    private boolean isConnected;
    private final User loggedInUser = new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image");

    public MockAuthentication(boolean isConnected) {
        this.isConnected = isConnected;
        emails.add(loggedInUser.getEmail());
        database = new MockDatabase();
    }

    @Override
    public Task<User> createUserWithEmailAndPassword(String username, String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<User>();

        if (emails.contains(email)){
            isConnected = false;
            source.setException( new Exception("You already have an account") );
        }
        else {
            emails.add(email);
            isConnected = true;
            String newId = "newUserId";
            User userToRegister = new User(newId,"newUser","newUser@kandle.ch","newFullName",null);
            source.setResult(userToRegister);
            database.createUser(userToRegister);

        }
        return source.getTask();
    }

    @Override
    public Task<User> signInWithEmailAndPassword(String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<User>();
        if (emails.contains(email)) {
            isConnected = true;
            database.getUserById("loggedInUserId");
            source.setResult(loggedInUser);

        } else {
            isConnected = false;
            source.setException(new Exception ("You do not have an account yet"));
        }

        return source.getTask();

    }

    @Override
    public void signOut() {
        isConnected=false;
    }

    public boolean userCurrentlyLoggedIn(){
        return isConnected;
    }

}
