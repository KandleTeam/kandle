package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.kandle.Achievement;
import ch.epfl.sdp.kandle.AchievementAdapter;
import ch.epfl.sdp.kandle.R;

public class AchievementFragment extends Fragment {

    final static int NUMBER_FOLLOWERS_ACHIEVEMENTS = 2;
    final static int NUMBER_FOLLOWING_ACHIEVEMENTS = 2;
    final static int NUMBER_POSTS_ACHIEVEMENTS = 3;
    final static int NUMBER_LIKES_TOTAL_ACHIEVEMENTS = 2;
    final static int NUMBER_LIKES_POST_ACHIEVEMENTS = 2;
    final static int SCALE_FOLLOWERS_ACHIEVEMENTS = 3;
    final static int SCALE_FOLLOWING_ACHIEVEMENTS = 3;
    final static int SCALE_POSTS_ACHIEVEMENTS = 5;
    final static int SCALE_LIKES_TOTAL_ACHIEVEMENTS = 5;
    final static int SCALE_LIKES_POST_ACHIEVEMENTS = 3;

    /**
     * Creates an AchievementFragment object
     */
    public AchievementFragment(){}

    /**
     * inserts Achievements in the list achievements
     * @param achievements
     * @param achievementAdapter
     */
    public static void getAchievements(List<Achievement> achievements, AchievementAdapter achievementAdapter) {
        createAchievements(achievements, NUMBER_POSTS_ACHIEVEMENTS, SCALE_POSTS_ACHIEVEMENTS, Achievement.Achievement_type.NB_POSTS, achievementAdapter);
        createAchievements(achievements, NUMBER_FOLLOWING_ACHIEVEMENTS, SCALE_FOLLOWING_ACHIEVEMENTS, Achievement.Achievement_type.FOLLOWING, achievementAdapter);
        createAchievements(achievements, NUMBER_FOLLOWERS_ACHIEVEMENTS, SCALE_FOLLOWERS_ACHIEVEMENTS, Achievement.Achievement_type.FOLLOWERS, achievementAdapter);
        createAchievements(achievements, NUMBER_LIKES_POST_ACHIEVEMENTS, SCALE_LIKES_POST_ACHIEVEMENTS, Achievement.Achievement_type.NB_LIKES_POST, achievementAdapter);
        createAchievements(achievements, NUMBER_LIKES_TOTAL_ACHIEVEMENTS, SCALE_LIKES_TOTAL_ACHIEVEMENTS, Achievement.Achievement_type.NB_LIKES_POSTS_TOTAL, achievementAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        List<Achievement> achievements = new ArrayList<>();
        AchievementAdapter achievementAdapter = new AchievementAdapter(achievements);
        getAchievements(achievements, achievementAdapter);
        View view = inflater.inflate(R.layout.fragment_achievement, container, false);
        achievementAdapter.changeList(achievements);
        RecyclerView flAchievements = view.findViewById(R.id.flAchievements);
        flAchievements.setLayoutManager(new LinearLayoutManager(this.getContext()));
        flAchievements.setAdapter(achievementAdapter);
        return view;
    }

    private static void createAchievements(List<Achievement> achievements, int numberOfAchievements, int scaleIncrementation, Achievement.Achievement_type typeOfAchievement, AchievementAdapter achievementAdapter) {
        for (int i = 1; i < numberOfAchievements + 1; i++) {
            achievements.add(new Achievement(typeOfAchievement, i * scaleIncrementation, achievementAdapter, null));
        }
    }


}
