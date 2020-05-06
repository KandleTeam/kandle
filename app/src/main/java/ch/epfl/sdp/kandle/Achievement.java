package ch.epfl.sdp.kandle;

import android.app.Activity;

import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.fragment.PostFragment;
import ch.epfl.sdp.kandle.fragment.ProfileFragment;

public class Achievement {

    Achievement_type type;
    int goal_value;
    private Authentication auth;
    private Database database;
    private AchievementAdapter achievementAdapter;
    private boolean state_achievement;
    private ProfileFragment fragment;

    public Achievement(Achievement_type type, int goal_value, AchievementAdapter adapter, ProfileFragment fragment) {
        this.type = type;
        this.goal_value = goal_value;
        this.achievementAdapter = adapter;
        state_achievement = false;
        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();
        this.fragment = fragment;
        if(auth.getCurrentUser() == null){
        }
    }

    public String getDescription() {
        return state_achievement ? "Achievement Completed !" : "Still Not Completed !";
    }

    public void setProfileFragment(ProfileFragment fragment){
        this.fragment = fragment;
    }

    public String getWayToComplete() {
        StringBuilder sb = new StringBuilder();
        sb.append("You need to ");
        switch (type) {
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

    public boolean checkAchievementState() {
        return state_achievement;
    }

    public void checkAchievement(boolean isAchievementFragment) {
            switch (type) {
                case FOLLOWERS:
                    checkFollowers(isAchievementFragment);
                    break;

                case FOLLOWING:
                    checkFollowing(isAchievementFragment);
                    break;

                case NB_POSTS:
                    checkPosts(isAchievementFragment);
                    break;

                case NB_LIKES_POST:
                    checkOnePostLikes(isAchievementFragment);
                    break;

                case NB_LIKES_POSTS_TOTAL:
                    checkPostsLikes(isAchievementFragment);
                    break;
            }
    }

    public void checkFollowers(boolean isAchievementFragment) {
        database.userIdFollowersList(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult() == null){
                }
                if (task.getResult().size() >= goal_value) {
                    state_achievement = true;
                    if(isAchievementFragment){
                        achievementAdapter.notifyChange();
                    }
                    else{
                        fragment.notifyChange();
                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkFollowing(boolean isAchievementFragment) {
        database.userIdFollowingList(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() >= goal_value) {
                    state_achievement = true;
                    if(isAchievementFragment){
                        achievementAdapter.notifyChange();
                    }
                    else{
                        fragment.notifyChange();
                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkPosts(boolean isAchievementFragment) {
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() >= goal_value) {
                    state_achievement = true;
                    if(isAchievementFragment){
                        achievementAdapter.notifyChange();
                    }
                    else{
                        fragment.notifyChange();
                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkPostsLikes(boolean isAchievementFragment) {
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int number_likes = 0;
                for (int i = 0; i < task.getResult().size(); i++) {
                    number_likes += task.getResult().get(i).getLikes();
                }
                if (number_likes >= goal_value) {
                    state_achievement = true;
                    if(isAchievementFragment){
                        achievementAdapter.notifyChange();
                    }
                    else{
                        fragment.notifyChange();
                    }
                }
            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkOnePostLikes(boolean isAchievementFragment) {
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (int i = 0; i < task.getResult().size(); i++) {
                    if (task.getResult().get(i).getLikes() >= goal_value) {
                        state_achievement = true;
                        i = task.getResult().size();
                        if(isAchievementFragment){
                            achievementAdapter.notifyChange();
                        }
                        else{
                            fragment.notifyChange();
                        }

                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    //the number is the number of followers, followings, posts, likes achieved in one posts or in total in order to succeed one achievement
    public enum Achievement_type {
        FOLLOWERS,
        FOLLOWING,
        NB_POSTS,
        NB_LIKES_POST,
        NB_LIKES_POSTS_TOTAL
    }
}
