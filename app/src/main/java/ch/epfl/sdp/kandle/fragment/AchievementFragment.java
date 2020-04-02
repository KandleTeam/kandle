package ch.epfl.sdp.kandle.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.UserAdapter;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.AuthenticationUser;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.InternalStorageHandler;

public class AchievementFragment extends Fragment {

    private Authentication auth;
    private Database database;

    public AchievementFragment(){
        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_achievement, container, false);
        TextView tv1 = (TextView)view.findViewById(R.id.is_posts);
        TextView tv2 = (TextView)view.findViewById(R.id.is_following);
        TextView tv3 = (TextView)view.findViewById(R.id.is_followers);
        checkPosts(tv1);
        checkFollowing(tv2);
        checkFollowers(tv3);
        return view;
    }

    private void checkPosts(TextView tv){
        database.getPostsByUserId(auth.getCurrentUser().getUid()).addOnCompleteListener(task1 ->{
            if(task1.isSuccessful()){
                if(task1.getResult().size() >= 10){
                    tv.setText("DONE");
                }
                else {
                    tv.setText("NOT DONE");
                }
            }
            else {
                System.out.println(task1.getException().getMessage());
                tv.setText("NOT DONE");
            }
        });
    }

    private void checkFollowing(TextView tv){
        database.userIdFollowingList(auth.getCurrentUser().getUid()).addOnCompleteListener(task2 -> {
            if(task2.isSuccessful()){
                if(task2.getResult().size() >= 3){
                    tv.setText("DONE");
                }
                else {
                    tv.setText("NOT DONE");
                }
            }
            else {
                tv.setText("NOT DONE");
                System.out.println(task2.getException().getMessage());
            }
        });
    }

    private void checkFollowers(TextView tv){
        database.userIdFollowersList(auth.getCurrentUser().getUid()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().size() >= 3){
                    tv.setText("DONE");
                }
                else {
                    tv.setText("NOT DONE");
                }
            }
            else {
                tv.setText("NOT DONE");
                System.out.println(task.getException().getMessage());
            }
        });
    }




}
