package ch.epfl.sdp.kandle.db;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.List;
import java.util.Objects;

import ch.epfl.sdp.kandle.User;

/**
 * A database backed by Cloud Firestore
 */
public class FirestoreDatabase implements Database {

    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private static final FirestoreDatabase instance = new FirestoreDatabase();

    private static final CollectionReference users = firestore.collection("users");
    private static final CollectionReference usernames = firestore.collection("usernames");
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
    public Task<User> getUserByName(final String username) {

        return users
                .whereEqualTo("username", username)
                .get()
                .continueWith(task -> {

                    QuerySnapshot results = task.getResult();

                    if(results.size() > 1)  {
                        throw new AssertionError("We done goofed somewhere! Duplicate usernames");
                    }
                    else if(results.size() == 0) {
                        throw new IllegalArgumentException(("No such user with username: " + username));
                    }
                    else return results.iterator().next().toObject(User.class);
                });
    }

    @Override
    public Task<User> getUserById(final String userId) {

        return users
                .document(userId)
                .get()
                .continueWith(task -> {

                    User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                    if (!user.getId().equals(userId)) throw new AssertionError("We done goofed somewhere! Unexpected uid");

                    return user;
                });

    }

    @Override
    public Task<Void> createUser(final User user) {

        final DocumentReference usernameDoc = usernames.document(user.getUsername());
        final DocumentReference userDoc = users.document(user.getId());


        return firestore
                .runTransaction(transaction -> {

                    DocumentSnapshot usernameSnapshot = transaction.get(usernameDoc);
                    DocumentSnapshot userSnapshot = transaction.get(userDoc);

                    if(userSnapshot.exists()) {
                        throw new FirebaseFirestoreException("User already exists!", FirebaseFirestoreException.Code.ALREADY_EXISTS);
                    }
                    else if(usernameSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Username already taken!", FirebaseFirestoreException.Code.ALREADY_EXISTS);
                    } else {
                        transaction.update(usernameDoc, "userId", user.getId());
                        transaction.set(userDoc, user);
                    }

                    return null;
                });

    }

    public Task<List<User>> searchUsers(final String prefix, int maxNumber) {

        char last = prefix.charAt(prefix.length()-1);
        String upperBound = prefix.substring(0, prefix.length()-1) + (last+1);

        return users
                .whereGreaterThanOrEqualTo("normalizedUsername", prefix)
                .whereLessThan("normalizedUsername", upperBound)
                .limit(maxNumber)
                .orderBy("username")
                .get()
                .continueWith(task -> task.getResult().toObjects(User.class));
    }


}
