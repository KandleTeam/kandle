package ch.epfl.sdp.kandle.Storage.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.epfl.sdp.kandle.User;

@Dao
public interface UserDao {

    String USER_TABLE_NAME = "Users";
    String USER_ATTR_USERNAME = "username";
    String USER_ATTR_EMAIL = "email";
    String USER_ATTR_NICKNAME = "nickname";
    String USER_ATTR_IMAGE_URL = "imageURL";
    String USER_ATTR_POSTS_LIST = "postIds";

    @Query("SELECT * from Users")
    List<User> getUserList();

    @Insert
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("DELETE FROM Users WHERE id NOT IN (SELECT id FROM Users ORDER BY id DESC LIMIT 50)")
    void storeOnly50Users();

    @Query("SELECT * FROM Users WHERE id=:userId")
    User getUserFromUserId(final String userId);

    @Query("SELECT * FROM Users WHERE username=:username")
    User getUserFromUsername(final String username);
}
