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
import java.util.List;
import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.dependencies.Post;
import ch.epfl.sdp.kandle.PostAdapter;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

import static ch.epfl.sdp.kandle.fragment.YourPostListFragment.POST_IMAGE;

public class FollowingPostsFragment extends Fragment {

    //TODO should not be able to delete posts other users made

    private String userId;
    private List<User> following;
    private List<Post> posts;
    private RecyclerView flPosts;
    private Authentication auth;
    private Database database;
    private Context context;
    PostAdapter adapter;
    View rootView;

    public FollowingPostsFragment() {
        posts = new ArrayList<>();
        following = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();
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
            System.out.println("Before " + posts.get(position).getPostId());
            if (posts.get(position).getImageURL() != null) {
                image.setVisibility(View.VISIBLE);
                image.setTag(POST_IMAGE);
                System.out.println("In if" + image.getTag());
                Picasso.get().load(posts.get(position).getImageURL()).into(image);
            }

            popupView.setOnClickListener((popup) -> {
                popupWindow.dismiss();
            });
        });
        flPosts.setAdapter(adapter);
        return rootView;
    }

}