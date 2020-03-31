package ch.epfl.sdp.kandle.dependencies;

import java.util.LinkedList;
import java.util.List;

public class Follow {
    public List<String> following;

    public List<String> followers;


    public Follow(List<String> following, List<String> followers) {
        this.following = following;
        this.followers = followers;
    }

    public Follow(){
        this.followers = new LinkedList<String>();
        this.following = new LinkedList<String>();
    }


    public void addFollowing ( String s){
        following.add(s);
    }

    public void addFollower ( String s){
        followers.add(s);
    }

    public void removeFollowing ( String s){
        following.remove(s);
    }

    public void removeFollower ( String s){
        followers.remove(s);
    }
}
