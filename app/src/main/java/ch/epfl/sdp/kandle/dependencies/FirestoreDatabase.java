package ch.epfl.sdp.kandle.dependencies;

import androidx.annotation.NonNull;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ch.epfl.sdp.kandle.Post;
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


    @Override
    public Task<User> getUserByName(final String username) {
        return users
                .whereEqualTo("username", username)
                .get()
                .continueWith(task -> {
                    if (task.getResult().isEmpty()) return null;
                    return task.getResult().iterator().next().toObject(User.class);
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
                        assert(user != null);
                        System.out.println(user.getId());
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
                .whereGreaterThanOrEqualTo("username", prefix)
                .whereLessThan("username", upperBound)
                .limit(maxNumber)
                .orderBy("username")
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

        userIdFollowingList(userId).addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {

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
    public Task<Void> updateNickname(String nickname) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fullname", nickname);
        return loggedInUser().update(map);
    }

    @Override
    public Task<String> getNickname() {
        return loggedInUser().get().continueWith(task -> {
            DocumentSnapshot doc = task.getResult();
            return doc != null? (String) doc.get("fullname") : null;
        });
    }

    @Override

    public Task<Void> addPost(String userId, Post p) {
        final DocumentReference addedPostDoc = posts.document(p.getPostId());
        final DocumentReference userAddingPostDoc = users.document(userId);

        return firestore
                .runTransaction(transaction -> {

                    DocumentSnapshot userAddingPostSnapshot = transaction.get(userAddingPostDoc);
                    List<String> posts = (List<String>) userAddingPostSnapshot.get("posts");

                    if (posts != null) {
                        if (!posts.contains(p.getPostId())) {
                            Map<String, Object> mapPosts = new HashMap<>();
                            posts.add(p.getPostId());
                            mapPosts.put("posts",posts);
                            transaction.set(userAddingPostDoc, mapPosts, SetOptions.merge());
                        }
                    }
                    else {
                        Map<String, Object> mapPosts = new HashMap<>();
                        mapPosts.put("posts", Arrays.asList(p.getPostId()));
                        transaction.set(userAddingPostDoc, mapPosts, SetOptions.merge());
                    }


                    transaction.set(addedPostDoc, p);
                    return null;
                });
    }

    @Override
    public Task<Void> deletePost(String userId, Post p) {
        final DocumentReference deletedPostDoc = posts.document(p.getPostId());
        final DocumentReference userDeletingPostDoc = users.document(userId);

        return firestore
                .runTransaction(transaction -> {

                    DocumentSnapshot userDeletingPostSnapshot = transaction.get(userDeletingPostDoc);

                    List<String> posts = (List<String>) userDeletingPostSnapshot.get("posts");

                    if (posts != null) {
                        if (posts.contains(p.getPostId())) {
                            Map<String, Object> mapPosts = new HashMap<>();
                            posts.remove(p.getPostId());
                            mapPosts.put("posts",posts);
                            transaction.set(userDeletingPostDoc, mapPosts, SetOptions.merge());
                        }
                    }

                    deletedPostDoc.delete();

                    return null;
                });
    }

    @Override
    public Task<Void> likePost(String userId, String postId) {
        final DocumentReference likedPostDoc = posts.document(postId);

        return firestore
                .runTransaction(transaction -> {

                    DocumentSnapshot likedPostSnapchot = transaction.get(likedPostDoc);

                    List<String> likers = (List<String>) likedPostSnapchot.get("likers");

                    if (likers != null) {
                        if (!likers.contains(userId)) {
                            Map<String, Object> mapLikers = new HashMap<>();
                            likers.add(userId);
                            mapLikers.put("likers",likers);
                            transaction.set(likedPostDoc, mapLikers, SetOptions.merge());
                        }
                    }

                    return null;
                });

    }

    @Override
    public Task<Void> unlikePost(String userId, String postId) {
        final DocumentReference unlikedPostDoc = posts.document(postId);

        return firestore
                .runTransaction(transaction -> {

                    DocumentSnapshot unlikedPostSnapchot = transaction.get(unlikedPostDoc);

                    List<String> likers = (List<String>) unlikedPostSnapchot.get("likers");
                    if (likers != null) {
                        if (likers.contains(userId)) {
                            Map<String, Object> mapLikers = new HashMap<>();
                            likers.remove(userId);
                            mapLikers.put("likers",likers);
                            transaction.set(unlikedPostDoc, mapLikers, SetOptions.merge());
                        }
                    }
                    return null;
                });
    }
    
    /*
    @Override
    public Task<List<String>> likers(String postId) {
        return posts
                .document(postId)
                .get()
                .continueWith(task -> (List<String>)  task.getResult().get("likers"));
    }
    */

    @Override
    public Task<List<Post>> getPostsByUserId(String userId) {
        Task <List<String>> taskListPostId =  users
                .document(userId)
                .get()
                .continueWith(task -> (List<String>) task.getResult().get("posts"));

        TaskCompletionSource<List<Post>> source = new TaskCompletionSource<>();
        taskListPostId.addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {

                if (task.isSuccessful()){
                   posts.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<QuerySnapshot> task2) {

                           if (task2.isSuccessful()){
                               List<Post> posts = new ArrayList<>();

                               if (task2.getResult()!=null) {

                                   for (QueryDocumentSnapshot documentSnapshot : task2.getResult()) {
                                       String postId = (String) documentSnapshot.get("postId");
                                       if (task.getResult().contains(postId)) {
                                           posts.add(documentSnapshot.toObject(Post.class));
                                       }
                                   }
                               }

                               source.setResult(posts);
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

    public Task<String> getUsername() {
        return loggedInUser().get().continueWith(task -> {
            DocumentSnapshot doc = task.getResult();
            return doc != null? (String) doc.get("username") : null;
        });
    }



}
