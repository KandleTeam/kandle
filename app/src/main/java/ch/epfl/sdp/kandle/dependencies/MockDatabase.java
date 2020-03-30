package ch.epfl.sdp.kandle.dependencies;

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

import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.User;

/**
 *  A mocked database. Upon creation, it contains:
 *  - a single user `admin`, with all-zero userID.
 *  - to be extended for posts, etc...
 */
public class MockDatabase implements Database {


    private class Follow {
        public List<String> following;

        public List<String> followers;

        public Follow(List<String> following, List<String> followers) {
            this.following = following;
            this.followers = followers;
        }

        public void addFollowing ( String s){
            following.add(s);
        }

        public void addFollower ( String s){
            followers.add(s);
        }

        public void removeFollowing ( String s){
            following.remove(s);
        }

        public void removeFollower ( String s){
            followers.remove(s);
        }
    }


    public static Map<String, User> users;
    private static Map<String, Follow> followMap;
    private static Map<String, Post> posts;

    public MockDatabase() {
        users = new HashMap<>();
        //String adminId = "user1Id"; // 28 zeros
        users.put("user1Id", new User("user1Id", "user1", "user1@kandle.ch", "Nickname", "image"));
        users.remove("user1Id");
        users.put("user1Id", new User("user1Id", "user1", "user1@kandle.ch", null,  "image"));
        users.remove("user1Id");
        users.put("user1Id", new User("user1Id", "user1", "user1@kandle.ch", null,  "image"));
        users.remove("user1Id");
        users.put("user1Id", new User("user1Id", "user1", "user1@kandle.ch", null,  "image"));
        users.remove("user1Id");

        users.put("user1Id", new User("user1Id", "user1", "user1@kandle.ch", null,  "image"));
        users.put("user2Id", new User("user2Id", "user2", "user2@kandle.ch", null,  "image"));
        users.put("user3Id", new User("user3Id", "user3", "user3@kandle.ch", null,  null));
        
        
        followMap = new HashMap<>();
        
        followMap.put("user1Id", new Follow( new LinkedList<>(Arrays.asList("user2Id")) , new LinkedList<>(Arrays.asList("user3Id"))));
        followMap.put("user2Id", new Follow(new LinkedList<>(Arrays.asList("user3Id")) , new LinkedList<>(Arrays.asList("user1Id"))));
        followMap.put("user3Id", new Follow(new LinkedList<>(Arrays.asList("user1Id")) , new LinkedList<>(Arrays.asList("user2Id"))));

        posts = new HashMap<>();

        posts.put("post1Id", new Post("Hello world !", "image", new Date(), "user1Id", "post1Id"));
        posts.put("post2Id", new Post("I'm user 1 !", null, new Date(), "user1Id", "post2Id"));
        users.get("user1Id").addPostId(posts.get("post1Id").getPostId());
        users.get("user1Id").addPostId(posts.get("post2Id").getPostId());

        posts.put("post3Id", new Post("I'm user 2 :)", null, new Date(), "user2Id", "post3Id"));
        users.get("user2Id").addPostId(posts.get("post3Id").getPostId());


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

        TaskCompletionSource<User> task = new TaskCompletionSource<>();

        if(users.containsKey(userId)) {
            task.setResult(users.get(userId));
        }
        //else {
           // task.setException(new IllegalArgumentException("No such user with id: " + userId));
        //}
        return task.getTask();
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
            task.setResult(null);
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
        User user = users.get("user1Id");
        user.setImageURL(uri);
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public Task<String> getProfilePicture() {

        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        User user = users.get("user1Id");
        source.setResult(user.getImageURL());
        return source.getTask();
    }

    @Override
    public Task<Void> updateNickname(String nickname) {
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        User user = users.get("user1Id");
        user.setFullname(nickname);
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public Task<String> getNickname() {
        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        User user = users.get("user1Id");
        source.setResult(user.getFullname());
        return source.getTask();
    }

    @Override
    public Task<String> getUsername() {
        TaskCompletionSource<String> source = new TaskCompletionSource<>();
        User user = users.get("user1Id");
        source.setResult(user.getUsername());
        return source.getTask();
    }

    @Override
    public Task<Void> addPost(String userId, Post p) {
        if(!users.get(userId).getPosts().contains(p.getPostId())) {
            posts.put(p.getPostId(), p);
            users.get(userId).addPostId(p.getPostId());
        }
        TaskCompletionSource<Void> source = new TaskCompletionSource<>();
        source.setResult(null);
        return source.getTask();
    }

    @Override
    public Task<Void> deletePost(String userId, Post p) {
        if(users.get(userId).getPosts().contains(p.getPostId())) {
            posts.remove(p.getPostId());
            users.get(userId).removePostId(p.getPostId());
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
