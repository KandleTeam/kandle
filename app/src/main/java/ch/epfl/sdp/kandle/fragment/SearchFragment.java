package ch.epfl.sdp.kandle.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.entities.user.UserAdapter;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;


public class SearchFragment extends Fragment {

    private static final int MAX_SEARCH_RESULTS = 20;
    private Database database;
    private ArrayList<User> mUsers = new ArrayList<>(0);
    private UserAdapter userAdapter = new UserAdapter(mUsers);
    private User currentUser;

    public SearchFragment() {

    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        database = new CachedFirestoreDatabase();
        currentUser = DependencyManager.getAuthSystem().getCurrentUser();

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView mRecyclerView = view.findViewById(R.id.recycler_view);
        EditText searchBar = view.findViewById(R.id.search_bar);
        mRecyclerView.setAdapter(userAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        giveFollowPropositions();
        searchBar.addTextChangedListener(searchBarWatcher());

        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();

        userAdapter.setOnItemClickListener((position, v) -> {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            final User user = mUsers.get(position);
            fragmentManager.beginTransaction().replace(R.id.flContent, ProfileFragment.newInstance(user))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }


    private TextWatcher searchBarWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String normalized = charSequence.toString().toLowerCase().replace(" ", "");
                if (!normalized.isEmpty()) {
                    database.searchUsers(normalized, MAX_SEARCH_RESULTS).addOnSuccessListener(results -> {
                        mUsers.clear();
                        for (User user : results) {
                            if (!user.getId().equals(currentUser.getId())) {
                                mUsers.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();

                    });
                } else {
                    mUsers.clear();
                    giveFollowPropositions();
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }


    private void giveFollowPropositions() {
        database.userFollowingList(currentUser.getId()).addOnSuccessListener(userFollowingList -> {
            for (User follower : userFollowingList) {
                database.userFollowingList(follower.getId()).addOnSuccessListener(followerFollowingList -> {
                    for (User follower2ndDegree : followerFollowingList) {
                        boolean alreadyFollowed;
                        if (mUsers.isEmpty()) {
                            alreadyFollowed = isUserInList(userFollowingList, follower2ndDegree);
                            if (!alreadyFollowed && !follower2ndDegree.getId().equals(currentUser.getId())) {
                                mUsers.add(follower2ndDegree);
                            }
                        } else {
                            boolean alreadyInList = isUserInList(mUsers, follower2ndDegree);
                            alreadyFollowed = isUserInList(userFollowingList, follower2ndDegree);
                            if (!alreadyFollowed && !alreadyInList && !follower2ndDegree.getId().equals(currentUser.getId())) {
                                mUsers.add(follower2ndDegree);
                            }
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                });
            }
        });
    }


    private boolean isUserInList(List<User> list, User user) {
        for (User user1 : list) {
            if (user.getId().equals(user1.getId())) {
                return true;
            }
        }
        return false;
    }
}