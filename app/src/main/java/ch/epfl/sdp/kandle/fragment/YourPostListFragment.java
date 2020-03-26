package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.epfl.sdp.kandle.ClickListener;
import ch.epfl.sdp.kandle.Post;
import ch.epfl.sdp.kandle.PostAdapter;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class YourPostListFragment extends Fragment {


    private String userId;
    private List<Post> posts;

    private Authentication auth;
    private Database database;

    private ImageButton mDeleteButton;

    private RecyclerView rvPosts;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    public static YourPostListFragment newInstance() {
        return new YourPostListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();

        userId = auth.getCurrentUser().getUid();

        database.getPostsByUserId(userId).addOnCompleteListener(new OnCompleteListener<List<Post>>() {
            @Override


            public void onComplete(@NonNull Task<List<Post>> task) {

                if (task.isSuccessful()){

                    if (task.getResult()!=null){
                        posts= new ArrayList<>(task.getResult());
                        //reverse to have the newer posts first
                        Collections.reverse(posts);
                    }

                    else {
                        posts = new ArrayList<Post>();
                    }

                    PostAdapter adapter = new PostAdapter(posts);

                    adapter.setOnItemClickListener((position, view) -> {
                        LayoutInflater inflater1 = getLayoutInflater();
                        View popupView = inflater1.inflate(R.layout.post_content, null);
                        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        boolean focusable = true; // lets taps ouside the popup also dismiss it
                        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                        TextView content = popupView.findViewById(R.id.post_content);
                        content.setText(posts.get(position).getDescription());

                        popupView.setOnClickListener((popup) -> {
                            popupWindow.dismiss();
                        });

                    });

                    // Attach the adapter to the recyclerview to populate items
                    rvPosts.setAdapter(adapter);
                    // Set layout manager to position the items

                }

                else {
                    System.out.println(task.getException().getMessage());
                }

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
