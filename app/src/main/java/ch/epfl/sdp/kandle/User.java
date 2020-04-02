package ch.epfl.sdp.kandle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User implements Serializable {
    private ArrayList<String> postsIds;
    private String id, username, email, nickname, imageURL;


    public User() {
        // Keep fields null
    }

    public User(String id, String username, String email, String nickname, String imageURL) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.imageURL = imageURL;
        this.postsIds = new ArrayList<>();
        this.nickname = nickname;

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

    public List<String> getPosts(){
        return Collections.unmodifiableList(postsIds);
    }

    public void addPostId(String postId) {
        postsIds.add(postId);
    }

    public void removePostId(String postId) {
        postsIds.remove(postId);
    }
}