package ch.epfl.sdp.kandle.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.PostAdapter;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
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
        database = new CachedFirestoreDatabase();


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


                adapter.setOnItemClickListener((position, view) -> {
                    LayoutInflater inflater1 = getLayoutInflater();
                    View popupView = inflater1.inflate(R.layout.post_content, null);
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = true; // lets taps ouside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                    TextView content = popupView.findViewById(R.id.post_content);
                    ImageView image = popupView.findViewById(R.id.postImage);
                    content.setText(posts.get(position).getDescription());

                    if (posts.get(position).getImageURL() != null) {
                        image.setVisibility(View.VISIBLE);
                        image.setTag(POST_IMAGE);
                        Picasso.get().load(posts.get(position).getImageURL()).into(image);
                    }

                    popupView.setOnClickListener((popup) -> {
                        popupWindow.dismiss();
                    });

                });

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
