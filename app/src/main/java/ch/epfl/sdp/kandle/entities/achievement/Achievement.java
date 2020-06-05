package ch.epfl.sdp.kandle.entities.achievement;

import android.annotation.SuppressLint;
import android.util.Log;

import ch.epfl.sdp.kandle.authentification.Authentication;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.fragment.ProfileFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getAuthSystem;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getDatabaseSystem;

public class Achievement {

    private Achievement_type type;
    private int goalValue;
    private Authentication auth;
    private Database database;
    private AchievementAdapter achievementAdapter;
    private boolean state_achievement;
    private ProfileFragment fragment;

    public Achievement(Achievement_type type, int goalValue, AchievementAdapter adapter, ProfileFragment fragment) {
        this.type = type;
        this.goalValue = goalValue;
        this.achievementAdapter = adapter;
        state_achievement = false;
        auth = getAuthSystem();
        database = getDatabaseSystem();
        this.fragment = fragment;
        if(auth.getCurrentUser() == null){
        }
    }

    /**
     * Says if the achievement is completed
     * @return a string explaining if it is completed or not
     */
    public String getDescription() {
        return state_achievement ? "Achievement Completed !" : "Still Not Completed !";
    }

    /**
     * Changes the profile fragment that is linked with the achievement
     * @param fragment
     */
    public void setProfileFragment(ProfileFragment fragment) {
        this.fragment = fragment;
    }

    /**
     * This checks which is the way to complete an achievement
     * @return a string explaining how to complete an achievement
     */
    @SuppressLint("DefaultLocale")
    public String getWayToComplete() {
        switch (type) {
            case FOLLOWERS:
                return String.format("You need to have %d followers", goalValue);
            case FOLLOWING:
                return String.format("You need to follow %d people", goalValue);
            case NB_POSTS:
                return String.format("You need to make %d posts", goalValue);
            case NB_LIKES_POST:
                return String.format("You need to have a post with %d likes", goalValue);
            case NB_LIKES_POSTS_TOTAL:
                return String.format("You need to have in total with %d likes in a post", goalValue);
            case OFFLINE_GAME_POINTS:
                return String.format("You need to have in total %d points in the Offline Game", goalValue);
            //THIS CASE IS NEVER REACHED
            default:
                return String.format("");
        }
    }

    /**
     * checks if an achievement is completed
     * @return true if it is or false if it is not
     */
    public boolean checkAchievementState() {
        return state_achievement;
    }

    /**
     * checks if the achievement is completed or not by checking in the database
     * @param isAchievementFragment
     */
    public void checkAchievement(boolean isAchievementFragment) {
            if(state_achievement){
            }
            else {
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

                    case OFFLINE_GAME_POINTS:
                        checkPointsOfflineGame(isAchievementFragment);
                        break;

                    default:
                        break;
                }
            }
    }


    private void checkFollowers(boolean isAchievementFragment) {
        database.userIdFollowersList(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() >= goalValue) {
                    state_achievement = true;
                    if (isAchievementFragment) {
                        achievementAdapter.notifyDataSetChanged();
                    } else {
                        fragment.notifyChange();
                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    private void checkFollowing(boolean isAchievementFragment) {
        database.userIdFollowingList(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() >= goalValue) {
                    state_achievement = true;
                    if (isAchievementFragment) {
                        achievementAdapter.notifyDataSetChanged();
                    } else {
                        fragment.notifyChange();
                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    private void checkPosts(boolean isAchievementFragment) {
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                if (task.getResult().size() >= goalValue) {
                    state_achievement = true;
                    if (isAchievementFragment) {
                        achievementAdapter.notifyDataSetChanged();
                    } else {
                        fragment.notifyChange();
                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    private void checkPostsLikes(boolean isAchievementFragment) {
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int number_likes = 0;
                for (int i = 0; i < task.getResult().size(); i++) {
                    number_likes += task.getResult().get(i).getLikes();
                }
                if (number_likes >= goalValue) {
                    state_achievement = true;
                    if (isAchievementFragment) {

                        achievementAdapter.notifyDataSetChanged();
                    } else {
                        fragment.notifyChange();
                    }
                }
            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    private void checkOnePostLikes(boolean isAchievementFragment) {
        database.getPostsByUserId(auth.getCurrentUser().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (int i = 0; i < task.getResult().size(); i++) {
                    if (task.getResult().get(i).getLikes() >= goalValue) {
                        state_achievement = true;
                        i = task.getResult().size();
                        if (isAchievementFragment) {
                            achievementAdapter.notifyDataSetChanged();
                        } else {
                            fragment.notifyChange();
                        }

                    }
                }

            } else {
                System.out.println(task.getException().getMessage());
            }
        });
    }

    public void checkPointsOfflineGame(boolean isAchievementFragment){
        if(auth.getCurrentUser().getHighScore() >= goalValue){
            state_achievement = true;
            if(isAchievementFragment){
                try {
                    achievementAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
            else{
                fragment.notifyChange();
            }
        }
    }

    public void setAdapter(AchievementAdapter achievementAdapter) {
        this.achievementAdapter = achievementAdapter;
    }

    //the number is the number of followers, followings, posts, likes achieved in one posts or in total in order to succeed one achievement
    public enum Achievement_type {
        FOLLOWERS,
        FOLLOWING,
        NB_POSTS,
        NB_LIKES_POST,
        NB_LIKES_POSTS_TOTAL,
        OFFLINE_GAME_POINTS
    }
}
