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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import ch.epfl.sdp.kandle.ImagePicker.ProfilePicPicker;
import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.CachedDatabase;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;


public class ProfileFragment extends Fragment {

    public final static int PROFILE_PICTURE_BEFORE = 6;
    public final static int PROFILE_PICTURE_AFTER = 7;
    private User user;
    private ProfilePicPicker profilePicPicker;
    private ImageView mProfilePicture, mEditPicture, mProfilePictureInMenu, mEditName;
    private TextView mNumberOfFollowers, mNumberOfFollowing, mUsername, mNicknameView, mNickNameInMenu;
    private EditText mNicknameEdit;
    private ViewSwitcher mNickname;
    private Button mFollowButton, mValidateNameButton, mValidatePictureButton;
    private Authentication auth;
    private Database database;

    private ProfileFragment(User user) {
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
        mValidatePictureButton = parent.findViewById(R.id.profileValidatePictureButton);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = DependencyManager.getAuthSystem();
        database = new CachedDatabase();

        profilePicPicker = new ProfilePicPicker(this);

        getViews(view);

        final User currentUser = LoggedInUser.getInstance();

        mValidateNameButton.setVisibility(View.GONE);
        mValidatePictureButton.setVisibility(View.GONE);

        if (!user.getId().equals(currentUser.getId())) {
            mEditPicture.setVisibility(View.GONE);
            mEditName.setVisibility(View.GONE);
        } else {
            mEditPicture.setOnClickListener(v -> {
                mEditPicture.setVisibility(View.GONE);
                profilePicPicker.openImage();
                mValidatePictureButton.setVisibility(View.VISIBLE);
            });

        }

        mEditName.setOnClickListener(v -> {
            mEditName.setVisibility(View.GONE);
            mValidateNameButton.setVisibility(View.VISIBLE);
            mNickname.showNext();
        });

        mValidateNameButton.setOnClickListener(v -> {

            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getActivity().getCurrentFocus()!=null) {

                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            String nickname = mNicknameEdit.getText().toString();
            if (nickname.trim().length() > 0) {
                mNicknameView.setText(nickname.trim());
                mNickNameInMenu.setText(nickname.trim());
                LoggedInUser.getInstance().setNickname(nickname.trim());
                database.updateNickname(nickname.trim());
            }
            mNickname.showPrevious();
            mValidateNameButton.setVisibility(View.GONE);
            mEditName.setVisibility(View.VISIBLE);
        });

        mValidatePictureButton.setOnClickListener(v -> {
            ProgressDialog pd = new ProgressDialog(getContext());
            pd.setMessage("uploading");
            pd.show();
            profilePicPicker.setProfilePicture().addOnCompleteListener(task -> {
                mProfilePictureInMenu.setTag(PROFILE_PICTURE_AFTER);
                mProfilePictureInMenu.setImageURI(profilePicPicker.getImageUri());
                pd.dismiss();
            });
            mValidatePictureButton.setVisibility(View.GONE);
            mEditPicture.setVisibility(View.VISIBLE);
        });

        mNicknameView = mNickname.findViewById(R.id.text_view);
        mNicknameEdit = mNickname.findViewById(R.id.edit_view);
        mNicknameView.setText(user.getNickname());
        mNicknameEdit.setText(user.getNickname());

        mUsername.setText("@" + user.getUsername());
        if (user.getImageURL() != null) {
            mProfilePicture.setTag(PROFILE_PICTURE_BEFORE);
            Picasso.get().load(user.getImageURL()).into(mProfilePicture);
        }

        setNumberOfFollowers();
        setNumberOfFollowing();

        database.userIdFollowingList(currentUser.getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if ((task.getResult() == null) || (!task.getResult().contains(user.getId()))) {
                    mFollowButton.setText(R.string.followBtnNotFollowing);
                } else {
                    mFollowButton.setText(R.string.followBtnAlreadyFollowing);
                }
            }
        });


        if (user.getId().equals(currentUser.getId())) {
            mFollowButton.setVisibility(View.GONE);
        } else {
            mFollowButton.setOnClickListener(followButtonListener(currentUser));
        }

        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();


        mNumberOfFollowers.setOnClickListener(v -> database.userFollowersList(user.getId()).addOnCompleteListener(numberListener("Followers", fragmentManager)));

        mNumberOfFollowing.setOnClickListener(v -> database.userFollowingList(user.getId()).addOnCompleteListener(numberListener("Following", fragmentManager)));


        return view;
    }

    private OnCompleteListener<List<User>> numberListener(String title, final FragmentManager fragmentManager) {
        return new OnCompleteListener<List<User>>() {
            @Override
            public void onComplete(@NonNull Task<List<User>> task) {
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
            }
        };
    }

    private View.OnClickListener followButtonListener(User currUser) {
        return v -> {
            System.out.println(mFollowButton.getText().toString());
            if (mFollowButton.getText().toString().equals(getString(R.string.followBtnNotFollowing))) {
                database.follow(currUser.getId(), user.getId()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("not following -> following");

                        mFollowButton.setText(R.string.followBtnAlreadyFollowing);
                        setNumberOfFollowers();
                    }
                });
            } else {
                database.unFollow(currUser.getId(), user.getId()).addOnCompleteListener(task -> {
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
        profilePicPicker.handleActivityResult(requestCode, resultCode, data);
        Uri uri = profilePicPicker.getImageUri();

        if (uri != null) {
            mProfilePicture.setTag(PROFILE_PICTURE_AFTER);
            mProfilePicture.setImageURI(uri);
        }
    }
}

