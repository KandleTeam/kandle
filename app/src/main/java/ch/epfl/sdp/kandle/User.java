package ch.epfl.sdp.kandle;

import java.io.Serializable;

public class User implements Serializable {
    private ArrayList<String> postsIds;
    private String id, username, email, fullname , imageURL;


    public User() {
        // Keep fields null
    }

    public User(String id, String username, String email, String fullname, String imageURL) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.imageURL = imageURL;
        this.postsIds = new ArrayList<>();
        this.fullname = fullname;

    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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

    public ArrayList<String> getPosts(){
        return postsIds;
    }

    public void addPostId(String postId){
        postsIds.add(postId);
    }

    public void removePostId(String postId){
        postsIds.remove(postId);
    }
}