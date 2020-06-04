package ch.epfl.sdp.kandle.fragment;

import android.content.Context;
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
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.post.PostAdapter;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.authentification.Authentication;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class YourPostListFragment extends Fragment {


    public static final int POST_IMAGE = 10;
    private String userId;
    private List<Post> posts;
    private Authentication auth;
    private Database database;

    private RecyclerView rvPosts;

    public static YourPostListFragment newInstance() {
        return new YourPostListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getCachedDatabase();


        userId = auth.getCurrentUser().getId();

        Context context = this.getContext();

        database.getPostsByUserId(userId).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                if (task.getResult() != null) {
                    posts = new ArrayList<>(task.getResult());
                    //reverse to have the newer posts first
                    Collections.reverse(posts);
                } else {
                    posts = new ArrayList<>();
                }

                PostAdapter adapter = new PostAdapter(posts, context);

                rvPosts.setAdapter(adapter);

            } else {
                System.err.println(task.getException().getMessage());
            }
        });


        View rootView = inflater.inflate(R.layout.fragment_your_post_list, container, false);
        rvPosts = rootView.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().setTitle(R.string.your_posts_item);
    }



/*
    public List<Post> getPostList() {
        return posts;
    }

    public void putInPostList(Post p) {
        posts.add(p);
        adapter.notifyDataSetChanged();
    }

    public void removePostAtIndex(int position){
        posts.remove(position);
        adapter.notifyDataSetChanged();
    }

    public void removePost(Post p){
        posts.remove(p);
        adapter.notifyDataSetChanged();
    }

 */


}
