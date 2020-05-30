package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ch.epfl.sdp.kandle.entities.post.Post;
import ch.epfl.sdp.kandle.entities.post.PostAdapter;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class EventListFragment extends Fragment {

    private RecyclerView mEvents;

    public static EventListFragment newInstance() { return new EventListFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Database database = new CachedFirestoreDatabase();

        database.getNearbyPosts(0,0, Double.MAX_VALUE).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                List<Post> events = new ArrayList<>();

                if (task.getResult() != null) {
                    List<Post> allPosts = new ArrayList<>(task.getResult());
                    for (Post p : allPosts) {
                        if (p.getType() != null && p.getType().equals(Post.EVENT) &&
                            p.getLikers().contains(DependencyManager.getAuthSystem().getCurrentUser().getId()) &&
                            p.getDate().getTime() > new Date().getTime()) {
                            events.add(p);
                        }
                    }
                    Collections.reverse(events);
                }

                PostAdapter adapter = new PostAdapter(events, this.getContext());

                mEvents.setAdapter(adapter);
            }
        });

        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);
        mEvents = rootView.findViewById(R.id.events);
        mEvents.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return rootView;
    }

}
