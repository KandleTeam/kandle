package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.post.PostAdapter;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class YourPostListFragment extends Fragment {


    public static final int POST_IMAGE = 10;
    private List<Post> posts;

    private RecyclerView rvPosts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Database database = new CachedFirestoreDatabase();
        String userId = DependencyManager.getAuthSystem().getCurrentUser().getId();

        database.getPostsByUserId(userId).addOnSuccessListener(posts -> {

            if (posts != null) {
                posts = new ArrayList<>(posts);
                //sort to have the newer posts first
                Collections.sort(posts, (p1, p2) -> Long.compare(p2.getDate().getTime(), p1.getDate().getTime()));
            } else {
                posts = new ArrayList<>();
            }

            PostAdapter adapter = new PostAdapter(posts, getContext());

            rvPosts.setAdapter(adapter);

        });

        View rootView = inflater.inflate(R.layout.fragment_your_post_list, container, false);
        rvPosts = rootView.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return rootView;
    }

}
