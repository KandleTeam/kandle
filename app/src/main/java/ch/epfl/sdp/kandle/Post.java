package ch.epfl.sdp.kandle;


import android.content.Context;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class Post {

    //private User author;
    private String type;    //photo, texte, video
    private LatLng location;
    private ArrayList<String> likers;
    private int likes;
    private static int count = 0;
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

    public Post(String type, String description, Date date) {
        this.type = type;
        this.location = null;
        this.likes = 0;
        this.description = description;
        this.comments = null;
        this.date = date;
        this.likers = new ArrayList<>();
        this.postId = UUID.randomUUID().toString();
    }


    public String getString() {
        return type;
    }

    public LatLng getLocation() {
        return location;
    }

    public ArrayList<String> getLikers(){
        return likers;
    }

    public int getLikes() {
        return likes;
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

    public int likePost() { return likes++; }

    public int dislikePost() {
        return likes--;
    }

    public Uri getImage(){
        return image;
    }

    public void setImage(Uri image){
        this.image = image;
    }
}