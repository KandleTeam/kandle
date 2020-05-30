package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.entities.user.UserAdapter;
import ch.epfl.sdp.kandle.authentification.Authentication;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class PopularUserFragment extends Fragment {

    public final static int USERS_ARRAY_SIZE = 5;
    TextView mTitle, mNumber;
    private Authentication auth;
    private Database database;
    private RecyclerView mRecyclerView;
    private HashMap<User, Integer> mUsersnbFollowers;
    private List<User> mUsers;
    private UserAdapter userAdapter = new UserAdapter(mUsers);
    private User currentUser;


    public PopularUserFragment() {
        this.mUsersnbFollowers = new HashMap<>();
        this.mUsers = new ArrayList<>();
        this.userAdapter = new UserAdapter(mUsers);

    }

    public static PopularUserFragment newInstance() {
        return new PopularUserFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        auth = DependencyManager.getAuthSystem();
        database = new CachedFirestoreDatabase();

        View view = inflater.inflate(R.layout.fragment_list_users, container, false);

        mNumber = view.findViewById(R.id.list_user_number);
        mTitle = view.findViewById(R.id.list_user_title);
        mTitle.setText("");
        mNumber.setText("");
        mRecyclerView = view.findViewById(R.id.list_user_recycler_view);
        currentUser = auth.getCurrentUser();

        mRecyclerView.setAdapter(userAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        mUsersnbFollowers.clear();
        database.usersList().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (User user : task.getResult()) {
                    database.userFollowersList(user.getId()).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            mUsersnbFollowers.put(user, task1.getResult().size());
                            List<Map.Entry<User, Integer>> listOfEntries = new ArrayList<>(mUsersnbFollowers.entrySet());
                            Collections.sort(listOfEntries, valueComparator);
                            mUsersnbFollowers = new HashMap<>(listOfEntries.size());
                            for (Map.Entry<User, Integer> entry : listOfEntries) {
                                mUsersnbFollowers.put(entry.getKey(), entry.getValue());
                            }
                            List<User> temp = new ArrayList<>();
                            for (int i = 0; i < Math.min(USERS_ARRAY_SIZE, listOfEntries.size()); i++) {
                                if (listOfEntries.get(i) != null) {
                                    temp.add(listOfEntries.get(i).getKey());
                                }
                            }
                            this.mUsers = temp;
                            userAdapter.notifyDataChange(mUsers);
                        }
                    });
                }
            }
        });


        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
        userAdapter.setOnItemClickListener((position, v) -> {
            final User user = mUsers.get(position);
            fragmentManager.beginTransaction().replace(R.id.flContent, ProfileFragment.newInstance(user))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        });
        return view;
    }


    Comparator<Map.Entry<User, Integer>> valueComparator = new Comparator<Map.Entry<User, Integer>>() {

        @Override
        public int compare(Map.Entry<User, Integer> e1, Map.Entry<User, Integer> e2) {
            Integer v1 = e1.getValue();
            Integer v2 = e2.getValue();
            return v2.compareTo(v1);
        }
    };



}

