package ch.epfl.sdp.kandle;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.kandle.storage.room.Converters;

import static ch.epfl.sdp.kandle.storage.room.UserDao.USER_ATTR_EMAIL;
import static ch.epfl.sdp.kandle.storage.room.UserDao.USER_ATTR_HIGH_SCORE;
import static ch.epfl.sdp.kandle.storage.room.UserDao.USER_ATTR_IMAGE_URL;
import static ch.epfl.sdp.kandle.storage.room.UserDao.USER_ATTR_NICKNAME;
import static ch.epfl.sdp.kandle.storage.room.UserDao.USER_ATTR_POSTS_LIST;
import static ch.epfl.sdp.kandle.storage.room.UserDao.USER_ATTR_USERNAME;
import static ch.epfl.sdp.kandle.storage.room.UserDao.USER_TABLE_NAME;

@Entity(tableName = USER_TABLE_NAME)
public class User implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    @ColumnInfo(name = USER_ATTR_USERNAME)
    private String username;
    @ColumnInfo(name = USER_ATTR_EMAIL)
    private String email;
    @ColumnInfo(name = USER_ATTR_NICKNAME)
    private String nickname;
    @ColumnInfo(name = USER_ATTR_IMAGE_URL)
    private String imageURL;
    @ColumnInfo(name = USER_ATTR_POSTS_LIST)
    @TypeConverters(Converters.class)
    private List<String> postsIds;
    @ColumnInfo(name = USER_ATTR_HIGH_SCORE)
    private int highScore;


    @Ignore
    public User() {
        // Keep fields null
    }

    public User(@NonNull String id, String username, String email, String nickname, String imageURL) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.imageURL = imageURL;
        this.nickname = nickname;
        postsIds = new ArrayList<>();
        this.highScore = 0;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public List<String> getPostsIds() {
        return Collections.unmodifiableList(postsIds);
    }

    public void setPostsIds(List<String> postsIds) {
        this.postsIds = postsIds;
    }

    public void addPostId(String postId) {
        postsIds.add(postId);
    }

    public void removePostId(String postId) {
        postsIds.remove(postId);
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }


}