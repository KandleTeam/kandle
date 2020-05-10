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

import java.util.List;

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.UserAdapter;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;


public class ListUsersFragment extends Fragment {

    //attributes
    List<User> users;
    String title, number;
    UserAdapter userAdapter;

    //views
    RecyclerView mRecyclerView;
    TextView mTitle, mNumber;

    //dependencies
    Authentication auth;
    Database database;


    private ListUsersFragment(List<User> users, String title, String number) {
        this.users = users;
        this.number = number;
        this.title = title;
        this.userAdapter = new UserAdapter(users);

    }

    public static ListUsersFragment newInstance(List<User> users, String title, String number) {
        return new ListUsersFragment(users, title, number);
    }

    private void getViews(View parent) {
        mRecyclerView = parent.findViewById(R.id.list_user_recycler_view);
        mNumber = parent.findViewById(R.id.list_user_number);
        mTitle = parent.findViewById(R.id.list_user_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_users, container, false);

        getViews(view);


        mNumber.setText(number);
        mTitle.setText(title);

        mRecyclerView.setAdapter(userAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));


        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();

        userAdapter.setOnItemClickListener((int position, View v) -> {

            final User user = users.get(position);

            fragmentManager.beginTransaction().replace(R.id.flContent, ProfileFragment.newInstance(user))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }

}
