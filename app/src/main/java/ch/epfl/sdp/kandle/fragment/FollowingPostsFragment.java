package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.authentication.Authentication;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.post.PostAdapter;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class FollowingPostsFragment extends Fragment {

    //TODO should not be able to delete posts other users made

    View rootView;
    private String currentUserId;
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
        currentUserId = LoggedInUser.getInstance().getId();
        PostAdapter adapter = new PostAdapter(posts, this.getContext());

        database.userFollowingList(currentUserId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    following = task.getResult();
                    if (!following.isEmpty()) {
                        for (User user : following) {
                            database.getPostsByUserId(user.getId()).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    if (task1.getResult() != null) {
                                        database.userCloseFollowersList(user.getId()).addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                boolean isCloseFollower = false;
                                                if (task2.getResult() != null) {
                                                    System.out.println("IT WORKS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   " + task2.getResult().size());
                                                    for (User user1 : task2.getResult()) {
                                                        if (user1.getId().equals(currentUserId)) {
                                                            posts.addAll(task1.getResult());
                                                            isCloseFollower = true;
                                                        }
                                                    }


                                                }
                                                if (!isCloseFollower) {
                                                    for (Post post : task1.getResult()) {
                                                        if (post.getIsForCloseFollowers() == null || (post.getIsForCloseFollowers() != null && post.getIsForCloseFollowers().equals(Post.NOT_CLOSE_FOLLOWER))) {
                                                            posts.add(post);
                                                        }
                                                    }
                                                }
                                                adapter.setPost(posts);
                                            } else {
                                                System.out.println(task2.getException().getMessage());
                                            }
                                        });
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