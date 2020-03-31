package ch.epfl.sdp.kandle.dependencies;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.User;

/**
 *  A mocked database. Upon creation, it contains:
 *  - a single user `admin`, with all-zero userID.
 *  - to be extended for posts, etc...
 */
public class MockDatabase implements Database {


    private  Map<String, User> users;
    private  Map<String, Follow> followMap;
    private  Map<String, Post> posts;

    public MockDatabase(boolean isConnected,Map<String, User> users, Map<String, Follow> followMap, Map<String, Post> posts) {
        this.users = users;
        this.posts = posts;
        this.followMap = followMap;
        if(isConnected){
            users.put(LoggedInUser.getInstance().getId(),LoggedInUser.getInstance());
            followMap.put(LoggedInUser.getInstance().getId(),new Follow());
        }

/*
        users.put("user1Id", new User("user1Id", "user1", "user1@kandle.ch", null,  null));
        users.put("loggedInUserId",new User("loggedInUserId","LoggedInUser","loggedInUser@kandle.ch","nickname","image"));

        followMap = new HashMap<>();
        followMap.put("user1Id", new Follow( new LinkedList<>(Arrays.asList("loggedInUserId")) , new LinkedList<>(Arrays.asList("loggedInUserId"))));
        followMap.put("loggedInUserId", new Follow( new LinkedList<>(Arrays.asList("user1Id")) , new LinkedList<>(Arrays.asList("user1Id"))));

        posts = new HashMap<>();
        posts.put("post1Id", new Post("text", "Hello world !", new Date(), "loggedInUserId", "post1Id"));
        posts.put("post2Id", new Post("text", "I'm user 1 !", new Date(), "loggedInUserId", "post2Id"));
        posts.get("post1Id").setImage("image");
        users.get("loggedInUserId").addPostId(posts.get("post1Id").getPostId());
        users.get("loggedInUserId").addPostId(posts.get("post2Id").getPostId());
*/


    }


    @Override
    public Task<User> getUserByName(String username) {

        TaskCompletionSource<User> task = new TaskCompletionSource<>();
        User result = null;

        for (User user : users.values()) {
            if (user.getUsername().equals(username)) {
                result = user;
                break;
            }
        }
        task.setResult(result);
        return task.getTask();

    }




    @Override
    public Task<User> getUserById(String userId) {

        TaskCompletionSource<User> source = new TaskCompletionSource<>();

        if(users.containsKey(userId)) {
            source.setResult(users.get(userId));
        } else {
            source.setException(new IllegalArgumentException("No such user with id: " + userId + "with users containing"));
        }

        return source.getTask();
    }



    @Override
    public Task<Void> createUser(User user) {

        TaskCompletionSource<Void> task = new TaskCompletionSource<>();

       /* if(users.containsKey(user.getId())) {
            task.setException(new IllegalArgumentException("User with this id already exists"));
        } else if(findUserByName(user.getUsername()).isPresent()) {
            task.setException(new IllegalArgumentException("User with this username already exists"));
        } else {


        */
            users.put(user.getId(), user);
            followMap.put(user.getId(), new Follow());
            //task.setResult(null);
        //}
        return task.getTask();
    }

    @Override
    public Task<List<User>> searchUsers(String prefix, int maxNumber) {
        List<User> results = new ArrayList<>();

        for(User u : users.values()) {
            if(u.getUsername().startsWith(prefix)) {
                results.add(u);
            }
        }
/*
        results.sort(new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                return u1.getUsername().compareTo(u2.getUsername());
            }
        });

 */
        Collections.sort(results, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });

        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
        source.setResult(new ArrayList<User>(results.subList(0, Math.min(maxNumber, results.size()))));
        return source.getTask();

    }

    @Override
    public Task<Void> follow(String userFollowing, String userFollowed) {

        Follow follow = followMap.get(userFollowing);
        Follow follow2 = followMap.get(userFollowed);

        if ( !follow.following.contains(userFollowed)) {
            follow.addFollowing(userFollowed);
            follow2.addFollower(userFollowing);
            followMap.put(userFollowing, follow);
            followMap.put(userFollowed, follow2);

        }

        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public Task<Void> unFollow(String userUnFollowing, String userUnFollowed) {

        Follow follow = followMap.get(userUnFollowing);
        Follow follow2 = followMap.get(userUnFollowed);

        if ( follow.following.contains(userUnFollowed)) {
            follow.removeFollowing(userUnFollowed);
            follow2.removeFollower(userUnFollowing);
            followMap.put(userUnFollowing, follow);
            followMap.put(userUnFollowed, follow2);

        }

        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public Task<List<String>> userIdFollowingList(String userId) {
        TaskCompletionSource<List<String>> source = new TaskCompletionSource<>();
        source.setResult(new ArrayList<String>(followMap.get(userId).following));
        return source.getTask();

    }

    @Override
    public Task<List<String>> userIdFollowersList(String userId) {
        TaskCompletionSource<List<String>> source = new TaskCompletionSource<>();
        source.setResult(new ArrayList<String>(followMap.get(userId).followers));
        return source.getTask();
    }

    @Override
    public Task<List<User>> userFollowingList(String userId) {
        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
        ArrayList<User> following = new ArrayList<>();

        for (String id : followMap.get(userId).following){
            following.add(users.get(id));
        }
        source.setResult(following);
        return source.getTask();
    }

    @Override
    public Task<List<User>> userFollowersList(String userId) {
        TaskCompletionSource<List<User>> source = new TaskCompletionSource<>();
        ArrayList<User> followers = new ArrayList<>();

        for (String id : followMap.get(userId).followers){
            followers.add(users.get(id));
        }
        source.setResult(followers);
        return source.getTask();
    }

    @Override
    public Task<Void> updateProfilePicture(String uri) {
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        User user = users.get(LoggedInUser.getInstance().getId());
        user.setImageURL(uri);

        return source.getTask();
    }

    @Override
    public Task<String> getProfilePicture() {

        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        User user = users.get(LoggedInUser.getInstance().getId());
        source.setResult(user.getImageURL());
        return source.getTask();
    }

    @Override
    public Task<Void> updateNickname(String nickname) {
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        User user = users.get(LoggedInUser.getInstance().getId());
        user.setNickname(nickname);
        return source.getTask();
    }

    @Override
    public Task<String> getNickname() {
        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        User user = users.get(LoggedInUser.getInstance().getId());
        source.setResult(user.getNickname());
        return source.getTask();
    }

    @Override
    public Task<String> getUsername() {
        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        User user = users.get(LoggedInUser.getInstance().getId());
        source.setResult(user.getUsername());
        return source.getTask();
    }

    @Override
    public Task<Void> addPost( Post p) {
        if(!users.get(p.getUserId()).getPosts().contains(p.getPostId())) {
            posts.put(p.getPostId(), p);
            users.get(p.getUserId()).addPostId(p.getPostId());
        }
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public Task<Void> deletePost(Post p) {
        if(users.get(p.getUserId()).getPosts().contains(p.getPostId())) {
            posts.remove(p.getPostId());
            users.get(p.getUserId()).removePostId(p.getPostId());
        }
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public Task<Void> likePost(String userId, String postId) {
        if(!posts.get(postId).getLikers().contains(userId)) {
            posts.get(postId).likePost(userId);
        }
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public Task<Void> unlikePost(String userId, String postId) {
        if(posts.get(postId).getLikers().contains(userId)) {
            posts.get(postId).unlikePost(userId);
        }
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();
    }

    /*
    @Override
    public Task<List<String>> likers(String postId) {
        TaskCompletionSource<List<String>> source = new TaskCompletionSource<>();
        source.setResult(new ArrayList<String>(posts.get(postId).getLikers()));
        return source.getTask();
    }
     */

    @Override
    public Task<List<Post>> getPostsByUserId(String userId) {
        List<String> userPostsIds = users.get(userId).getPosts();
        List<Post> postsList = new ArrayList<Post>();
        for (Map.Entry<String,Post> entry : posts.entrySet()){
            if (userPostsIds.contains(entry.getValue().getPostId())){
                postsList.add(entry.getValue());
            }
        }
        TaskCompletionSource<List<Post>> source = new TaskCompletionSource<>();
        source.setResult(postsList);
        return source.getTask();
    }


}
