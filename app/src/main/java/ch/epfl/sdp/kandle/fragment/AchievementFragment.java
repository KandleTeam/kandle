package ch.epfl.sdp.kandle.fragment;

import android.app.Activity;
import android.content.Context;
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


    private static List<Achievement> achievements = new ArrayList<>();
    private RecyclerView flAchievements;
    private View view;
    private AchievementAdapter achievementAdapter =  new AchievementAdapter(achievements, this.getContext());


    public AchievementFragment() {
        if(achievements.isEmpty()) {
            createAchievements(3, 5, Achievement.Achievement_type.NB_POSTS, null);
            createAchievements(2, 3, Achievement.Achievement_type.FOLLOWING, null);
            createAchievements(2, 3, Achievement.Achievement_type.FOLLOWERS, null);
            createAchievements(2, 3, Achievement.Achievement_type.NB_LIKES_POST, null);
            createAchievements(2, 5, Achievement.Achievement_type.NB_LIKES_POSTS_TOTAL, null);
        }
    }

    public static List<Achievement> getAchievements(){
        if(achievements.isEmpty()) {
            createAchievements(3, 5, Achievement.Achievement_type.NB_POSTS, null);
            createAchievements(2, 3, Achievement.Achievement_type.FOLLOWING, null);
            createAchievements(2, 3, Achievement.Achievement_type.FOLLOWERS, null);
            createAchievements(2, 3, Achievement.Achievement_type.NB_LIKES_POST, null);
            createAchievements(2, 5, Achievement.Achievement_type.NB_LIKES_POSTS_TOTAL, null);
        }
        return achievements;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        for(Achievement achievement : achievements){
            achievement.setAdapter(achievementAdapter);
        }
        view = inflater.inflate(R.layout.fragment_achievement, container, false);
        achievementAdapter.changeList(achievements);
        flAchievements = view.findViewById(R.id.flAchievements);
        flAchievements.setLayoutManager(new LinearLayoutManager(this.getContext()));
        flAchievements.setAdapter(achievementAdapter);
        return view;
    }

    private static void createAchievements(int numberOfAchievements, int scaleIncrementation, Achievement.Achievement_type typeOfAchievement, AchievementAdapter achievementAdapter) {
        for (int i = 1; i < numberOfAchievements + 1; i++) {
            achievements.add(new Achievement(typeOfAchievement, i * scaleIncrementation, achievementAdapter, null));
        }
    }


}
