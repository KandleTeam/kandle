package ch.epfl.sdp.kandle;


import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Post {

    //private User author;
    private String type;    //photo, texte, video
    private LatLng location;
    private int likes;
    private static int count = 0;
    private int postId = 0;
    // private String content;
    private String description;
    private ArrayList<String> comments;
    private Date date;


    public Post(String type, LatLng location, String description, Date date){
        this.type = type;
        this.location = location;
        this.likes = 0;
        postId = count++;
        this.description = description;
        this.comments = new ArrayList<>();
        this.date = date;

    }

    public Post(String type,String description,Date date){
        this.type = type;
        this.location = null;
        this.likes = 0;
        this.description = description;
        this.comments = null;
        this.date = date;
        postId = count ++;
    }


    public String getType(){
        return type;
    }

    public LatLng getLocation(){
        return location;
    }

    public int getLikes(){
        return likes;
    }

    public int getPost_id(){
        return postId;
    }

    public String getDescription(){
        return description;
    }

    public ArrayList<String> getComments(){
        return comments;
    }

    public Date getDate(){
        return date;
    }

    public int likePost(){
        return likes++;
    }

    public int dislikePost(){
        return likes--;
    }
}