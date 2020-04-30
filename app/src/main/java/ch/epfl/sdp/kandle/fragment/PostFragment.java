package ch.epfl.sdp.kandle.fragment;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;


public class PostFragment extends Fragment {

    public final static int POST_IMAGE = 20;
    public final static int PROFILE_PICTURE_IMAGE = 21;
    Database database;
    private Post post;
    private User user;
    private Location location;
    private int distance;
    //Views
    private TextView username, description, numberOfLikes, distanceView;
    private ImageView profilePicture, postImage;
    private Button followButton;
    private ImageButton likeButton;
    private TextView date;

    private PostFragment(Post post, Location location, User user, int distance) {
        this.post = post;
        this.location = location;
        this.user = user;
        this.distance = distance;
    }

    public static PostFragment newInstance(Post post, Location location, User user, int distance) {
        return new PostFragment(post, location, user, distance);
    }


    private void getViews(View parent) {
        username = parent.findViewById(R.id.postFragmentUsername);
        description = parent.findViewById(R.id.postFragmentDescription);
        numberOfLikes = parent.findViewById(R.id.postFragmentNumberOfLikes);
        profilePicture = parent.findViewById(R.id.postFragmentProfilePicture);
        postImage = parent.findViewById(R.id.postFragmentPostImage);
        followButton = parent.findViewById(R.id.postFragmentFollowButton);
        likeButton = parent.findViewById(R.id.postFragmentLikeButton);
        distanceView = parent.findViewById(R.id.postFragmentDistanceText);
        date = parent.findViewById(R.id.postDateTime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        getViews(view);
        final User currentUser = LoggedInUser.getInstance();
        final String currentUserId = currentUser.getId();
        database = DependencyManager.getDatabaseSystem();

        if (post.getType() != null && post.getType().equals(Post.EVENT)) {
            likeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_event_red_24dp));
            date.setVisibility(View.VISIBLE);
        }

        username.setText(user.getUsername());
        distanceView.setText(distance + " m");


        if (user.getImageURL() != null) {
            profilePicture.setTag(PROFILE_PICTURE_IMAGE);
            Picasso.get().load(user.getImageURL()).into(profilePicture);
        }

        description.setText(post.getDescription());
        if (user.getId().equals(currentUserId)) {
            followButton.setVisibility(View.GONE);
        } else {
            database.userIdFollowingList(currentUserId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if ((task.getResult() == null) || (!task.getResult().contains(user.getId()))) {
                        followButton.setText(R.string.followBtnNotFollowing);
                    } else {
                        followButton.setText(R.string.followBtnAlreadyFollowing);
                    }
                }
            });
            followButton.setOnClickListener(followButtonListener(currentUser));
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date.setText((dateFormat.format(post.getDate())));

        numberOfLikes.setText(String.valueOf(post.getLikes()));

        if (post.getType()!=null && post.getType().equals(Post.EVENT) || distance <= 30) {
            likeButton.setOnClickListener(v -> {

                if (post.getLikers().contains(currentUserId)) {
                    database.unlikePost(currentUserId, post.getPostId()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            post.unlikePost(currentUserId);
                            numberOfLikes.setText(String.valueOf(post.getLikes()));
                        } else {
                            Toast.makeText(PostFragment.this.getContext(), R.string.no_connexion, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    database.likePost(currentUserId, post.getPostId()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            post.likePost(currentUserId);
                            numberOfLikes.setText(String.valueOf(post.getLikes()));
                        }
                    });
                }
            });
        } else {
            distanceView.setAlpha((float) 0.5);
            likeButton.setAlpha((float) 0.5);
            likeButton.setOnClickListener(v -> Toast.makeText(PostFragment.this.getContext(), "You are too far away from the post to like it", Toast.LENGTH_SHORT).show());
        }

        if (post.getImageURL() != null) {
            postImage.setVisibility(View.VISIBLE);
            postImage.setTag(POST_IMAGE);
            Picasso.get().load(post.getImageURL()).into(postImage);
        }

        if (LoggedInUser.isGuestMode()) {
            likeButton.setClickable(false);
            followButton.setVisibility(View.GONE);
        }

        return view;
    }

    private View.OnClickListener followButtonListener(User currUser) {
        return v -> {
            System.out.println(followButton.getText().toString());
            if (followButton.getText().toString().equals(getString(R.string.followBtnNotFollowing))) {
                database.follow(currUser.getId(), user.getId()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followButton.setText(R.string.followBtnAlreadyFollowing);
                    }
                });
            } else {
                database.unFollow(currUser.getId(), user.getId()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followButton.setText(R.string.followBtnNotFollowing);
                    }
                });
            }
        };
    }
}
