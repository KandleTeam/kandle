package ch.epfl.sdp.kandle;

import java.util.ArrayList;

public class User {

    private String id, username, email, fullname , normalizedUsername, imageURL;
    private ArrayList<String> postsIds;

    public User() {
        // Keep fields null
    }

    public User(String id, String username, String email, String imageURL) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.normalizedUsername = username.toLowerCase().replaceAll("[^a-z0-9]", "");
        this.imageURL = imageURL;
        this.postsIds = new ArrayList<>();
    }

    /*public String getFullname() {
        return fullname;
    }*/

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    /*
    public void setUsername(String username) {
        this.username = username;
        this.normalizedUsername = username.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
     */

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNormalizedUsername() {
        return normalizedUsername;
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