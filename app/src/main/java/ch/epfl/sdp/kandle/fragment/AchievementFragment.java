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
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class AchievementFragment extends Fragment {

    private Authentication auth;
    private Database database;
    private List<Achievement> achievements;
    private RecyclerView flAchievements;
    private View view;


    public AchievementFragment() {
        auth = DependencyManager.getAuthSystem();
        achievements = new ArrayList<>();
        database = new CachedFirestoreDatabase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_achievement, container, false);
        AchievementAdapter achievementAdapter = new AchievementAdapter(achievements, this.getContext());
        createAchievements(3, 5, Achievement.Achievement_type.NB_POSTS, achievementAdapter);
        createAchievements(2, 3, Achievement.Achievement_type.FOLLOWING, achievementAdapter);
        createAchievements(2, 3, Achievement.Achievement_type.FOLLOWERS, achievementAdapter);
        createAchievements(2, 3, Achievement.Achievement_type.NB_LIKES_POST, achievementAdapter);
        createAchievements(2, 5, Achievement.Achievement_type.NB_LIKES_POSTS_TOTAL, achievementAdapter);
        achievementAdapter.changeList(achievements);
        flAchievements = view.findViewById(R.id.flAchievements);
        flAchievements.setLayoutManager(new LinearLayoutManager(this.getContext()));
        flAchievements.setAdapter(achievementAdapter);
        return view;
    }

    private void createAchievements(int numberOfAchievements, int scaleIncrementation, Achievement.Achievement_type typeOfAchievement, AchievementAdapter achievementAdapter) {
        for (int i = 1; i < numberOfAchievements + 1; i++) {
            achievements.add(new Achievement(typeOfAchievement, i * scaleIncrementation, achievementAdapter));
        }
    }


}
