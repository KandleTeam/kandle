package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.Map;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.User;

public class MockAuthentication implements Authentication {


    private Map<String, String> accounts;
    private boolean isConnected;
    private String password;

    public MockAuthentication(boolean isConnected, Map<String, String> accounts, String password) {
        this.isConnected = isConnected;
        this.accounts = accounts;
        this.password = password;


    }

    @Override
    public boolean getCurrentUserAtApplicationStart() {
        User localUser = DependencyManager.getInternalStorageSystem().getCurrentUser();
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            if (localUser != null && isConnected) {
                LoggedInUser.init(localUser);
                return true;
            }
        } else {
            if (localUser != null) {
                LoggedInUser.init(localUser);
                return true;
            }
        }
        return false;
    }


    @Override
    public Task<User> createUserWithEmailAndPassword(String username, String email, String password) {

        TaskCompletionSource source = new TaskCompletionSource<User>();

        if (accounts.containsKey(email)) {
            source.setException(new Exception("You already have an account"));
        } else {
            String newId = "newId";
            User userToRegister = new User(newId, username, email, "nickname", null);
            accounts.put(email, newId);
            DependencyManager.getInternalStorageSystem().saveUserAtLoginOrRegister(userToRegister);
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

        if (accounts.containsKey(email)) {
            isConnected = true;
            User user = DependencyManager.getDatabaseSystem().getUserById(accounts.get(email)).getResult();
            LoggedInUser.init(user);
            DependencyManager.getInternalStorageSystem().saveUserAtLoginOrRegister(user);
            source.setResult(user);
        } else {
            source.setException(new Exception("You do not have an account yet"));
        }
        return source.getTask();
    }

    @Override
    public Task<Void> reAuthenticate(String password) {
        TaskCompletionSource source = new TaskCompletionSource<Void>();
        if (!this.password.equals(password)) {
            source.setException(new Exception("Passwords do not match"));
        } else {
            source.setResult(null);
        }
        return source.getTask();
    }

    @Override
    public Task<Void> updatePassword(String password) {
        TaskCompletionSource source = new TaskCompletionSource<Void>();
        this.password = password;
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public void signOut() {
        isConnected = false;
        DependencyManager.getInternalStorageSystem().deleteUser();
        LoggedInUser.clear();
    }

    @Override
    public User getCurrentUser() {
        return LoggedInUser.getInstance();
    }

    @Override
    public Task<Void> deleteUser() {
        TaskCompletionSource source = new TaskCompletionSource<Void>();
        isConnected = false;
        DependencyManager.getInternalStorageSystem().deleteUser();
        accounts.remove(LoggedInUser.getInstance().getEmail());
        DependencyManager.getLocalDatabase().clearAllTables();
        LoggedInUser.clear();
        return source.getTask();
    }


}
