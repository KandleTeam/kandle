package ch.epfl.sdp.kandle.Storage.caching;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.Storage.firebase.FirestoreDatabase;
import ch.epfl.sdp.kandle.Storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.Storage.room.PostDao;
import ch.epfl.sdp.kandle.Storage.room.UserDao;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.InternalStorage;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.exceptions.IncompleteDataException;
import ch.epfl.sdp.kandle.exceptions.NoInternetException;

public class CachedFirestoreDatabase implements Database {

    private Database database = DependencyManager.getDatabaseSystem();
    private InternalStorage internalStorage = DependencyManager.getInternalStorageSystem();
    private LocalDatabase localDatabase = DependencyManager.getLocalDatabase();
    private UserDao userDao;
    private PostDao postDao;

    public CachedFirestoreDatabase() {
        userDao = localDatabase.userDao();
        postDao = localDatabase.postDao();

    }

    @Override
    public Task<User> getUserByName(String username) {
        TaskCompletionSource source = new TaskCompletionSource();
        User user = userDao.getUserFromUsername(username);
        if (user == null) {
            if (DependencyManager.getNetworkStateSystem().isConnected()) {
                database.getUserByName(username).addOnCompleteListener(v -> {
                    if (v.isSuccessful()) {
                        if (v.getResult() != null) {
                            insertAndResizeUserLocalDb(v.getResult());
                        }
                        source.setResult(v.getResult());
                    } else {
                        source.setException(v.getException());
                    }
                });
            } else {
                source.setException(new NoInternetException());
            }
        } else {
            source.setResult(user);
        }
        return source.getTask();
    }


    @Override
    public Task<User> getUserById(String userId) {
        TaskCompletionSource source = new TaskCompletionSource();
        User user = userDao.getUserFromUserId(userId);
        System.out.println(userId);
        if (user == null) {
            if (DependencyManager.getNetworkStateSystem().isConnected()) {
                database.getUserById(userId).addOnCompleteListener(v -> {
                    if (v.isSuccessful()) {
                        if (v.getResult() != null) {
                            insertAndResizeUserLocalDb(v.getResult());
                        }
                        source.setResult(v.getResult());
                    } else {
                        source.setException(v.getException());
                    }
                });

            } else {
                source.setException(new NoInternetException());
            }
        } else {
            source.setResult(user);
        }
        return source.getTask();
    }

    private void insertAndResizeUserLocalDb(User user) {
        userDao.insertUser(user);
        if (userDao.getUserList().size() > LocalDatabase.MAX_USER_IN_DB) {
            userDao.storeOnly50Users();
        }
    }

    private void insertAndResizePostLocalDb(Post post) {
        postDao.insertPost(post);
        if (postDao.getPostList().size() > LocalDatabase.MAX_POST_IN_DB) {
            postDao.storeOnly50Posts();
        }
    }

    @Override
    public Task<Void> createUser(User user) {
        userDao.insertUser(user);
        return database.createUser(user);
    }


    @Override
    public Task<List<User>> searchUsers(String prefix, int maxNumber) {
        return database.searchUsers(prefix, maxNumber);
    }

    @Override
    public Task<Void> follow(String userFollowing, String userFollowed) {
        return database.follow(userFollowing, userFollowed);
    }

    @Override
    public Task<Void> unFollow(String userUnFollowing, String userUnFollowed) {
        return database.unFollow(userUnFollowing, userUnFollowed);
    }

    @Override
    public Task<List<String>> userIdFollowingList(String userId) {
        return database.userIdFollowingList(userId);
    }

    @Override
    public Task<List<String>> userIdFollowersList(String userId) {
        return database.userIdFollowersList(userId);
    }

    @Override
    public Task<List<User>> userFollowingList(String userId) {
        return database.userFollowingList(userId);
    }

    @Override
    public Task<List<User>> userFollowersList(String userId) {
        return database.userFollowersList(userId);
    }

    @Override
    public Task<Void> addPost(Post post) {
        insertAndResizePostLocalDb(post);
        return database.addPost(post);
    }

    @Override
    public Task<Void> deletePost(Post post) {
        postDao.deletePost(post);
        return database.deletePost(post);
    }

    /**
     * Adds userId to the list of all the likers of the post with postId
     * First of all updates the post locally if the aimed post is present
     * Then updates the post remotely
     *
     * @param userId
     * @param postId
     * @return
     */
    @Override
    public Task<Void> likePost(String userId, String postId) {
        Post toUpdate = postDao.getPostFromPostId(postId);


        if (toUpdate != null) {
            List<String> newLikers = new ArrayList<>();
            newLikers.add(userId);
            for (String user : toUpdate.getLikers()) {
                newLikers.add(user);
            }
            toUpdate.setLikers(newLikers);
            postDao.updatePost(toUpdate);
        }
        //TODO get the post here and add it to the local DB if there is no post found locally
        return database.likePost(userId, postId);

    }

    /**
     * Removes userId from the list of all the likers of the post with postId
     * First of all updates the post locally if the aimed post is present
     * Then updates the post remotely
     *
     * @param userId
     * @param postId
     * @return
     */
    @Override
    public Task<Void> unlikePost(String userId, String postId) {
        Post toUpdate = postDao.getPostFromPostId(postId);

        if (toUpdate != null) {
            List<String> newLikers = new ArrayList<>();
            for (String user : toUpdate.getLikers()) {
                newLikers.add(user);
            }
            newLikers.remove(userId);
            toUpdate.setLikers(newLikers);

            postDao.updatePost(toUpdate);
        }
        //TODO get the post here and add it to the local DB if there is no post found locally
        return database.unlikePost(userId, postId);
    }

    /**
     * This method returns all the user that liked the post with postId
     * We first check locally because the likers will be the same locally or remotely.
     *
     * @param postId
     * @return
     */
    @Override
    public Task<List<User>> getLikers(String postId) {
        Post post = postDao.getPostFromPostId(postId);
        TaskCompletionSource source = new TaskCompletionSource();
        List<User> users = new ArrayList<>();
        List<Task<User>> usersTask = new ArrayList<>();
        if (post != null) {

            for (String userId : post.getLikers()) {
                usersTask.add(this.getUserById(userId).addOnCompleteListener(v -> {
                    if (v.isSuccessful()) {
                        users.add(v.getResult());
                    }
                }));
            }
            Tasks.whenAll(usersTask).addOnCompleteListener(v -> {
                //If the size is different we weren't able to retrieve all the users thus
                //the app is offline
                if (users.size() == 0) {
                    source.setException(new NoInternetException());
                } else if (users.size() != post.getLikers().size()) {
                    source.setException(new IncompleteDataException());
                } else {
                    source.setResult(users);
                }
            });

        } else {
            if (DependencyManager.getNetworkStateSystem().isConnected()) {
                return database.getLikers(postId);
            } else {
                source.setException(new NoInternetException());
            }
        }

        return source.getTask();
    }

    /**
     * Returns all the posts a user has made
     * If the app is offline this might not return all the posts create by the user from th fact
     * that they have to be stored locally i.e only the stored post will be returned
     * If the app is online we combine the local and remote data and update the local Db with the remote one
     *
     * @param userId
     * @return
     */
    @Override
    public Task<List<Post>> getPostsByUserId(String userId) {
        TaskCompletionSource source = new TaskCompletionSource();
        //This maybe incomplete
        List<Post> posts = localDatabase.userWithPostsDao().getPostsFromUserId(userId);
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            database.getPostsByUserId(userId).addOnCompleteListener(v -> {
                List<Post> result = v.getResult();
                if (result.removeAll(posts)) {
                    for (Post p : result) {
                        insertAndResizePostLocalDb(p);
                    }
                }
                posts.addAll(v.getResult());
                source.setResult(posts);
            });
        } else {
            source.setResult(localDatabase.userWithPostsDao().getPostsFromUserId(userId));
            //TODO Set a flag to handle incompltete information, we don't want to throw and excpetion here because we still may have information
        }
        return source.getTask();

    }

    /**
     * Returns the nearby posts. If the app is offline it will return the cached nearby posts
     * but there might be not every nearby post in the local cache therefor the list is incomplete
     *
     * @param latitude
     * @param longitude
     * @param distance
     * @return
     */
    @Override
    public Task<List<Post>> getNearbyPosts(double latitude, double longitude, double distance) {
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.getNearbyPosts(latitude, longitude, distance);
        } else {
            TaskCompletionSource source = new TaskCompletionSource();
            List<Post> nearbyPosts = new ArrayList<>();
            List<Post> posts = postDao.getPostList();
            for (Post p : posts) {
                if (FirestoreDatabase.nearby(latitude, longitude, p.getLatitude(), p.getLongitude(), distance)) {
                    nearbyPosts.add(p);
                }
            }
            source.setResult(nearbyPosts);
            return source.getTask();
        }

    }


    //-----------------This part handles the local user-----------------------
    @Override
    public Task<Void> updateProfilePicture(String uri) {
        LoggedInUser.getInstance().setImageURL(uri);
        User user = internalStorage.getCurrentUser();
        user.setImageURL(uri);
        internalStorage.updateUser(user);
        userDao.updateUser(user);
        return database.updateProfilePicture(uri);
    }

    @Override
    public Task<Void> updateNickname(String nickname) {
        LoggedInUser.getInstance().setNickname(nickname);
        User user = internalStorage.getCurrentUser();
        user.setNickname(nickname);
        internalStorage.updateUser(user);
        userDao.updateUser(user);
        return database.updateNickname(nickname);
    }



}

