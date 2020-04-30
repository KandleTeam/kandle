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

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.PostAdapter;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class FollowingPostsFragment extends Fragment {

    //TODO should not be able to delete posts other users made

    View rootView;
    private String userId;
    private List<User> following;
    private List<Post> posts;
    private RecyclerView flPosts;
    private Authentication auth;
    private Database database;

    public FollowingPostsFragment() {
        posts = new ArrayList<>();
        following = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        auth = DependencyManager.getAuthSystem();
        database = new CachedFirestoreDatabase();
        rootView = inflater.inflate(R.layout.fragment_following_posts, container, false);
        flPosts = rootView.findViewById(R.id.flPosts);
        flPosts.setLayoutManager(new LinearLayoutManager(this.getContext()));
        userId = LoggedInUser.getInstance().getId();
        PostAdapter adapter = new PostAdapter(posts, this.getContext());

        database.userFollowingList(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    following = task.getResult();
                    if (!following.isEmpty()) {
                        for (User user : following) {
                            database.getPostsByUserId(user.getId()).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    if (task1.getResult() != null) {
                                        posts.addAll(task1.getResult());
                                        adapter.setPost(posts);
                                    }
                                } else {
                                    System.out.println(task1.getException().getMessage());
                                }
                            });
                        }
                    }

                }
            } else {
                System.out.println(task.getException().getMessage());
            }

        });


        flPosts.setAdapter(adapter);
        return rootView;
    }

}