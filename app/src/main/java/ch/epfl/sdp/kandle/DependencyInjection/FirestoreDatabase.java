package ch.epfl.sdp.kandle.DependencyInjection;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ch.epfl.sdp.kandle.User;

public class FirestoreDatabase extends Database {

    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private static final FirestoreDatabase instance = new FirestoreDatabase();

    private static final CollectionReference users = firestore.collection("users");
    private static final CollectionReference usernames = firestore.collection("usernames");
    private static final CollectionReference posts = firestore.collection("posts");
    private static final CollectionReference follow = firestore.collection("follow");


    private  Map<String, Object> mapDeleteFollowing = (Map<String, Object>) new HashMap<>().put("following", FieldValue.delete());


    private  Map<String, Object> mapDeleteFollowers = (Map<String, Object>) new HashMap<>().put("followers", FieldValue.delete());

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
        //String upperBound = prefix.substring(0, prefix.length()-1) + (last+1);
        String upperBound = prefix.substring(0, prefix.length()-1) + (char)(last+1);


        return users
                .whereGreaterThanOrEqualTo("normalizedUsername", prefix)
                //.whereLessThan("normalizedUsername", prefix + "\uf8ff")
                .whereLessThan("normalizedUsername", upperBound)
                .limit(maxNumber)
                .orderBy("normalizedUsername")
                .get()
                .continueWith(new Continuation<QuerySnapshot, List<User>>() {
                    @Override
                    public List<User> then(@NonNull Task<QuerySnapshot> task) {
                        System.out.println("check");
                        return task.getResult().toObjects(User.class);

                     }
                });
    }


    @Override
    public Task<Void> follow(final String userFollowing, final String userFollowed) {
        final DocumentReference userFollowingDoc = follow.document(userFollowing);
        final DocumentReference userFollowedDoc = follow.document(userFollowed);

        return firestore
                .runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                        DocumentSnapshot userFollowingSnapshot = transaction.get(userFollowingDoc);
                        DocumentSnapshot userFollowedSnapshot = transaction.get(userFollowedDoc);

                        List<String> following = (List<String>) userFollowingSnapshot.get("following");
                        List<String> followers = (List<String>) userFollowedSnapshot.get("followers");

                        if (following != null) {
                            if (!following.contains(userFollowed)) {
                                Map<String, Object> mapFollowing = new HashMap<>();
                                following.add(userFollowed);
                                mapFollowing.put("following",following);
                               // transaction.set(userFollowingDoc, mapDeleteFollowing,SetOptions.merge() );
                                transaction.set(userFollowingDoc, mapFollowing, SetOptions.merge());
                            }
                        }
                         else {
                            Map<String, Object> mapFollowing = new HashMap<>();
                            mapFollowing.put("following", Arrays.asList(userFollowed));
                            transaction.set(userFollowingDoc, mapFollowing, SetOptions.merge());
                         }


                        if (followers !=null) {

                            if (!followers.contains(userFollowing)) {

                                Map<String, Object> mapFollowed = new HashMap<>();
                                followers.add(userFollowing);
                                mapFollowed.put("followers",followers);
                               // transaction.set(userFollowedDoc, mapDeleteFollowers, SetOptions.merge());
                                transaction.set(userFollowedDoc, mapFollowed, SetOptions.merge());
                            }
                        }
                        else {
                            Map<String, Object> mapFollowed = new HashMap<>();
                            mapFollowed.put("followers", Arrays.asList(userFollowing));

                            transaction.set(userFollowedDoc, mapFollowed, SetOptions.merge());
                        }

                        return null;
                    }


                });
    }

    @Override
    public Task<Void> unFollow(final String userUnFollowing, final String userUnFollowed) {
        final DocumentReference userUnFollowingDoc = follow.document(userUnFollowing);
        final DocumentReference userUnFollowedDoc = follow.document(userUnFollowed);

        return firestore
                .runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                        DocumentSnapshot userUnFollowingSnapshot = transaction.get(userUnFollowingDoc);
                        DocumentSnapshot userUnFollowedSnapshot = transaction.get(userUnFollowedDoc);

                        List<String> following = (List<String>) userUnFollowingSnapshot.get("following");
                        List<String> followers = (List<String>) userUnFollowedSnapshot.get("followers");

                        if (following != null) {
                            if (following.contains(userUnFollowed)) {
                                Map<String, Object> mapFollowing = new HashMap<>();
                                following.remove(userUnFollowed);
                                mapFollowing.put("following", following);
                                transaction.set(userUnFollowingDoc, mapFollowing, SetOptions.merge());
                            }
                        }

                        if (followers !=null) {

                            if (followers.contains(userUnFollowing)) {

                                Map<String, Object> mapFollowed = new HashMap<>();
                                followers.remove(userUnFollowing);
                                mapFollowed.put("followers", followers);
                                transaction.set(userUnFollowedDoc, mapFollowed, SetOptions.merge());
                            }
                        }
                        return null;
                    }
                });
    }

    @Override
    public Task<List<String>> followingList(String userId) {
        return follow
                .document(userId)
                .get()
                .continueWith(new Continuation<DocumentSnapshot, List<String>>() {
                    @Override
                    public List<String> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        return  (List<String>)  task.getResult().get("following");
                    }
                });
    }

    @Override
    public Task<List<String>> followersList(String userId) {
        return follow
                .document(userId)
                .get()
                .continueWith(new Continuation<DocumentSnapshot, List<String>>() {
                    @Override
                    public List<String> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        return  (List<String>)  task.getResult().get("followers");
                    }
                });

    }



}
