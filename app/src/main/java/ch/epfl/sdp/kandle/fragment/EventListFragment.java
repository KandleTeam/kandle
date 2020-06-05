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
import java.util.Comparator;
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

        Database database = DependencyManager.getCachedDatabase();

        database.getParticipatingEvents().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()!=null) {
                List<Post> events = new ArrayList<>(task.getResult());
                for (int i = events.size() - 1; i>=0; i--){
                    if (events.get(i).getDate().getTime() < new Date().getTime()) {
                        events.remove(i);
                    }
                }
                //sort to have closest events first
                Collections.sort(events, (e1, e2) -> Long.compare(e1.getDate().getTime(), e2.getDate().getTime()));

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
