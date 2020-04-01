package ch.epfl.sdp.kandle.dependencies;

import com.google.android.gms.tasks.Task;

import java.util.List;

import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.User;

public interface Database {


    /**
     * Asynchronously retrieves a User from the Database from its unique username. If such a user does
     * not exist, the task returns null.
     * @param username the user's username
     * @return a Task for the resulting User.
     */
     Task<User> getUserByName(String username);

    /**
     * Asynchronously retrieves a User from the Database from its unique userId. If such a user does
     * not exist, the task fails.
     *      * @param userId the user's userId
     *      * @return
     */

    Task<User> getUserById(String userId);




    /**
     * Asynchronously attempts to create a User in the Database. The task can fail if:
     * - the client cannot access the database
     * - there is already a user with this userId
     * - there is already a user with this namedi
     * @param user the user to be created in the database
     * @return an empty Task, signifying the outcome
     */
    Task<Void> createUser(User user);

    Task<Integer> getNumberOfPosts(final String uid);

    Task<List<User>> searchUsers(final String prefix, int maxNumber);

    Task<Void> follow(final String userFollowing, final String userFollowed);

    Task<Void> unFollow(final String userUnFollowing, final String userUnFollowed);

    Task<List<String>> userIdFollowingList(String userId);

    Task<List<String>> userIdFollowersList(String userId);

    Task<List<User>> userFollowingList(String userId);

    Task<List<User>> userFollowersList(String userId);

    Task<Void> updateProfilePicture(String uri);

    Task<String> getProfilePicture();

    Task<Void> updateNickname(String nickname);

    Task<String> getNickname();

    Task<String> getUsername();

    Task<Void> addPost(String userId, Post p);

    Task<Void> deletePost(String userId, Post p);

    Task<Void> likePost(String userId, String postId);

    Task<Void> unlikePost(String userId, String postId);

    //Task<List<String>> likers(String postId);

    //Task<Void> editPost(Post p);

    Task<List<Post>> getPostsByUserId(String userId);

}
