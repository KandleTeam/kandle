package ch.epfl.sdp.kandle.storage.firebase;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.storage.Database;

public class FirestoreDatabase implements Database {

    private static final int LIKE_BONUS_DISTANCE = 250;
    private static final int DATE_MALUS_DISTANCE = 150;

    private static final FirebaseFirestore FIRESTORE = FirebaseFirestore.getInstance();
    private static final FirestoreDatabase INSTANCE = new FirestoreDatabase();
    private static final CollectionReference USERS = FIRESTORE.collection("users");
    private static final CollectionReference USERNAMES = FIRESTORE.collection("usernames");
    private static final CollectionReference NOTIFICATION = FIRESTORE.collection("notification");
    private static final CollectionReference POSTS = FIRESTORE.collection("posts");
    private static final CollectionReference FOLLOW = FIRESTORE.collection("follow");

    // Array fields of the documents in collection 'follow'
    private static final String FOLLOWERS = "followers";
    private static final String FOLLOWING = "following";
    private static final String CLOSE_FOLLOWERS = "close_followers";
    private static final String NOTIFICATION_NEW_FOLLOWER_TITLE = "New Follower !";
    private static final String NOTIFICATION_NEW_FOLLOWER_TEXT = "YOUHOU ! You have a new Follower !";
    private static final String NOTIFICATION_LIKE_TITLE = "New like !";
    private static final String NOTIFICATION_LIKE_TEXT = "YOUHOU ! Someone has liked your post !";


    private FirestoreDatabase() {
        // For now, disable caching
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FIRESTORE.setFirestoreSettings(settings);
    }

    public static FirestoreDatabase getInstance() {
        return INSTANCE;
    }

    public static boolean nearby(double latitude, double longitude, double postLatitude, double postLongitude, double distance, int numLikes, long numDays) {

        LatLng startLatLng = new LatLng(latitude, longitude);
        LatLng endLatLng = new LatLng(postLatitude, postLongitude);
        return SphericalUtil.computeDistanceBetween(startLatLng, endLatLng) <= distance + numLikes * LIKE_BONUS_DISTANCE - numDays * DATE_MALUS_DISTANCE;

    }

    private DocumentReference loggedInUser() {
        return USERS.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    public Task<User> getUserByName(final String username) {
        return USERS
                .whereEqualTo("username", username)
                .get()
                .continueWith(task -> {
                    if (task.getResult().isEmpty()) return null;
                    return task.getResult().iterator().next().toObject(User.class);
                });
    }

    @Override
    public Task<User> getUserById(final String userId) {
        return USERS
                .document(userId)
                .get()
                .continueWith(task -> {
                    User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                    assert (user != null);
                    return user;
                });
    }

    @Override
    public Task<Void> createUser(final User user) {

        final DocumentReference usernameDoc = USERNAMES.document(user.getUsername());
        final DocumentReference userDoc = USERS.document(user.getId());

        return FIRESTORE
                .runTransaction(transaction -> {

                    DocumentSnapshot usernameSnapshot = transaction.get(usernameDoc);

                    if (usernameSnapshot.exists()) {
                        throw new FirebaseFirestoreException("Username already taken!", FirebaseFirestoreException.Code.ALREADY_EXISTS);
                    } else {

                        Map<String, Object> usernameMap = new HashMap<>();
                        usernameMap.put("username", user.getUsername());
                        transaction.set(usernameDoc, usernameMap);

                        transaction.set(userDoc, user);

                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
                            String token = instanceIdResult.getToken();
                            Map<String, Object> deviceMap = new HashMap<>();
                            deviceMap.put("deviceId", token);
                            USERS.document(user.getId()).set(deviceMap, SetOptions.merge());
                        });
                    }

                    return null;
                });

    }

    @Override
    public Task<List<User>> searchUsers(String prefix, int maxNumber) {
        char last = prefix.charAt(prefix.length() - 1);
        String upperBound = prefix.substring(0, prefix.length() - 1) + (char) (last + 1);

        return USERS
                .whereGreaterThanOrEqualTo("username", prefix)
                .whereLessThan("username", upperBound)
                .limit(maxNumber)
                .orderBy("username")
                .get()
                .continueWith(task -> task.getResult().toObjects(User.class));
    }

    @Override
    public Task<List<User>> usersList() {
        return USERS.get().continueWith(task -> task.getResult().toObjects(User.class));
    }

    @Override
    public Task<Void> follow(final String userFollowing, final String userFollowed) {
        final DocumentReference userFollowingDoc = FOLLOW.document(userFollowing);
        final DocumentReference userFollowedDoc = FOLLOW.document(userFollowed);

        return FIRESTORE
                .runTransaction(transaction -> {
                    DocumentSnapshot userFollowingSnapshot = transaction.get(userFollowingDoc);
                    DocumentSnapshot userFollowedSnapshot = transaction.get(userFollowedDoc);
                    List<String> following = (List<String>) userFollowingSnapshot.get(FOLLOWING);
                    List<String> followers = (List<String>) userFollowedSnapshot.get(FOLLOWERS);

                    addFollow(userFollowed, userFollowingDoc, transaction, following, FOLLOWING);
                    addFollow(userFollowing, userFollowedDoc, transaction, followers, FOLLOWERS);

                    return null;
                });
    }

    private void addFollow(String userId, DocumentReference userDoc, Transaction transaction, List<String> followList, String process) {
        if (followList != null) {
            if (!followList.contains(userId)) {
                Map<String, Object> mapFollowing = new HashMap<>();
                followList.add(userId);
                mapFollowing.put(process, followList);
                transaction.set(userDoc, mapFollowing, SetOptions.merge());
                if (process.equals(FOLLOWING))
                    sendNotification(userId, NOTIFICATION_NEW_FOLLOWER_TITLE, NOTIFICATION_NEW_FOLLOWER_TEXT);
            }
        } else {
            Map<String, Object> mapFollowing = new HashMap<>();
            mapFollowing.put(process, Collections.singletonList(userId));
            transaction.set(userDoc, mapFollowing, SetOptions.merge());
            if (process.equals(FOLLOWING))
                sendNotification(userId, NOTIFICATION_NEW_FOLLOWER_TITLE, NOTIFICATION_NEW_FOLLOWER_TEXT);
        }
    }

    private void sendNotification(String userFollowedId, String title, String text) {
        USERS.document(userFollowedId).get().addOnSuccessListener(documentSnapshot -> {
            String deviceId = (String) documentSnapshot.get("deviceId");
            if (deviceId != null) {
                Map<String, String> notificationData = new HashMap<>();
                notificationData.put("toUserId", userFollowedId);
                notificationData.put("toDeviceId", deviceId);
                notificationData.put("title", title);
                notificationData.put("text", text);
                NOTIFICATION.document(UUID.randomUUID().toString()).set(notificationData);
            }
        });


    }

    @Override
    public Task<Void> unFollow(final String userUnFollowing, final String userUnFollowed) {
        final DocumentReference userUnFollowingDoc = FOLLOW.document(userUnFollowing);
        final DocumentReference userUnFollowedDoc = FOLLOW.document(userUnFollowed);

        return FIRESTORE
                .runTransaction(transaction -> {
                    DocumentSnapshot userUnFollowingSnapshot = transaction.get(userUnFollowingDoc);
                    DocumentSnapshot userUnFollowedSnapshot = transaction.get(userUnFollowedDoc);
                    List<String> following = (List<String>) userUnFollowingSnapshot.get(FOLLOWING);
                    List<String> followers = (List<String>) userUnFollowedSnapshot.get(FOLLOWERS);

                    deleteFollow(userUnFollowed, userUnFollowingDoc, transaction, following, FOLLOWING);
                    deleteFollow(userUnFollowing, userUnFollowedDoc, transaction, followers, FOLLOWERS);

                    return null;
                });
    }

    private void deleteFollow(String userId, DocumentReference userDoc, Transaction transaction, List<String> followList, String process) {
        if (followList != null) {
            if (followList.contains(userId)) {
                Map<String, Object> mapFollow = new HashMap<>();
                followList.remove(userId);
                mapFollow.put(process, followList);
                transaction.set(userDoc, mapFollow, SetOptions.merge());
            }
        }
    }

    @Override
    public Task<Void> setCloseFollower(final String userFollowing, final String userFollowed) {
        final DocumentReference userFollowingDoc = FOLLOW.document(userFollowing);
        final DocumentReference userFollowedDoc = FOLLOW.document(userFollowed);

        return FIRESTORE
                .runTransaction(transaction -> {

                    DocumentSnapshot userFollowingSnapshot = transaction.get(userFollowingDoc);
                    DocumentSnapshot userFollowedSnapshot = transaction.get(userFollowedDoc);

                    List<String> followers = (List<String>) userFollowedSnapshot.get(FOLLOWERS);
                    List<String> closeFollowers = (List<String>) userFollowedSnapshot.get(CLOSE_FOLLOWERS);
                    if (closeFollowers != null) {
                        if (followers.contains(userFollowing) && !closeFollowers.contains(userFollowing)) {
                            /*Map<String, Object> mapCloseFollowed = new HashMap<>();
                            closeFollowers.add(userFollowing);
                            mapCloseFollowed.put(CLOSE_FOLLOWERS, closeFollowers);
                            transaction.set(userFollowedDoc, mapCloseFollowed, SetOptions.merge());*/
                            setCloseFollowers(userFollowing, userFollowedDoc, transaction, closeFollowers);
                        }
                    } else {
                        /*Map<String, Object> mapCloseFollowed = new HashMap<>();
                        mapCloseFollowed.put(CLOSE_FOLLOWERS, Arrays.asList(userFollowing));
                        transaction.set(userFollowedDoc, mapCloseFollowed, SetOptions.merge());*/
                        setCloseFollowers(userFollowing, userFollowedDoc, transaction, new ArrayList<>());
                    }

                    return null;
                });
    }

    private void setCloseFollowers(String userId, DocumentReference userDoc, Transaction transaction, List<String> closeFollowers) {
        Map<String, Object> mapCloseFollowed = new HashMap<>();
        closeFollowers.add(userId);
        mapCloseFollowed.put(CLOSE_FOLLOWERS, closeFollowers);
        transaction.set(userDoc, mapCloseFollowed, SetOptions.merge());
    }

    @Override
    public Task<Void> unsetCloseFollower(final String userFollowing, final String userFollowed) {
        final DocumentReference userFollowingDoc = FOLLOW.document(userFollowing);
        final DocumentReference userFollowedDoc = FOLLOW.document(userFollowed);

        return FIRESTORE
                .runTransaction(transaction -> {

                    DocumentSnapshot userFollowingSnapshot = transaction.get(userFollowingDoc);
                    DocumentSnapshot userFollowedSnapshot = transaction.get(userFollowedDoc);
                    List<String> closeFollowers = (List<String>) userFollowedSnapshot.get(CLOSE_FOLLOWERS);
                    if (closeFollowers != null) {
                        if (closeFollowers.contains(userFollowing)) {
                            Map<String, Object> mapFollowed = new HashMap<>();
                            closeFollowers.remove(userFollowing);
                            mapFollowed.put(CLOSE_FOLLOWERS, closeFollowers);
                            transaction.set(userFollowedDoc, mapFollowed, SetOptions.merge());
                        }
                    }
                    return null;
                });
    }

    /**
     * Returns a list of userIds of the users followed by the specified user, or following the
     * specified user, depending on the `field` parameter
     *
     * @param userId userId of the concerned user
     * @param field  should be either FOLLOWING or FOLLOWERS (use constants)
     * @return a list containing the userIds of following/followed users (be it empty)
     */
    private Task<List<String>> getFollowerOrFollowedListTask(String userId, String field) {
        return FOLLOW
                .document(userId)
                .get()
                .continueWith(task -> {
                    List<String> userIds = (List<String>) task.getResult().get(field);
                    if (userIds == null) return new ArrayList<>();
                    else return userIds;
                });
    }

    /**
     * Asynchronously retrieve the userIds of the users who the specified user follows
     *
     * @param userId the concerned user
     * @return a list of userIds
     */
    @Override
    public Task<List<String>> userIdFollowingList(String userId) {
        return getFollowerOrFollowedListTask(userId, FOLLOWING);
    }

    /**
     * Asynchronously retrieve the userIds of the users following the specified user
     *
     * @param userId the concerned user
     * @return a list of userIds
     */
    @Override
    public Task<List<String>> userIdFollowersList(String userId) {
        return getFollowerOrFollowedListTask(userId, FOLLOWERS);
    }

    @Override
    public Task<List<String>> userIdCloseFollowersList(String userId) {
        return getFollowerOrFollowedListTask(userId, CLOSE_FOLLOWERS);
    }

    private OnCompleteListener<List<String>> userListCompleteListener(TaskCompletionSource source) {
        return task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    USERS.get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            List<User> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task2.getResult()) {
                                String id = (String) document.get("id");
                                if (task.getResult().contains(id)) {
                                    users.add(document.toObject(User.class));
                                }
                            }
                            source.setResult(users);
                        } else {
                            source.setException(new Exception(task2.getException().getMessage()));
                        }
                    });
                } else {
                    source.setResult(new ArrayList<User>());
                }
            } else {
                source.setException(new Exception(task.getException().getMessage()));
            }
        };
    }

    @Override
    public Task<List<User>> userFollowingList(String userId) {
        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
        userIdFollowingList(userId).addOnCompleteListener(userListCompleteListener(source));
        return source.getTask();
    }

    @Override
    public Task<List<User>> userFollowersList(String userId) {
        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
        userIdFollowersList(userId).addOnCompleteListener(userListCompleteListener(source));
        return source.getTask();
    }

    @Override
    public Task<List<User>> userCloseFollowersList(String userId) {
        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
        userIdCloseFollowersList(userId).addOnCompleteListener(userListCompleteListener(source));
        return source.getTask();
    }

    @Override
    public Task<Void> updateProfilePicture(String uri) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("imageURL", uri);
        return loggedInUser().update(map);
    }

    @Override
    public Task<Void> updateNickname(String nickname) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nickname", nickname);
        return loggedInUser().update(map);
    }

    @Override

    public Task<Void> addPost(Post p) {
        final DocumentReference addedPostDoc = POSTS.document(p.getPostId());
        final DocumentReference userAddingPostDoc = USERS.document(p.getUserId());

        return FIRESTORE
                .runTransaction(transaction -> {

                    DocumentSnapshot userAddingPostSnapshot = transaction.get(userAddingPostDoc);
                    List<String> postsIds = (List<String>) userAddingPostSnapshot.get("postsIds");

                    if (postsIds != null) {
                        if (!postsIds.contains(p.getPostId())) {
                            Map<String, Object> mapPosts = new HashMap<>();
                            postsIds.add(p.getPostId());
                            mapPosts.put("postsIds", postsIds);
                            transaction.set(userAddingPostDoc, mapPosts, SetOptions.merge());
                        }
                    } else {
                        Map<String, Object> mapPosts = new HashMap<>();
                        mapPosts.put("postsIds", Arrays.asList(p.getPostId()));
                        transaction.set(userAddingPostDoc, mapPosts, SetOptions.merge());
                    }

                    transaction.set(addedPostDoc, p);
                    return null;
                });
    }

    @Override
    public Task<Void> editPost(Post p, String postId) {
        final DocumentReference editedPostDoc = POSTS.document(postId);
        return FIRESTORE
                .runTransaction(transaction -> {
                    transaction.set(editedPostDoc, p);
                    return null;
                });
    }

    @Override
    public Task<Void> deletePost(Post p) {
        final DocumentReference deletedPostDoc = POSTS.document(p.getPostId());
        final DocumentReference userDeletingPostDoc = USERS.document(p.getUserId());

        return FIRESTORE
                .runTransaction(transaction -> {

                    DocumentSnapshot userDeletingPostSnapshot = transaction.get(userDeletingPostDoc);

                    List<String> postsIds = (List<String>) userDeletingPostSnapshot.get("postsIds");

                    if (postsIds != null) {
                        if (postsIds.contains(p.getPostId())) {
                            Map<String, Object> mapPosts = new HashMap<>();
                            postsIds.remove(p.getPostId());
                            mapPosts.put("postsIds", postsIds);
                            transaction.set(userDeletingPostDoc, mapPosts, SetOptions.merge());
                        }
                    }


                    deletedPostDoc.delete();

                    return null;
                });
    }

    @Override
    public Task<Void> likePost(String userId, String postId) {
        final DocumentReference likedPostDoc = POSTS.document(postId);
        return FIRESTORE
                .runTransaction(likePostTransaction(likedPostDoc, userId, "like"));
    }

    @Override
    public Task<Void> unlikePost(String userId, String postId) {
        final DocumentReference unlikedPostDoc = POSTS.document(postId);
        return FIRESTORE
                .runTransaction(likePostTransaction(unlikedPostDoc, userId, "unlike"));
    }

    private Transaction.Function<Void> likePostTransaction(DocumentReference postDoc, String userId, String process) {
        return transaction -> {
            DocumentSnapshot postSnapchot = transaction.get(postDoc);
            List<String> likers = (List<String>) postSnapchot.get("likers");
            Map<String, Object> mapLikers = new HashMap<>();
            if (process.equals("like")) {
                likers.add(userId);
                sendNotification((String) postSnapchot.get("userId"), NOTIFICATION_LIKE_TITLE, NOTIFICATION_LIKE_TEXT);
            } else likers.remove(userId);
            mapLikers.put("likers", likers);
            transaction.set(postDoc, mapLikers, SetOptions.merge());
            return null;
        };
    }

    @Override
    public Task<List<User>> getLikers(String postId) {
        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
        Task<List<String>> getLikersIdTask = POSTS
                .document(postId)
                .get()
                .continueWith(task -> (List<String>) task.getResult().get("likers"));

        getLikersIdTask.addOnCompleteListener(userListCompleteListener(source));
        return source.getTask();
    }

    @Override
    public Task<List<Post>> getPostsByUserId(String userId) {
        Task<List<String>> taskListPostId = USERS
                .document(userId)
                .get()
                .continueWith(task -> (List<String>) task.getResult().get("postsIds"));

        TaskCompletionSource<List<Post>> source = new TaskCompletionSource<>();
        taskListPostId.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                POSTS.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        List<Post> posts = new ArrayList<>();
                        if (task2.getResult() != null) {
                            for (QueryDocumentSnapshot documentSnapshot : task2.getResult()) {
                                String postId = (String) documentSnapshot.get("postId");
                                if (task.getResult().contains(postId)) {
                                    posts.add(documentSnapshot.toObject(Post.class));
                                }
                            }
                        }
                        source.setResult(posts);
                    } else {
                        source.setException(new Exception(task2.getException().getMessage()));
                    }
                });
            } else {
                source.setException(new Exception(task.getException().getMessage()));
            }
        });
        return source.getTask();
    }

    @Override
    public Task<List<Post>> getNearbyPosts(double latitude, double longitude, double distance) {

        TaskCompletionSource<List<Post>> source = new TaskCompletionSource<>();
        POSTS.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Post> posts = new ArrayList<>();

                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {

                    if ((documentSnapshot.get("latitude") != null) && (documentSnapshot.get("longitude") != null)) {
                        double postLatitude = (double) documentSnapshot.get("latitude");
                        double postLongitude = (double) documentSnapshot.get("longitude");
                        List<String> likers = (List<String>) documentSnapshot.get("likers");

                        Date postDate = ((Timestamp) documentSnapshot.get("date")).toDate();
                        Date now = new Date();
                        long numDays = TimeUnit.DAYS.convert(Math.abs(now.getTime() - postDate.getTime()), TimeUnit.MILLISECONDS);
                        int numLikes = 0;
                        if (likers != null) numLikes = likers.size();

                        if ((nearby(latitude, longitude, postLatitude, postLongitude, distance, numLikes, numDays))) {
                            posts.add(documentSnapshot.toObject(Post.class));
                        }
                    }

                }
                Collections.sort(posts, (o1, o2) -> o2.getLikers().size() - o1.getLikers().size());
                source.setResult(posts);

            } else {
                source.setException(task.getException());
            }
        });
        return source.getTask();

    }

    @Override
    public Task<Post> getPostByPostId(String postId) {
        return POSTS
                .document(postId)
                .get()
                .continueWith(task -> {
                    Post post = Objects.requireNonNull(task.getResult()).toObject(Post.class);
                    assert (post != null);
                    return post;
                });
    }

    @Override
    public Task<List<Post>> getParticipatingEvents() {
        return POSTS
                .whereEqualTo("type", Post.EVENT)
                .whereArrayContains("likers", LoggedInUser.getInstance().getId())
                .get()
                .continueWith(task -> task.getResult().toObjects(Post.class));
    }

    @Override
    public Task<Void> updateHighScore(int highScore) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("highScore", highScore);
        return loggedInUser().update(map);
    }


}
