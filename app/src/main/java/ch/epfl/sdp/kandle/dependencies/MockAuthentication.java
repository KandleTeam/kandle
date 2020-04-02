package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.Map;
import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.User;

public class MockAuthentication implements Authentication {


    private Map<String, String> accounts;
    private boolean isConnected;
    private Database database;

    public MockAuthentication(boolean isConnected, Map<String, String> accounts, Database database) {
        this.isConnected = isConnected;
        this.accounts = accounts;
        this.database = database;
        if (isConnected) {
            accounts.put(LoggedInUser.getInstance().getEmail(), LoggedInUser.getInstance().getId());
        }

    }

    @Override
    public Task<User> createUserWithEmailAndPassword(String username, String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<User>();

        if (accounts.keySet().contains(email)) {
            source.setException(new Exception("You already have an account"));
        } else {
            String newId = "newId";
            User userToRegister = new User(newId, username, email, "nickname", null);
            accounts.put(email, newId);
            DependencyManager.getDatabaseSystem().createUser(userToRegister);
            isConnected = true;
            LoggedInUser.init(userToRegister);
            source.setResult(userToRegister);
        }
        return source.getTask();
    }

    @Override
    public Task<User> signInWithEmailAndPassword(String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<User>();

        if (accounts.keySet().contains(email)) {
            isConnected = true;
            User user = DependencyManager.getDatabaseSystem().getUserById(accounts.get(email)).getResult();
            LoggedInUser.init(user);
            source.setResult(user);
        } else {
            source.setException(new Exception("You do not have an account yet"));
        }
        return source.getTask();
    }

    @Override
    public void signOut() {
        isConnected = false;
        LoggedInUser.clear();
    }

    public boolean userCurrentlyLoggedIn() {
        return isConnected;
    }

}
