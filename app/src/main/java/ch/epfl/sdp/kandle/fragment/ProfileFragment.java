package ch.epfl.sdp.kandle.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.kandle.entities.achievement.Achievement;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.authentification.Authentication;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.utils.imagePicker.ProfilePicPicker;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;


public class ProfileFragment extends Fragment {

    public final static int PROFILE_PICTURE_BEFORE = 6;
    public final static int PROFILE_PICTURE_AFTER = 7;
    private User user;
    private ImageView mProfilePicture, mEditPicture, mProfilePictureInMenu, mEditName, mBadge;
    private TextView mNumberOfFollowers, mNumberOfFollowing, mUsername, mNicknameView, mNickNameInMenu;
    private EditText mNicknameEdit;
    private ViewSwitcher mNickname;
    private Button mFollowButton, mValidateNameButton, mValidatePictureButton;
    private Authentication auth;
    private Database database;
    private Uri imageUri;
    private List<Achievement> achievements;
    private static int nbValidatedAchievements;

    private ProfileFragment(User user) {
        this.user = user;
        this.achievements = new ArrayList<>();
        nbValidatedAchievements = 0;
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
        mValidatePictureButton = parent.findViewById(R.id.profileValidatePictureButton);
        mBadge = parent.findViewById(R.id.badgePicture);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        AchievementFragment.getAchievements(achievements);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = DependencyManager.getAuthSystem();
        database = new CachedFirestoreDatabase();

        getViews(view);

        final User currentUser = LoggedInUser.getInstance();

        mValidateNameButton.setVisibility(View.GONE);
        mValidatePictureButton.setVisibility(View.GONE);

        if (!user.getId().equals(currentUser.getId()) || LoggedInUser.isGuestMode()) {
            mEditPicture.setVisibility(View.GONE);
            mEditName.setVisibility(View.GONE);
        } else {
            mEditPicture.setOnClickListener(v -> {
                mEditPicture.setVisibility(View.GONE);
                ProfilePicPicker.openImage(this);
            });

        }
        nbValidatedAchievements = 0;
        for(Achievement achievement: achievements){

            achievement.setProfileFragment(this);
            achievement.checkAchievement(false);
        }

       changeBadge();

        mEditName.setOnClickListener(v -> {
            mEditName.setVisibility(View.GONE);
            mValidateNameButton.setVisibility(View.VISIBLE);
            mNickname.showNext();
        });

        mValidateNameButton.setOnClickListener(v -> {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getActivity().getCurrentFocus() != null) {

                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            String nickname = mNicknameEdit.getText().toString();
            if (nickname.trim().length() > 0) {
                String nickname_trim = nickname.trim();
                mNicknameView.setText(nickname_trim);
                mNickNameInMenu.setText(nickname_trim);
                auth.getCurrentUser().setNickname(nickname_trim);
                HashMap<String, Object> nicknameMap = new HashMap<>();
                nicknameMap.put("nickname", nickname_trim);
                database.updateNickname(nicknameMap);
            }
            mNickname.showPrevious();
            mValidateNameButton.setVisibility(View.GONE);
            mEditName.setVisibility(View.VISIBLE);
        });

        mValidatePictureButton.setOnClickListener(v -> {
            ProgressDialog pd = new ProgressDialog(getContext());
            pd.setMessage("uploading");
            pd.show();
            ProfilePicPicker.setProfilePicture(imageUri).addOnCompleteListener(task -> {
                mProfilePictureInMenu.setTag(PROFILE_PICTURE_AFTER);
                mProfilePictureInMenu.setImageURI(imageUri);
                pd.dismiss();
            });
            mValidatePictureButton.setVisibility(View.GONE);
            mEditPicture.setVisibility(View.VISIBLE);
        });

        mNicknameView = mNickname.findViewById(R.id.text_view);
        mNicknameEdit = mNickname.findViewById(R.id.edit_view);
        mNicknameView.setText(user.getNickname());
        mNicknameEdit.setText(user.getNickname());

        mUsername.setText(String.format("@%s", user.getUsername()));
        if (user.getImageURL() != null) {
            mProfilePicture.setTag(PROFILE_PICTURE_BEFORE);
            File image = DependencyManager.getInternalStorageSystem().getImageFileById(user.getId());
            if(image != null) {
                Picasso.get().load(image).into(mProfilePicture);
                System.out.println("Fetched from internal storage");
            }else {
                Picasso.get().load(user.getImageURL()).into(mProfilePicture);
            }
        }

        setNumberOfFollowers();
        setNumberOfFollowing();


        if (user.getId().equals(currentUser.getId())) {
            mFollowButton.setVisibility(View.GONE);
        } else {
            database.userIdFollowingList(currentUser.getId()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if ((task.getResult() == null) || (!task.getResult().contains(user.getId()))) {
                        mFollowButton.setText(R.string.followBtnNotFollowing);
                    } else {
                        mFollowButton.setText(R.string.followBtnAlreadyFollowing);
                    }
                }
            });
            mFollowButton.setOnClickListener(followButtonListener(currentUser));
        }

        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();


        mNumberOfFollowers.setOnClickListener(v -> database.userFollowersList(user.getId()).addOnCompleteListener(numberListener("Followers", fragmentManager)));
        mNumberOfFollowing.setOnClickListener(v -> database.userFollowingList(user.getId()).addOnCompleteListener(numberListener("Following", fragmentManager)));
        if (LoggedInUser.isGuestMode()) {
            mNumberOfFollowers.setClickable(false);
            mNumberOfFollowing.setClickable(false);
        }


        return view;
    }

    private OnCompleteListener<List<User>> numberListener(String title, final FragmentManager fragmentManager) {
        return task -> {
            if (task.isSuccessful()) {

                fragmentManager.beginTransaction().replace(R.id.flContent, ListUsersFragment.newInstance(
                        task.getResult()
                        , title
                        , Integer.toString(task.getResult().size())
                ))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();

            }
        };
    }

    private View.OnClickListener followButtonListener(User currUser) {
        return v -> {
            if (mFollowButton.getText().toString().equals(getString(R.string.followBtnNotFollowing))) {
                Map<String, String> notificationData = new HashMap<>();
                notificationData.put("title", getString(R.string.NOTIFICATION_FOLLOW_TITLE) );
                notificationData.put("text", getString(R.string.NOTIFICATION_FOLLOW_TEXT));
                notificationData.put("toUserId", user.getId());
                database.follow(currUser.getId(), user.getId(), notificationData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mFollowButton.setText(R.string.followBtnAlreadyFollowing);
                        setNumberOfFollowers();
                    }
                });
            } else {
                database.unFollow(currUser.getId(), user.getId()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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

    private void setNumberOfFollowers() {
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
        imageUri = ProfilePicPicker.handleActivityResultAndGetUri(requestCode, resultCode, data);

        if (imageUri != null) {
            mProfilePicture.setTag(PROFILE_PICTURE_AFTER);
            mProfilePicture.setImageURI(imageUri);
            mValidatePictureButton.setVisibility(View.VISIBLE);
        }
    }

    public void notifyChange(){
        nbValidatedAchievements++;
        changeBadge();
    }

    private void changeBadge(){
        mBadge.setTag(R.drawable.ic_icons2_medal_64);
        if(nbValidatedAchievements > 0 && nbValidatedAchievements <= 2){
            mBadge.setImageResource(R.drawable.ic_icons2_medal_64);
            mBadge.setTag(R.drawable.ic_icons2_medal_64);
        }
        else if (nbValidatedAchievements <= 4 && nbValidatedAchievements > 2){
            mBadge.setImageResource(R.drawable.ic_icons1_medal_64);
            mBadge.setTag(R.drawable.ic_icons1_medal_64);

        }
        else if (nbValidatedAchievements <= 6 && nbValidatedAchievements > 4){
            mBadge.setImageResource(R.drawable.icons8_medal_64_1);
            mBadge.setTag(R.drawable.icons8_medal_64_1);
        }
        else if(nbValidatedAchievements > 6){
            mBadge.setImageResource(R.drawable.icons8_medal_64_2);
            mBadge.setTag(R.drawable.icons8_medal_64_2);
        }

    }


}

