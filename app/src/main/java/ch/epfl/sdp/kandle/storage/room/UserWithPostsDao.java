package ch.epfl.sdp.kandle.storage.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ch.epfl.sdp.kandle.Post;

@Dao
public interface UserWithPostsDao {
    @Transaction
    @Query("SELECT * FROM Users")
    List<UserWithPosts> getAllUserWithPosts();

    @Transaction
    @Query("SELECT * FROM Posts WHERE userId=:userId")
    List<Post> getPostsFromUserId(final String userId);
}
