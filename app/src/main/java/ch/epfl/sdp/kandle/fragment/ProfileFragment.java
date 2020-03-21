package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.util.List;

import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.AuthenticationUser;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;


public class ProfileFragment extends Fragment {

    User user;

    ImageView mProfilePicture;
    TextView mNumberOfFollowers, mNumberOfFollowing, mUsername;
    Button mFollowButton;
    Authentication auth;
    Database database;

    public final static int PROFILE_PICTURE_TAG = 6;

    public ProfileFragment (User user){
        this.user = user;
    }


    public static ProfileFragment newInstance(User user) {
        return new ProfileFragment(user);
    }

    private void getViews(View parent) {
        mProfilePicture = parent.findViewById(R.id.profilePicture);
        mNumberOfFollowers = parent.findViewById(R.id.profileNumberOfFollowers);
        mNumberOfFollowing = parent.findViewById(R.id.profileNumberOfFollowing);
        mUsername = parent.findViewById(R.id.profileUsername);
        mFollowButton = parent.findViewById(R.id.profileFollowButton);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();

        getViews(view);

        final AuthenticationUser authenticationUser = auth.getCurrentUser();

        mUsername.setText(user.getUsername());
        if(user.getImageURL() != null) {
            mProfilePicture.setTag(PROFILE_PICTURE_TAG);
            Picasso.get().load(user.getImageURL()).into(mProfilePicture);
        }

        setNumberOfFollowers();
        setNumberOfFollowing();

        database.followingList(authenticationUser.getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                if ((task.getResult() == null) || (!task.getResult().contains(user.getId()))){
                    mFollowButton.setText(R.string.followBtnNotFollowing);
                }

                else {
                    mFollowButton.setText(R.string.followBtnAlreadyFollowing);
                }
            }
        });

        mFollowButton.setOnClickListener(followButtonListener(authenticationUser));


        return view;
    }

    private View.OnClickListener followButtonListener(AuthenticationUser currUser) {
        return v -> {
            System.out.println(mFollowButton.getText().toString());
            if (mFollowButton.getText().toString().equals(getString(R.string.followBtnNotFollowing))) {
                database.follow(currUser.getUid(), user.getId()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        System.out.println("not following -> following");

                        mFollowButton.setText(R.string.followBtnAlreadyFollowing);
                        setNumberOfFollowers();
                    }
                });
            }
            else {
                database.unFollow(currUser.getUid(), user.getId()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("following -> not following");

                        mFollowButton.setText(R.string.followBtnNotFollowing);
                        setNumberOfFollowers();
                    }
                });
            }
        };
    }


    private void setNumberOfFollowing() {
        database.followingList(user.getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    mNumberOfFollowing.setText(Integer.toString(task.getResult().size()));
                }
            }
        });
    }

    private void setNumberOfFollowers(){
        database.followersList(user.getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    mNumberOfFollowers.setText(Integer.toString(task.getResult().size()));
                }
            }
        });
    }
}
