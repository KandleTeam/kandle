package ch.epfl.sdp.kandle.MockInstances;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import ch.epfl.sdp.kandle.User;

public class FirestoreDatabase extends Database {

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
                .continueWith(new Continuation<QuerySnapshot, User>() {

                    @Override
                    public User then(@NonNull Task<QuerySnapshot> task) {

                        QuerySnapshot results = task.getResult();

                        if(results.size() > 1)  {
                            throw new AssertionError("We done goofed somewhere! Duplicate usernames");
                        }
                        else if(results.size() == 0) {
                            throw new IllegalArgumentException(("No such user with username: " + username));
                        }
                        else return results.iterator().next().toObject(User.class);
                    }
                });
    }

    @Override
    public Task<User> getUserById(final String userId) {
        return users
                .document(userId)
                .get()
                .continueWith(new Continuation<DocumentSnapshot, User>() {

                    @Override
                    public User then(@NonNull Task<DocumentSnapshot> task) {

                        User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                        if (!user.getId().equals(userId)) throw new AssertionError("We done goofed somewhere! Unexpected uid");

                        return user;
                    }
                });
    }

    @Override
    public Task<Void> createUser(final User user) {

        final DocumentReference usernameDoc = usernames.document(user.getUsername());
        final DocumentReference userDoc = users.document(user.getId());


        return firestore
                .runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                            DocumentSnapshot usernameSnapshot = transaction.get(usernameDoc);
                            DocumentSnapshot userSnapshot = transaction.get(userDoc);

                            if(userSnapshot.exists()) {
                                throw new FirebaseFirestoreException("User already exists!", FirebaseFirestoreException.Code.ALREADY_EXISTS);
                            }
                            else if(usernameSnapshot.exists()) {
                                throw new FirebaseFirestoreException("Username already taken!", FirebaseFirestoreException.Code.ALREADY_EXISTS);
                            } else {
                                System.out.println("store");

                                Map<String,Object> map = new HashMap<>();
                                map.put("username", user.getUsername());


                                transaction.set(usernameDoc, map);
                                transaction.set(userDoc, user);
                        }

                        return null;
                    }
                });

    }

    @Override
    public Task<List<User>> searchUsers(String prefix, int maxNumber) {
        char last = prefix.charAt(prefix.length()-1);
        String upperBound = prefix.substring(0, prefix.length()-1) + (last+1);


        return users
                .whereGreaterThanOrEqualTo("normalizedUsername", prefix)
                .whereLessThan("normalizedUsername", upperBound)
                .limit(maxNumber)
                .orderBy("username")
                .get()
                .continueWith(new Continuation<QuerySnapshot, List<User>>() {
                    @Override
                    public List<User> then(@NonNull Task<QuerySnapshot> task) {
                        return task.getResult().toObjects(User.class);
                    }
                });
    }


}
