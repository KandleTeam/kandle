package ch.epfl.sdp.kandle;


import android.content.Context;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class Post {

    private String type;    //photo, texte, video
    private LatLng location;
    private  int likes;
    private ArrayList<String> likers;
    private String userId;
    private String postId;
    private Uri image;
    private String description;
    private ArrayList<String> comments;
    private Date date;


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

    public Post(String type, String description, Date date, String userId) {
        this.type = type;
        this.location = null;
        this.description = description;
        this.comments = null;
        this.date = date;
        this.likers = new ArrayList<>();
        this.likes=0;
        this.postId = UUID.randomUUID().toString();
        this.userId = userId;
    }


    public String getType() {
        return type;
    }

    public LatLng getLocation() {
        return location;
    }

    public ArrayList<String> getLikers(){
        return likers;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getLikes() {
        return likers.size();
    }

    public String getPostId() {
        return postId;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public Date getDate() {
        return date;
    }

    public void likePost() {
        likers.add(userId);
    }
    public void unlikePost() {
        likers.remove(userId);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Uri getImage(){
        return image;
    }

    public void setImage(Uri image){
        this.image = image;
    }
}