package ch.epfl.sdp.kandle.Storage.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.User;


@Dao
public interface PostDao {
    @Query("SELECT * from Posts")
    List<Post> getPostList();

    @Insert
    void insertPost(Post post);

    @Update
    void updatePost(Post post);

    @Delete
    void deletePost(Post post);

    @Query("DELETE FROM Posts where date NOT IN (SELECT date from Posts ORDER BY date DESC LIMIT 50)")
    void storeOnly50Posts();

    @Query("SELECT * FROM Posts WHERE postId=:postId")
    Post getPostFromPostId(final String postId);


}
