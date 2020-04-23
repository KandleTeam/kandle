package ch.epfl.sdp.kandle;

import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.List;

import ch.epfl.sdp.kandle.caching.CachedDatabase;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class Achievement {

    Achievement_type type;
    int goal_value;
    private Authentication auth;
    private Database database;
    private AchievementAdapter achievementAdapter;
    private boolean state_achievement;

    public Achievement(Achievement_type type, int goal_value, AchievementAdapter adapter){
        this.type = type;
        this.goal_value = goal_value;
        this.achievementAdapter = adapter;
        state_achievement = false;
        auth = DependencyManager.getAuthSystem();
        database = new CachedDatabase();
    }

    public String getDescription() {
        return state_achievement ? "Achievement Completed !" : "Still Not Completed !";
    }

    public String getWayToComplete() {
        StringBuilder sb = new StringBuilder();
        sb.append("You need to ");
        switch (type){
            case FOLLOWERS:
                return sb.append("have ").append(goal_value).append(" followers").toString();

            case FOLLOWING:
                return sb.append("follow ").append(goal_value).append(" people").toString();

            case NB_POSTS:
                return sb.append("make ").append(goal_value).append(" posts").toString();

            case NB_LIKES_POST:
                return sb.append("have a post with ").append(goal_value).append(" likes").toString();

            case NB_LIKES_POSTS_TOTAL:
                return sb.append("have in total ").append(goal_value).append(" likes in your posts").toString();
                //THIS CASE IS NEVER REACHED
            default:
                return sb.toString();
        }
    }

    //the number is the number of followers, followings, posts, likes achieved in one posts or in total in order to succeed one achievement
    public enum Achievement_type {
        FOLLOWERS,
        FOLLOWING,
        NB_POSTS,
        NB_LIKES_POST,
        NB_LIKES_POSTS_TOTAL}

    public boolean checkAchievementState(){
        return state_achievement;
    }



    public void checkAchievement(){
        if(!state_achievement){
            switch (type){
                case FOLLOWERS:
                    checkFollowers();
                break;

                case FOLLOWING:
                    checkFollowing();
                break;

                case NB_POSTS:
                    checkPosts();
                break;

                case NB_LIKES_POST:
                    checkOnePostLikes();
                break;

                case NB_LIKES_POSTS_TOTAL:
                    checkPostsLikes();
                break;
            }
        }
    }

    public void checkFollowers(){
        database.userIdFollowersList(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().size() >= goal_value){
                    System.out.println(goal_value);
                    state_achievement = true;
                    achievementAdapter.notifyChange();
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkFollowing(){
        database.userIdFollowingList(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().size() >= goal_value){
                    state_achievement = true;
                    achievementAdapter.notifyChange();
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkPosts(){
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().size() >= goal_value){
                    state_achievement = true;
                    achievementAdapter.notifyChange();
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkPostsLikes(){
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int number_likes = 0;
                for(int i = 0; i < task.getResult().size(); i++){
                    number_likes += task.getResult().get(i).getLikes();
                }
                if(number_likes >= goal_value) {
                    state_achievement = true;
                    achievementAdapter.notifyChange();
                }
            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkOnePostLikes(){
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for(int i = 0; i < task.getResult().size(); i++){
                    if(task.getResult().get(i).getLikes() >= goal_value){
                        state_achievement = true;
                        i = task.getResult().size();
                        achievementAdapter.notifyChange();
                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }
}
