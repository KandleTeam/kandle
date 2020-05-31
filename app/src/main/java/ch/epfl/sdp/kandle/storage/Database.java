package ch.epfl.sdp.kandle.storage;

import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Map;

import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.user.User;

public interface Database {

    /**
     * Asynchronously retrieves a User from the Database from its unique username. If such a user does
     * not exist, the task returns null.
     *
     * @param username the user's username
     * @return a Task for the resulting User.
     */
    Task<User> getUserByName(String username);

    /**
     * Asynchronously retrieves a User from the Database from its unique userId. If such a user does
     * not exist, the task fails.
     * * @param userId the user's userId
     * * @return
     */

    Task<User> getUserById(String userId);

    /**
     * Asynchronously attempts to create a User in the Database. The task can fail if:
     * - the client cannot access the database
     * - there is already a user with this userId
     * - there is already a user with this namedi
     *
     * @param user the user to be created in the database
     * @return an empty Task, signifying the outcome
     */
    Task<Void> createUser(User user, Map<String, Object> usernameMap, Map<String, Object> deviceMap);


    Task<Void> createUser(User user);
    /**
     * Asynchronously retrieves maxNumber of users that have a username matching prefix
     *
     * @param prefix
     * @param maxNumber
     * @return A list of user of at most maxNUmber length that have their usernames matching the prefix
     */
    Task<List<User>> searchUsers(final String prefix, int maxNumber);

    /**
     * Asynchronously retrieves maxNumber of users that have a username matching prefix
     *
     * @return A list of user of at most maxNUmber length that have their usernames matching the prefix
     */
    Task<List<User>> usersList();

    /**
     * Asynchronously adds the userFollowing to the follow list of the userFollowed
     *
     * @param userFollowing
     * @param userFollowed
     * @return A void task
     */
    Task<Void> follow(final String userFollowing, final String userFollowed);

    /**
     * Asynchronously removes the userUnFollowing from the follow list of the userUnFollowed
     *
     * @param userUnFollowing
     * @param userUnFollowed
     * @return A void task
     */
    Task<Void> unFollow(final String userUnFollowing, final String userUnFollowed);

    /**
     * Asynchronously sets a UserFollowing as a Close UserFollowing
     *
     * @param userFollowing
     * @param userFollowed
     * @return A void task
     */
    Task<Void> setCloseFollower(final String userFollowing, final String userFollowed);

    /**
     * Asynchronously unsets a UserFollowing as a Close UserFollowing
     *
     * @param userFollowing
     * @param userFollowed
     * @return A void task
     */
    Task<Void> unsetCloseFollower(final String userFollowing, final String userFollowed);

    /**
     * Asynchronously retrieves all the userIds that the target user is following
     *
     * @param userId The id of the target user
     * @return A list of userIds that the target user follows
     */
    Task<List<String>> userIdFollowingList(String userId);

    /**
     * Asynchronously retrieves all the user ids that are following the target user
     *
     * @param userId The id of the target user
     * @return A list of userIds that are following the target user
     */
    Task<List<String>> userIdFollowersList(String userId);

    /**
     * Asynchronously retrieves all the users uds that are close followers to the target
     *
     * @param userId The id of the target user
     * @return A list of user that the target user follows
     */
    Task<List<String>> userIdCloseFollowersList(String userId);

    /**
     * Asynchronously retrieves all the users that the target user is following
     *
     * @param userId The id of the target user
     * @return A list of user that the target user follows
     */
    Task<List<User>> userFollowingList(String userId);

    /**
     * Asynchronously retrieves all the users that are following the target user
     *
     * @param userId The id of the target user
     * @return A list of user that are following the target user
     */
    Task<List<User>> userFollowersList(String userId);

    /**
     * Asynchronously retrieves all the users that are following the target user
     *
     * @param userId The id of the target user
     * @return A list of user that are following the target user
     */
    Task<List<User>> userCloseFollowersList(String userId);

    /**
     * Asynchronously updates the current users profil picture URI
     *
     * @param uri
     * @return A void task
     */
    Task<Void> updateProfilePicture(String uri);

    /**
     * Asynchronously updates the current users nickname
     *
     * @param nickname
     * @return A void task
     */
    Task<Void> updateNickname(String nickname);

    /**
     * Asynchronously adds a new post to the database
     *
     * @param p The post to add
     * @return A void task
     */
    Task<Void> addPost(Post p);

    /**
     * Asynchronously edits a already present post in the database
     *
     * @param p      The updated post
     * @param postId The Id of the post to update in the database
     * @return A void task
     */
    Task<Void> editPost(Post p, String postId);

    /**
     * Asynchronously deletes a post in the database
     *
     * @param p The post to delete in the database
     * @return A void task
     */
    Task<Void> deletePost(Post p);

    /**
     * Asynchronously likes a post with the userId
     *
     * @param userId The user that liked the post
     * @param postId The post that has been liked
     * @return A void task
     */
    Task<Void> likePost(String userId, String postId);

    /**
     * Asynchronously unlikes a post with the userId
     *
     * @param userId The user that unliked the post
     * @param postId The post that has been unliked
     * @return A void task
     */
    Task<Void> unlikePost(String userId, String postId);

    /**
     * Asynchronously retrieves all the users that liked the given post
     *
     * @param postId The id of the concerned post
     * @return A list of user that liked the post with the corresponding postId
     */
    Task<List<User>> getLikers(String postId);

    /**
     * Asynchronously retrives all the posts that a user has made
     *
     * @param userId
     * @return A list of post that were created by the user with the corresponding userId
     */
    Task<List<Post>> getPostsByUserId(String userId);

    /**
     * Asynchronously retrives all the nearby post
     *
     * @param latitude  The latitude of the zone center
     * @param longitude The longitude of the zone center
     * @param distance  The radius of the zone in which the post are retrieved
     * @return A list of nearby post
     */
    Task<List<Post>> getNearbyPosts(double latitude, double longitude, double distance);

    /**
     * Asynchronously retrieves the post with the corresponding postId
     *
     * @param postId The id of the post to retrieve
     * @return A post with the corresponding Id
     */
    Task<Post> getPostByPostId(String postId);

    /**
     * Updates high score of the user in the offline game
     *
     * @param highScore the user's new high score
     * @return A void task
     */
    Task<Void> updateHighScore(int highScore);


}
