package ch.epfl.sdp.kandle.storage.room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;
import ch.epfl.sdp.kandle.entities.post.Post;

@Dao
public interface UserWithPostsDao {
    @Transaction
    @Query("SELECT * FROM Users")
    List<UserWithPosts> getAllUserWithPosts();

    @Transaction
    @Query("SELECT * FROM Posts WHERE userId=:userId")
    List<Post> getPostsFromUserId(final String userId);
}
