package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class AchievementFragment extends Fragment {

    private Authentication auth;
    private Database database;

    public AchievementFragment() {
        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievement, container, false);
        TextView tv1 = (TextView) view.findViewById(R.id.is_posts);
        TextView tv2 = (TextView) view.findViewById(R.id.is_following);
        TextView tv3 = (TextView) view.findViewById(R.id.is_followers);
        checkPosts(tv1);
        checkFollowing(tv2);
        checkFollowers(tv3);
        return view;
    }

    private void checkPosts(TextView tv) {
        database.getPostsByUserId(LoggedInUser.getInstance().getId()).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                setText(tv, task1.getResult().size() >= 10);
            } else {
                setText(tv, false);
                System.out.println(task1.getException().getMessage());
            }
        });
    }

    private void checkFollowing(TextView tv) {
        database.userIdFollowingList(LoggedInUser.getInstance().getId()).addOnCompleteListener(task2 -> {
            if (task2.isSuccessful()) {
                setText(tv, task2.getResult().size() >= 3);
            } else {
                tv.setText("NOT DONE");
                System.out.println(task2.getException().getMessage());
            }
        });
    }

    private void checkFollowers(TextView tv) {
        database.userIdFollowersList(LoggedInUser.getInstance().getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                setText(tv, task.getResult().size() >= 3);
            } else {
                setText(tv, false);
                System.out.println(task.getException().getMessage());
            }
        });
    }

    private void setText(TextView tv, boolean condition) {
        if (condition) {
            tv.setText("DONE");
        } else {
            tv.setText("NOT DONE");
        }
    }


}
