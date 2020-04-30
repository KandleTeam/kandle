package ch.epfl.sdp.kandle.storage.firebase;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class FirebaseAuthentication implements Authentication {

    private static final FirebaseAuth FAUTH = FirebaseAuth.getInstance();
    private static final FirebaseAuthentication INSTANCE = new FirebaseAuthentication();
    private Database database = new CachedFirestoreDatabase();

    private FirebaseAuthentication() {
    }

    public static FirebaseAuthentication getInstance() {
        return INSTANCE;
    }

    /**
     * In this function we consider the case where the user didn't log out from the app but closed it
     * Therefore the CurrentUser would be non null but the Instance of the loggedInUser would be wiped.
     * We then call the CachedDatabase to get the User data back to init the LoggedInUser
     *
     * @return boolean that indicates if there is a current user logged in or not
     */
    public boolean getCurrentUserAtApplicationStart() {
        User localUser = DependencyManager.getInternalStorageSystem().getCurrentUser();
        if (LoggedInUser.getInstance() != null) {
            return true;
        }

        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            if (localUser != null && FAUTH.getCurrentUser() != null) {
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


    /**
     * This method allows to create a new User in Firebase and stores him in Firestore
     * If the account creation is a success we store the User in the database and set the current user to it
     *
     * @param username
     * @param email
     * @param password
     * @return a task that contains a kandle user
     */
    @Override
    public Task<User> createUserWithEmailAndPassword(final String username, final String email, final String password) {
        Task<AuthResult> authResult = FAUTH.createUserWithEmailAndPassword(email, password);
        TaskCompletionSource<User> source = new TaskCompletionSource<User>();
        return authResult.continueWithTask(task -> {
            if (authResult.isSuccessful()) {
                String userId = authResult.getResult().getUser().getUid();
                User newUser = new User(userId, username, email, username, null);
                database.createUser(newUser).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        LoggedInUser.init(newUser);
                        DependencyManager.getInternalStorageSystem().saveUserAtLoginOrRegister(newUser);
                        source.setResult(newUser);
                    } else {
                        source.setException(task1.getException());
                    }
                });
            } else {
                source.setException(authResult.getException());
            }
            return source.getTask();
        });
    }

    /**
     * This method checks if the user already has an account or not.
     * If he does we look for the user in the database and return it through a task.
     * Note that there should always be an entry for the user in the database if he has an account
     * If it's not the case we return a task that contains the exception of the authentication task
     *
     * @param email
     * @param password
     * @return a task that contains a kandle user
     */
    @Override
    public Task<User> signInWithEmailAndPassword(String email, String password) {

        Task<AuthResult> authResult = FAUTH.signInWithEmailAndPassword(email, password);
        return authResult.continueWithTask(task -> {
            if (authResult.isSuccessful()) {
                String userId = authResult.getResult().getUser().getUid();
                return database.getUserById(userId).continueWith(task1 -> {
                    User user = task1.getResult();
                    LoggedInUser.init(user);
                    DependencyManager.getInternalStorageSystem().saveUserAtLoginOrRegister(user);
                    return user;
                });
            } else {
                TaskCompletionSource<User> source = new TaskCompletionSource<User>();
                source.setException(authResult.getException());
                return source.getTask();
            }
        });
    }

    /**
     * This function allows to change the current password of the user logged in
     * Note that this function doesn't has to update the current user instance in the app
     *
     * @param password
     * @return
     */
    @Override
    public Task<Void> reAuthenticate(String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(getCurrentUser().getEmail(), password);
        return FAUTH.getCurrentUser().reauthenticate(credential);
    }

    @Override
    public Task<Void> updatePassword(String password) {
        return FAUTH.getCurrentUser().updatePassword(password);
    }

    @Override
    public void signOut() {
        LoggedInUser.clear();
        DependencyManager.getInternalStorageSystem().deleteUser();
        FAUTH.signOut();
    }

    @Override
    public User getCurrentUser() {
        return LoggedInUser.getInstance();
    }


}
