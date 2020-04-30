package ch.epfl.sdp.kandle.Storage.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.epfl.sdp.kandle.Post;


@Dao
public interface PostDao {

    String POSTS_TABLE_NAME = "Posts";
    String POST_ATTR_USER_ID = "userId";
    String POST_ATTR_LATITUDE = "latitude";
    String POST_ATTR_LONGITUDE = "longitude";
    String POST_ATTR_LIKERS_LIST = "likers";
    String POST_ATTR_IMAGE_URL = "imageURL";
    String POST_ATTR_DESCRIPTION = "description";
    String POST_ATTR_EDITABLE = "editable";
    String POST_ATTR_DATE = "date";


    @Query("SELECT * from Posts")
    List<Post> getPostList();

    @Insert
    void insertPost(Post post);

    @Update
    void updatePost(Post post);

    @Delete
    void deletePost(Post post);

    @Query("DELETE FROM Posts where date NOT IN (SELECT date FROM Posts ORDER BY date DESC LIMIT 50)")
    void storeOnly50Posts();

    @Query("SELECT * FROM Posts WHERE postId=:postId")
    Post getPostFromPostId(final String postId);


}
