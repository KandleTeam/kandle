package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.kandle.Achievement;
import ch.epfl.sdp.kandle.AchievementAdapter;
import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.caching.CachedDatabase;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class AchievementFragment extends Fragment {

    private Authentication auth;
    private Database database;
    private List<Achievement> achievements;
    private RecyclerView flAchievements;
    private View view;



    public AchievementFragment() {
        auth = DependencyManager.getAuthSystem();
        database = new CachedDatabase();
        achievements = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_achievement, container, false);
        AchievementAdapter achievementAdapter = new AchievementAdapter(achievements, this.getContext());
        for(int i = 1; i < 4; i++){
            achievements.add(new Achievement(Achievement.Achievement_type.NB_POSTS, i * 5, achievementAdapter));
        }
        for(int i = 1; i < 3; i++){
            achievements.add(new Achievement(Achievement.Achievement_type.FOLLOWING, i * 3, achievementAdapter));
        }
        for(int i = 1; i < 3; i++){
            achievements.add(new Achievement(Achievement.Achievement_type.FOLLOWERS,  i * 3, achievementAdapter));
        }
        for(int i = 1; i < 3; i++){
            achievements.add(new Achievement(Achievement.Achievement_type.NB_LIKES_POST, i * 3, achievementAdapter));
        }
        for (int i = 1; i < 3; i++){
            achievements.add(new Achievement(Achievement.Achievement_type.NB_LIKES_POSTS_TOTAL, i * 5, achievementAdapter));
        }
        achievementAdapter.changeList(achievements);
        flAchievements = view.findViewById(R.id.flAchievements);
        flAchievements.setLayoutManager(new LinearLayoutManager(this.getContext()));
        flAchievements.setAdapter(achievementAdapter);
        return view;
    }






}
