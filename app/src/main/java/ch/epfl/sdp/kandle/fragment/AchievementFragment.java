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

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.entities.achievement.Achievement;
import ch.epfl.sdp.kandle.entities.achievement.AchievementAdapter;

public class AchievementFragment extends Fragment {


    private static List<Achievement> achievements;
    private RecyclerView flAchievements;
    private View view;


    public AchievementFragment() {
        achievements = new ArrayList<>();
    }

    public static void getAchievements(List<Achievement> achievements) {
        createAchievements(achievements, 3, 5, Achievement.Achievement_type.NB_POSTS, null);
        createAchievements(achievements, 2, 3, Achievement.Achievement_type.FOLLOWING, null);
        createAchievements(achievements, 2, 3, Achievement.Achievement_type.FOLLOWERS, null);
        createAchievements(achievements, 2, 3, Achievement.Achievement_type.NB_LIKES_POST, null);
        createAchievements(achievements, 2, 5, Achievement.Achievement_type.NB_LIKES_POSTS_TOTAL, null);
        createAchievements(achievements, 2, 5, Achievement.Achievement_type.OFFLINE_GAME_POINTS, null);
    }

    private static void createAchievements(List<Achievement> achievements, int numberOfAchievements, int scaleIncrementation, Achievement.Achievement_type typeOfAchievement, AchievementAdapter achievementAdapter) {
        for (int i = 1; i < numberOfAchievements + 1; i++) {
            achievements.add(new Achievement(typeOfAchievement, i * scaleIncrementation, achievementAdapter, null));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AchievementAdapter achievementAdapter = new AchievementAdapter(achievements, this.getContext());
        createAchievements(achievements, 3, 5, Achievement.Achievement_type.NB_POSTS, achievementAdapter);
        createAchievements(achievements, 2, 3, Achievement.Achievement_type.FOLLOWING, achievementAdapter);
        createAchievements(achievements, 2, 3, Achievement.Achievement_type.FOLLOWERS, achievementAdapter);
        createAchievements(achievements, 2, 3, Achievement.Achievement_type.NB_LIKES_POST, achievementAdapter);
        createAchievements(achievements, 2, 5, Achievement.Achievement_type.NB_LIKES_POSTS_TOTAL, achievementAdapter);
        createAchievements(achievements, 2, 5, Achievement.Achievement_type.OFFLINE_GAME_POINTS, achievementAdapter);
        view = inflater.inflate(R.layout.fragment_achievement, container, false);
        achievementAdapter.changeList(achievements);
        flAchievements = view.findViewById(R.id.flAchievements);
        flAchievements.setLayoutManager(new LinearLayoutManager(this.getContext()));
        flAchievements.setAdapter(achievementAdapter);
        return view;
    }


}
