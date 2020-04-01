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

import ch.epfl.sdp.kandle.AchievementsActivity;
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

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_achievement, container, false);
        TextView tv1 = (TextView)view.findViewById(R.id.is_posts);
        tv1.setText("NOT DONE");
        TextView tv2 = (TextView)view.findViewById(R.id.is_following);
        tv2.setText("NOT DONE");

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();

        database.getNumberOfPosts(auth.getCurrentUser().getUid()).addOnCompleteListener(task1 ->{
            if(task1.isSuccessful()){
                if(task1.getResult() >= 10){
                    tv1.setText("DONE");
                }
            }
            else {
                System.out.println(task1.getException().getMessage());
            }
        });

        database.userIdFollowingList(auth.getCurrentUser().getUid()).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               if(task.getResult().size() >= 3){
                   tv2.setText("DONE");
               }
           }
           else {
               System.out.println(task.getException().getMessage());
           }
        });
        return view;
    }


}
