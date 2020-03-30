package ch.epfl.sdp.kandle;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Post {

    private LatLng location;
    private ArrayList<String> likers;
    private String userId;
    private String postId;
    private String imageURL;
    private String description;
    private ArrayList<String> comments;
    private Date date;
    private int likes;


   /* public Post(String type, LatLng location, int likes, String description, ArrayList<String> comments, Date date){
        this.type = type;
        this.location = location;
        this.likes = likes;
        postId = count++;
        this.description = description;
        this.comments = comments;
        this.date = date;
    }*/


   public Post(){

   }


    public Post(String description, String imageURL, Date date, String userId) {
        this.location = null;
        this.description = description;
        this.comments = null;
        this.date = date;
        this.likers = new ArrayList<>();
        this.likes=0;
        this.postId = UUID.randomUUID().toString();
        this.userId = userId;
        this.imageURL = imageURL;

    }

    //Useful for tests
    public Post(String description, String imageURL, Date date, String userId, String postId) {
        this.location = null;
        this.description = description;
        this.comments = null;
        this.date = date;
        this.likers = new ArrayList<>();
        this.likes=0;
        this.postId = postId;
        this.userId = userId;
        this.imageURL = imageURL;
    }




    /*
    public LatLng getLocation() {
        return location;
    }
     */

    public List<String> getLikers(){
        return Collections.unmodifiableList(likers);
    }

    public int getLikes() {
        return likers.size();
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    /*
    public ArrayList<String> getComments() {
        return comments;
    }
     */

    public Date getDate() {
        return (Date) date.clone();
    }

    public void likePost(String userId) {
        likers.add(userId);
    }
    public void unlikePost(String userId) {
        likers.remove(userId);
    }

    /*

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }*/

    public String getImageURL(){
        return imageURL;
    }

    public void setImageURL(String imageURL){
        this.imageURL = imageURL;
    }
}