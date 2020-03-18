package ch.epfl.sdp.kandle;

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

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostFragment extends Fragment {


    private RecyclerView rvPosts;
    private ArrayList<Post> posts = new ArrayList<>(0); //From user
    private PostAdapter adapter = new PostAdapter(posts);

    private ImageButton mlikeButton;
    private boolean alreadyLiked = false;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.post_fragment, container, false);
        rvPosts = rootView.findViewById(R.id.rvPosts);

        posts.add(new Post("Text", "( : this is my post : )", new Date()));

        adapter.setOnItemClickListener(new ClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                LayoutInflater inflater = getLayoutInflater();
                View popupView = inflater.inflate(R.layout.post_content, null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                TextView content = popupView.findViewById(R.id.post_content);
                content.setText(posts.get(position).getDescription());

                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

            }
        });
        // Attach the adapter to the recyclerview to populate items
        rvPosts.setAdapter(adapter);
        // Set layout manager to position the items
        rvPosts.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return rootView;
    }


    public ArrayList<Post> getPostList() {
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


}
