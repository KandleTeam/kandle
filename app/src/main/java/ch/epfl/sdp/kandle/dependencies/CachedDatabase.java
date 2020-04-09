package ch.epfl.sdp.kandle.dependencies;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.User;

public class CachedDatabase implements Database {

    private Database database = DependencyManager.getDatabaseSystem();
    private InternalStorage internalStorage = DependencyManager.getInternalStorageSystem();

    public CachedDatabase() {

    }

    @Override
    public Task<User> getUserByName(String username) {
        return database.getUserByName(username);
    }

    //TODO implement a getUserbyId for the local cache only

    @Override
    public Task<User> getUserById(String userId) {
        /* To put into Firestoredatabase such that mocking is easier
        TaskCompletionSource<User> source = new TaskCompletionSource();
        if(NetworkStatus.isConnected()) {
            return database.getUserById(userId);
        } else {
            System.out.println("Throw no internet exception");
            source.setException(new NoInternetException());
            return source.getTask();
        }
        */
        return database.getUserById(userId);
    }

    @Override
    public Task<Void> createUser(User user) {
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
    public Task<Void> addPost(Post p) {
        return database.addPost(p);
    }

    @Override
    public Task<Void> deletePost(Post p) {
        return database.deletePost(p);
    }

    @Override
    public Task<Void> likePost(String userId, String postId) {
        return database.likePost(userId, postId);
    }

    @Override
    public Task<Void> unlikePost(String userId, String postId) {
        return database.unlikePost(userId, postId);
    }

    @Override
    public Task<List<Post>> getPostsByUserId(String userId) {
        return database.getPostsByUserId(userId);
    }

    //-----------------This part handles the local user-----------------------
    //TODO do the databse operation async such that we dont wait for the update in the database to progress
    @Override
    public Task<Void> updateProfilePicture(String uri) {
        LoggedInUser.getInstance().setImageURL(uri);
        User user = internalStorage.getCurrentUser();
        user.setImageURL(uri);
        internalStorage.updateUser(user);
        return database.updateProfilePicture(uri);
    }

    @Override
    public Task<String> getProfilePicture() {
        User user = LoggedInUser.getInstance();
        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        if (user != null) {
            source.setResult(user.getImageURL());
        } else {
            user = internalStorage.getCurrentUser();
            if (user != null) {
                source.setResult(user.getImageURL());
            } else {
                return database.getProfilePicture();
            }
        }
        return source.getTask();
    }

    @Override
    public Task<Void> updateNickname(String nickname) {
        LoggedInUser.getInstance().setNickname(nickname);
        User user = internalStorage.getCurrentUser();
        user.setNickname(nickname);
        internalStorage.updateUser(user);
        return database.updateNickname(nickname);
    }

    @Override
    public Task<String> getNickname() {
        User user = LoggedInUser.getInstance();
        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        if (user != null) {
            source.setResult(user.getNickname());
        } else {
            user = internalStorage.getCurrentUser();
            if (user != null) {
                source.setResult(user.getNickname());
            } else {
                return database.getNickname();
            }
        }
        return source.getTask();
    }

    @Override
    public Task<String> getUsername() {
        User user = LoggedInUser.getInstance();
        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        if (user != null) {
            source.setResult(user.getUsername());
        } else {
            user = internalStorage.getCurrentUser();
            if (user != null) {
                source.setResult(user.getUsername());
            } else {
                return database.getUsername();
            }
        }
        return source.getTask();
    }


}

