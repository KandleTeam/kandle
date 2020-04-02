package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.User;

public class FirebaseAuthentication implements Authentication {

    private static final FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private static final FirebaseAuthentication auth = new FirebaseAuthentication();
    private FirestoreDatabase database = FirestoreDatabase.getInstance();

    private FirebaseAuthentication() {
    }

    public static FirebaseAuthentication getInstance() {
        return auth;
    }


    /**
     * In this function we consider the case where the user didn't log out from the app but closed it
     * Therefor the CurretnUser woudl be non null but the Instance of the loggedinUser would be.
     * We ther call the CachedDatabase to get the User data back to init the LoggedInUser
     *
     * @return boolean that idicates if there is a current user logged in or not
     */
    public boolean userCurrentlyLoggedIn() {
        if (fAuth.getCurrentUser() != null) {
            database.getUserById(fAuth.getCurrentUser().getUid()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = task.getResult();
                    LoggedInUser.init(user);
                } else {
                    task.getException().printStackTrace();
                }
            });
            return true;
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
     * @return a task that contains a kandle User
     */
    @Override
    public Task<User> createUserWithEmailAndPassword(final String username, final String email, final String password) {
        Task<AuthResult> authResult = fAuth.createUserWithEmailAndPassword(email, password);
        TaskCompletionSource<User> source = new TaskCompletionSource<User>();
        return authResult.continueWithTask(task -> {
            if (authResult.isSuccessful()) {
                String userId = authResult.getResult().getUser().getUid();
                LoggedInUser.init(new User(userId, username, email, username, null));
                database.createUser(LoggedInUser.getInstance()).addOnCompleteListener(task1 -> {
                    source.setResult(LoggedInUser.getInstance());
                });
            } else {
                source.setException(authResult.getException());
            }
            return source.getTask();
        });
    }

    /**
     * This method checks if the user already has an account or not.
     * If he does we look for the User in the database and return it throught a task
     * If it's not the case we return a task that contains the excpetion of the authentification task
     *
     * @param email
     * @param password
     * @return a task that contains a kandle user
     */
    @Override
    public Task<User> signInWithEmailAndPassword(String email, String password) {

        Task<AuthResult> authResult = fAuth.signInWithEmailAndPassword(email, password);
        return authResult.continueWithTask(task -> {
            if (authResult.isSuccessful()) {
                String userId = authResult.getResult().getUser().getUid();
                return database.getUserById(userId).continueWith(task1 -> {
                    User user = task1.getResult();
                    LoggedInUser.init(user);
                    return user;
                });
            } else {
                TaskCompletionSource<User> source = new TaskCompletionSource<User>();
                source.setException(authResult.getException());
                return source.getTask();
            }
        });
    }

    @Override
    public void signOut() {
        LoggedInUser.clear();
        fAuth.signOut();
    }
}
