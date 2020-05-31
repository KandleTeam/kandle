package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.post.PostAdapter;
import ch.epfl.sdp.kandle.R;


public class LandmarkFragment extends Fragment {


    private String title;
    private String imageUri;
    private List<Post> posts;
    private PostAdapter postAdapter;

    private TextView titleView;
    private RecyclerView postsListView;
    private ImageView imageLandmark;

    public final static int LANDMARK_IMAGE = 31;

    public LandmarkFragment( String title, String imageUri, List<Post> posts) {
        this.title = title;
        this.imageUri = imageUri;
        System.out.println(posts.size());
        if (posts.size()>5) {
            this.posts = posts.subList(0, 5);
        }else {
            this.posts = posts;
        }
    }


    private void getViews(View view) {
        titleView = view.findViewById(R.id.landmarkFragmentTitle);
        postsListView = view.findViewById(R.id.landmarkFragmentPostsList);
        imageLandmark = view.findViewById(R.id.landmarkFragmentImage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_landmark, container, false);
        getViews(view);

        titleView.setText(title);

        if (imageUri!= null) {
            imageLandmark.setTag(LANDMARK_IMAGE);
            Picasso.get().load(imageUri).into(imageLandmark);
        }


        this.postAdapter = new PostAdapter(posts, this.getContext());
        postsListView.setAdapter(postAdapter);
        postsListView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return view;
    }


}