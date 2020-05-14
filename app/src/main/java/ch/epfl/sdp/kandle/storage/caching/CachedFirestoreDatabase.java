package ch.epfl.sdp.kandle.storage.caching;


import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.InternalStorage;
import ch.epfl.sdp.kandle.exceptions.NoInternetException;
import ch.epfl.sdp.kandle.storage.firebase.FirestoreDatabase;
import ch.epfl.sdp.kandle.storage.room.LocalDatabase;
import ch.epfl.sdp.kandle.storage.room.PostDao;
import ch.epfl.sdp.kandle.storage.room.UserDao;

public class CachedFirestoreDatabase implements Database {

    private final Database database = DependencyManager.getDatabaseSystem();
    private final InternalStorage internalStorage = DependencyManager.getInternalStorageSystem();
    private final LocalDatabase localDatabase = DependencyManager.getLocalDatabase();
    private final UserDao userDao;
    private final PostDao postDao;

    public CachedFirestoreDatabase() {
        userDao = localDatabase.userDao();
        postDao = localDatabase.postDao();

    }

    @SuppressWarnings("unchecked")
    @Override
    public Task<User> getUserByName(String username) {

        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.getUserByName(username).addOnCompleteListener( v -> {
                if (v.isSuccessful() && v.getResult() != null) {
                    insertAndResizeUserLocalDb(v.getResult());
                }
            });
        } else {
            TaskCompletionSource<User> source = new TaskCompletionSource<>();
            source.setResult(userDao.getUserFromUsername(username));
            return source.getTask();
        }
    }


    @Override
    public Task<User> getUserById(String userId) {

        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.getUserById(userId).addOnCompleteListener( v -> {
                if (v.isSuccessful()) {
                    insertAndResizeUserLocalDb(v.getResult());
                }
            });
        } else {
            TaskCompletionSource<User> source = new TaskCompletionSource<>();
            source.setResult(userDao.getUserFromUserId(userId));
            return source.getTask();
        }
    }

    /**
     * Inserts a new user into the localdatabase and resizes it if necessary
     * This function has not to be called in the UI thread
     *
     * @param user the user to insert into the local database
     */
    private void insertAndResizeUserLocalDb(User user) {
        userDao.insertUser(user);
        if (userDao.getUserList().size() > LocalDatabase.MAX_USER_IN_DB) {
            userDao.storeOnly50Users();
        }
    }

    /**
     * Inserts a new post into the localdatabase and resizes it if necessary
     * This function has not to be called in the UI thread
     *
     * @param post the user to insert into the local database
     */
    private void insertAndResizePostLocalDb(Post post) {
        postDao.insertPost(post);
        if (postDao.getPostList().size() > LocalDatabase.MAX_POST_IN_DB) {
            postDao.storeOnly50Posts();
        }
    }


    private Task<Void> NoInternetExpcetionTask() {
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setException(new NoInternetException());
        return source.getTask();
    }

    @Override
    public Task<Void> createUser(User user) {
        return database.createUser(user).addOnCompleteListener( v -> {
            if (v.isSuccessful()) {
                userDao.insertUser(user);
            }
        });
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
        return database.addPost(post).addOnCompleteListener(v -> {
            if (v.isSuccessful()) {
                insertAndResizePostLocalDb(post);
            }
        });
    }

    @Override
    public Task<Void> editPost(Post p, String postId) {
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.editPost(p, postId).addOnCompleteListener(v -> {
                if (v.isSuccessful()) {
                    postDao.updatePost(p);
                }
            });
        } else {
            return NoInternetExpcetionTask();
        }
    }


    @Override
    public Task<Void> deletePost(Post post) {
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.deletePost(post).addOnCompleteListener(v -> {
                if (v.isSuccessful()) {
                    postDao.deletePost(post);
                }
            });
        } else {
            return NoInternetExpcetionTask();
        }
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
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.likePost(userId, postId).addOnCompleteListener( v -> {
                Post toUpdate = postDao.getPostFromPostId(postId);
                if (toUpdate != null) {
                    List<String> newLikers = new ArrayList<>();
                    newLikers.addAll(toUpdate.getLikers());
                    newLikers.add(userId);
                    toUpdate.setLikers(newLikers);
                    postDao.updatePost(toUpdate);
                }
            });
        } else {
            Log.d("NOCONNECT", "indeed throws the task excpetion");
            return NoInternetExpcetionTask();
        }

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
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.unlikePost(userId, postId).addOnCompleteListener(v -> {
                Post toUpdate = postDao.getPostFromPostId(postId);
                if (toUpdate != null) {
                    List<String> newLikers = new ArrayList<>();
                    newLikers.addAll(toUpdate.getLikers());
                    newLikers.remove(userId);
                    toUpdate.setLikers(newLikers);
                    postDao.updatePost(toUpdate);
                }
            });
        } else {
            return NoInternetExpcetionTask();
        }

    }


    @Override
    public Task<List<User>> getLikers(String postId) {

        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.getLikers(postId).addOnCompleteListener(v -> {
                if (v.isSuccessful()) {
                    userDao.insertAllUsers(v.getResult());
                }
            });
        } else {
            TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
            List<User> users = new ArrayList<>();
            Post p = postDao.getPostFromPostId(postId);
            if (p != null) {
                for (String userId : p.getLikers()) {
                    User user = userDao.getUserFromUserId(userId);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
            source.setResult(users);
            return source.getTask();
        }

    }


    @Override
    public Task<List<Post>> getPostsByUserId(String userId) {
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.getPostsByUserId(userId).addOnCompleteListener( v -> {
                if (v.isSuccessful()) {
                    postDao.insertPostList(v.getResult());
                }
            });
        } else {
            TaskCompletionSource<List<Post>> source = new TaskCompletionSource<>();
            List<Post> posts = localDatabase.userWithPostsDao().getPostsFromUserId(userId);
            source.setResult(posts);
            return source.getTask();
        }


    }


    @Override
    public Task<List<Post>> getNearbyPosts(double latitude, double longitude, double distance) {

        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            List<Post> posts = new ArrayList<>();
            return database.getNearbyPosts(latitude, longitude, distance).addOnCompleteListener( v -> {
                if (v.isSuccessful()) {
                    postDao.insertPostList(v.getResult());
                }
            });
        } else {
            TaskCompletionSource<List<Post>> source = new TaskCompletionSource<>();
            List<Post> nearbyPosts = new ArrayList<>();
            List<Post> posts = postDao.getPostList();
            Date now = new Date();
            for (Post p : posts) {
                long numDays = TimeUnit.DAYS.convert(Math.abs(now.getTime() - p.getDate().getTime()), TimeUnit.MILLISECONDS);
                //TODO Move the nearby function into an appropriate location other then FirestoreDB
                if (FirestoreDatabase.nearby(latitude, longitude, p.getLatitude(), p.getLongitude(), distance, p.getLikers().size(), numDays)) {
                    nearbyPosts.add(p);
                }
            }
            source.setResult(nearbyPosts);
            return source.getTask();
        }

    }

    @Override
    public Task<Post> getPostByPostId(String postId) {
        Post p;
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.getPostByPostId(postId).addOnCompleteListener( v -> {
                if (v.isSuccessful()) {
                    postDao.insertPost(v.getResult());
                }
            });
        } else {
            TaskCompletionSource<Post> source = new TaskCompletionSource<>();
            p = postDao.getPostFromPostId(postId);
            source.setResult(p);
            return source.getTask();
        }
    }

    //-----------------This part handles the local user-----------------------
    @Override
    public Task<Void> updateProfilePicture(String uri) {
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.updateProfilePicture(uri).addOnCompleteListener( v -> {
                if (v.isSuccessful()) {
                    LoggedInUser.getInstance().setImageURL(uri);
                    User user = internalStorage.getCurrentUser();
                    user.setImageURL(uri);
                    internalStorage.updateUser(user);
                    userDao.updateUser(user);
                }
            });
        } else {
            return NoInternetExpcetionTask();
        }

    }

    @Override
    public Task<Void> updateNickname(String nickname) {
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.updateNickname(nickname).addOnCompleteListener(v -> {
                if (v.isSuccessful()) {
                    LoggedInUser.getInstance().setNickname(nickname);
                    User user = internalStorage.getCurrentUser();
                    user.setNickname(nickname);
                    internalStorage.updateUser(user);
                    userDao.updateUser(user);
                }
            });
        } else {
           return NoInternetExpcetionTask();
        }
    }

    @Override
    public Task<Void> updateHighScore(int highScore) {
        if (DependencyManager.getNetworkStateSystem().isConnected()) {
            return database.updateHighScore(highScore).addOnCompleteListener(v -> {
                if (v.isSuccessful()) {
                    LoggedInUser.getInstance().setHighScore(highScore);
                    User user = internalStorage.getCurrentUser();
                    user.setHighScore(highScore);
                    internalStorage.updateUser(user);
                    userDao.updateUser(user);
                }
            });
        } else {
            return NoInternetExpcetionTask();
        }
    }


}

