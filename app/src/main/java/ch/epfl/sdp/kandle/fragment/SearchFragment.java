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

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.UserAdapter;


public class SearchFragment extends Fragment {


    private Authentication auth;
    private Database database;
    private RecyclerView mRecyclerView;
    private ArrayList<User> mUsers = new ArrayList<>(0);
    private UserAdapter userAdapter = new UserAdapter(mUsers);
    private User currentUser;
    EditText search_bar;

    public SearchFragment() {

    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        auth = DependencyManager.getAuthSystem();
        database = new CachedFirestoreDatabase();

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        search_bar = view.findViewById(R.id.search_bar);
        currentUser = auth.getCurrentUser();

        mRecyclerView.setAdapter(userAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (!charSequence.toString().replace(" ", "").isEmpty()) {
                    database.searchUsers(charSequence.toString().toLowerCase().replace(" ", ""), 20).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mUsers.clear();
                            for (User user : task.getResult()) {
                                if (!user.getId().equals(currentUser.getId())) {
                                    mUsers.add(user);
                                }
                            }
                            userAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    mUsers.clear();
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();

        userAdapter.setOnItemClickListener((position, v) -> {

            //closeKeyBoard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            //if (getActivity().getCurrentFocus()!=null)
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            final User user = mUsers.get(position);
            fragmentManager.beginTransaction().replace(R.id.flContent, ProfileFragment.newInstance(user))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }


}
