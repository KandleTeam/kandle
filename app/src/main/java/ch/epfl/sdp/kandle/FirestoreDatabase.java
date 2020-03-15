package ch.epfl.sdp.kandle;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A database backed by Cloud Firestore
 */
public class FirestoreDatabase implements Database {

    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private static final FirestoreDatabase instance = new FirestoreDatabase();

    private static final CollectionReference users = firestore.collection("users");
    private static final CollectionReference posts = firestore.collection("posts");



    private FirestoreDatabase() {
        // For now, disable caching
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        firestore.setFirestoreSettings(settings);
    }

    public static FirestoreDatabase getInstance() {
        return instance;
    }

    @Override
    public Task<User> getUserByName(String username) {

        DocumentReference docUser = firestore.collection("users").document();

        // TODO
        // also: make Firestore also maintain an index by username

        return null;
    }

    @Override
    public Task<User> getUserById(final String userId) {

        return firestore.collection("users")
                .document(userId)
                .get()
                .continueWith(new Continuation<DocumentSnapshot, User>() {

                    @Override
                    public User then(@NonNull Task<DocumentSnapshot> task) {

                        User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                        assert user != null;
                        assert (user.getId()).equals(userId);

                        return user;
                    }
                });

    }

    @Override
    public Task<Void> createUser(User user) {
        // TODO
        // use a transaction to ensure username uniqueness
        return null;
    }


}
