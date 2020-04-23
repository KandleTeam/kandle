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

import ch.epfl.sdp.kandle.Storage.room.Converters;

@Entity(tableName = "Users")
public class User implements Serializable {

    @NonNull
    @PrimaryKey(autoGenerate = false)
    private String id;
    @ColumnInfo(name = "username")
    private String username;
    @ColumnInfo(name = "email")
    private String email;
    @ColumnInfo(name = "nickname")
    private String nickname;
    @ColumnInfo(name = "imageURL")
    private String imageURL;


    @ColumnInfo(name = "postIds")
    @TypeConverters(Converters.class)
    private List<String> postsIds;


    @Ignore
    public User() {
        // Keep fields null
    }

    public User(String id, String username, String email, String nickname, String imageURL) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.imageURL = imageURL;
        this.nickname = nickname;
        postsIds = new ArrayList<>();

    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

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

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageURL() {
        return imageURL;
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



}