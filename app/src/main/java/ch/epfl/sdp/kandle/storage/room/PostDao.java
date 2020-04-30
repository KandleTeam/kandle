package ch.epfl.sdp.kandle.storage.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPost(Post post);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPostList(List<Post> posts);

    @Update
    void updatePost(Post post);

    @Delete
    void deletePost(Post post);

    @Query("DELETE FROM Posts WHERE date NOT IN (SELECT date FROM Posts ORDER BY date DESC LIMIT 50)")
    void storeOnly50Posts();

    @Query("SELECT * FROM Posts WHERE postId=:postId")
    Post getPostFromPostId(final String postId);


}
