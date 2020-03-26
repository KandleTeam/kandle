package ch.epfl.sdp.kandle.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import ch.epfl.sdp.kandle.CustomAccountActivity;
import ch.epfl.sdp.kandle.ImagePicker.ProfilePicPicker;
import ch.epfl.sdp.kandle.MainActivity;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.AuthenticationUser;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;


public class ProfileFragment extends Fragment {

    User user;
    private ProfilePicPicker profilePicPicker;
    ImageView mProfilePicture, mEditPicture, mProfilePictureInMenu, mEditName;
    TextView mNumberOfFollowers, mNumberOfFollowing, mUsername, mNicknameView, mNickNameInMenu;
    EditText mNicknameEdit;
    ViewSwitcher mNickname;
    Button mFollowButton, mValidateNameButton;
    Authentication auth;
    Database database;

    public final static int PROFILE_PICTURE_BEFORE = 6;
    public final static int PROFILE_PICTURE_AFTER = 7;

    private ProfileFragment (User user){
        this.user = user;
    }


    public static ProfileFragment newInstance(User user) {
        return new ProfileFragment(user);
    }

    private void getViews(View parent) {
        mProfilePicture = parent.findViewById(R.id.profilePicture);
        NavigationView mNavigationView = getActivity().findViewById(R.id.navigation_view);
        mProfilePictureInMenu = mNavigationView.getHeaderView(0).findViewById(R.id.profilePicInMenu);
        mNickNameInMenu = mNavigationView.getHeaderView(0).findViewById(R.id.nicknameInMenu);
        mNumberOfFollowers = parent.findViewById(R.id.profileNumberOfFollowers);
        mNumberOfFollowing = parent.findViewById(R.id.profileNumberOfFollowing);
        mNickname = parent.findViewById(R.id.profileNickname);
        mUsername = parent.findViewById(R.id.profileUsername);
        mFollowButton = parent.findViewById(R.id.profileFollowButton);
        mEditPicture = parent.findViewById(R.id.profileEditPictureButton);
        mEditName = parent.findViewById(R.id.profileEditNameButton);
        mValidateNameButton = parent.findViewById(R.id.profileValidateNameButton);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();

        profilePicPicker = new ProfilePicPicker(this);

        getViews(view);

        final AuthenticationUser authenticationUser = auth.getCurrentUser();

        mValidateNameButton.setVisibility(View.GONE);

        if(!user.getId().equals(authenticationUser.getUid())){
            mEditPicture.setVisibility(View.GONE);
            mEditName.setVisibility(View.GONE);
        }
        else {
            mEditPicture.setOnClickListener(v -> {
                profilePicPicker.openImage();
            });

        }

        mEditName.setOnClickListener(v -> {
            mEditName.setVisibility(View.GONE);
            mValidateNameButton.setVisibility(View.VISIBLE);
            mNickname.showNext();
        });

        mValidateNameButton.setOnClickListener(v -> {
            String nickname = mNicknameEdit.getText().toString();
            if (nickname.trim().length()>0) {
                mNicknameView.setText(nickname.trim());
                mNickNameInMenu.setText(nickname.trim());
                database.updateNickname(nickname.trim());
            }
            mNickname.showPrevious();
            mValidateNameButton.setVisibility(View.GONE);
            mEditName.setVisibility(View.VISIBLE);
        });

        mNicknameView = mNickname.findViewById(R.id.text_view);
        mNicknameEdit = mNickname.findViewById(R.id.edit_view);
        mNicknameView.setText(user.getFullname());
        mNicknameEdit.setText(user.getFullname());

        mUsername.setText("@" + user.getUsername());
        if(user.getImageURL() != null) {
            mProfilePicture.setTag(PROFILE_PICTURE_BEFORE);
            Picasso.get().load(user.getImageURL()).into(mProfilePicture);
        }

        setNumberOfFollowers();
        setNumberOfFollowing();

        database.userIdFollowingList(authenticationUser.getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                if ((task.getResult() == null) || (!task.getResult().contains(user.getId()))){
                    mFollowButton.setText(R.string.followBtnNotFollowing);
                }

                else {
                    mFollowButton.setText(R.string.followBtnAlreadyFollowing);
                }
            }
        });


        if (user.getId().equals(authenticationUser.getUid())){
            mFollowButton.setVisibility(View.GONE);
        }
        else {
            mFollowButton.setOnClickListener(followButtonListener(authenticationUser));
        }

        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();


        mNumberOfFollowers.setOnClickListener(v -> database.userFollowersList(user.getId()).addOnCompleteListener(numberListener("Followers", fragmentManager)));

        mNumberOfFollowing.setOnClickListener(v -> database.userFollowingList(user.getId()).addOnCompleteListener(numberListener("Following", fragmentManager)));


        return view;
    }

    private OnCompleteListener<List<User>> numberListener (String title, final FragmentManager fragmentManager ){
        return new OnCompleteListener<List<User>>() {
            @Override
            public void onComplete(@NonNull Task<List<User>> task) {
                if (task.isSuccessful()){

                    fragmentManager.beginTransaction().replace(R.id.flContent, ListUsersFragment.newInstance(
                            task.getResult()
                            , title
                            , Integer.toString(task.getResult().size())
                    ))
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(null)
                            .commit();

                }
            }
        };
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
        database.userIdFollowingList(user.getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    mNumberOfFollowing.setText(Integer.toString(task.getResult().size()));
                }
            }
        });
    }

    private void setNumberOfFollowers(){
        database.userIdFollowersList(user.getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    mNumberOfFollowers.setText(Integer.toString(task.getResult().size()));
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = profilePicPicker.handleActivityResult(requestCode, resultCode, data);

        if (uri != null) {
            mProfilePicture.setTag(PROFILE_PICTURE_AFTER);
            mProfilePicture.setImageURI(uri);
            mProfilePictureInMenu.setTag(PROFILE_PICTURE_AFTER);
            mProfilePictureInMenu.setImageURI(uri);
        }
    }
}

