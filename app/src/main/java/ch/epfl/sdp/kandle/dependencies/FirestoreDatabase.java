package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ch.epfl.sdp.kandle.User;

public class FirestoreDatabase implements Database {

    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private static final FirestoreDatabase instance = new FirestoreDatabase();

    private static final CollectionReference users = firestore.collection("users");
    private static final CollectionReference usernames = firestore.collection("usernames");
    private static final CollectionReference posts = firestore.collection("posts");
    private static final CollectionReference follow = firestore.collection("follow");

    private DocumentReference loggedInUser() { return users.document(FirebaseAuth.getInstance().getCurrentUser().getUid());}


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

/*
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

 */


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
                        }

                         else {

                            Map<String,Object> map = new HashMap<>();
                            map.put("username", user.getUsername());


                            transaction.set(usernameDoc, map);
                            transaction.set(userDoc, user);
                        }

                    return null;
                });

    }

    @Override
    public Task<List<User>> searchUsers(String prefix, int maxNumber) {
        char last = prefix.charAt(prefix.length()-1);
        String upperBound = prefix.substring(0, prefix.length()-1) + (char)(last+1);


        return users
                .whereGreaterThanOrEqualTo("normalizedUsername", prefix)
                .whereLessThan("normalizedUsername", upperBound)
                .limit(maxNumber)
                .orderBy("normalizedUsername")
                .get()
                .continueWith(task -> task.getResult().toObjects(User.class));
    }


    @Override
    public Task<Void> follow(final String userFollowing, final String userFollowed) {
        final DocumentReference userFollowingDoc = follow.document(userFollowing);
        final DocumentReference userFollowedDoc = follow.document(userFollowed);

        return firestore
                .runTransaction(transaction -> {

                    DocumentSnapshot userFollowingSnapshot = transaction.get(userFollowingDoc);
                    DocumentSnapshot userFollowedSnapshot = transaction.get(userFollowedDoc);

                    List<String> following = (List<String>) userFollowingSnapshot.get("following");
                    List<String> followers = (List<String>) userFollowedSnapshot.get("followers");

                    if (following != null) {
                        if (!following.contains(userFollowed)) {
                            Map<String, Object> mapFollowing = new HashMap<>();
                            following.add(userFollowed);
                            mapFollowing.put("following",following);
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
                            transaction.set(userFollowedDoc, mapFollowed, SetOptions.merge());
                        }
                    }
                    else {
                        Map<String, Object> mapFollowed = new HashMap<>();
                        mapFollowed.put("followers", Arrays.asList(userFollowing));

                        transaction.set(userFollowedDoc, mapFollowed, SetOptions.merge());
                    }

                    return null;
                });
    }

    @Override
    public Task<Void> unFollow(final String userUnFollowing, final String userUnFollowed) {
        final DocumentReference userUnFollowingDoc = follow.document(userUnFollowing);
        final DocumentReference userUnFollowedDoc = follow.document(userUnFollowed);

        return firestore
                .runTransaction(transaction -> {

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

                    if (followers != null) {

                        if (followers.contains(userUnFollowing)) {

                            Map<String, Object> mapFollowed = new HashMap<>();
                            followers.remove(userUnFollowing);
                            mapFollowed.put("followers", followers);
                            transaction.set(userUnFollowedDoc, mapFollowed, SetOptions.merge());
                        }
                    }
                    return null;
                });
    }

    @Override
    public Task<List<String>> userIdFollowingList(String userId) {
        return follow
                .document(userId)
                .get()
                .continueWith(task -> (List<String>)  task.getResult().get("following"));
    }

    @Override
    public Task<List<String>> userIdFollowersList(String userId) {
        return follow
                .document(userId)
                .get()
                .continueWith(task -> (List<String>)  task.getResult().get("followers"));
    }

    @Override
    public Task<List<User>> userFollowingList(String userId) {

       // Task<List<String>> taskUserIdFollowing = userIdFollowingList(userId);
        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();

        userIdFollowingList(userId).addOnCompleteListener(task -> {

            if (task.isSuccessful()){

                if (task.getResult() != null) {

                    users.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                            if (task2.isSuccessful()){
                                List<User> users = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task2.getResult()) {
                                    String id =  (String) document.get("id");
                                    if (task.getResult().contains(id)){
                                        users.add(document.toObject(User.class));
                                    }
                                }

                                source.setResult(users);
                            }

                            else {
                                source.setException( new Exception(task2.getException().getMessage()));
                            }

                        }
                    });

                }
                else {
                    source.setResult(null);
                }
            }
            else {
                source.setException( new Exception(task.getException().getMessage()));
        }
        });

        return source.getTask();
    }

    @Override
    public Task<List<User>> userFollowersList(String userId) {
       // Task<List<String>> taskUserIdFollowers = userIdFollowersList(userId);
        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();

        userIdFollowersList(userId).addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {

                if (task.isSuccessful()){

                    users.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                            if (task2.isSuccessful()){
                                List<User> users = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task2.getResult()) {
                                    String id =  (String) document.get("id");
                                    if (task.getResult().contains(id)){
                                        users.add(document.toObject(User.class));
                                    }
                                }

                                source.setResult(users);
                            }

                            else {
                                source.setException( new Exception(task2.getException().getMessage()));
                            }

                        }
                    });
                }
                else {
                    source.setException( new Exception(task.getException().getMessage()));
                }
            }
        });

        return source.getTask();
    }




    @Override
    public Task<Void> updateProfilePicture(String uri) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("imageURL", uri);
        return loggedInUser().update(map);
    }

    @Override
    public Task<String> getProfilePicture() {
        return loggedInUser().get().continueWith(task -> {
            DocumentSnapshot doc = task.getResult();
            return doc != null? (String) doc.get("imageURL") : null;
        });
    }

    @Override
    public Task<String> getUsername() {
        return loggedInUser().get().continueWith(task -> {
            DocumentSnapshot doc = task.getResult();
            return doc != null? (String) doc.get("fullname") : null;
        });
    }


}
