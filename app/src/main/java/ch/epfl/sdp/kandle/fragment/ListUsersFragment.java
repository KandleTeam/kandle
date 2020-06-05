package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.entities.user.User;
import ch.epfl.sdp.kandle.entities.user.UserAdapter;

public class ListUsersFragment extends Fragment {

    //attributes
    private List<User> users;
    private String title, number;
    private UserAdapter userAdapter;

    //views
    private RecyclerView mRecyclerView;
    private TextView mTitle, mNumber;

    private ListUsersFragment(List<User> users, String title, String number) {
        this.users = new ArrayList<>(users);
        this.number = number;
        this.title = title;
        this.userAdapter = new UserAdapter(this.users);
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

        if (mTitle.getText().equals("Followers"))
            userAdapter.setIsFollowersList(true);

        mRecyclerView.setAdapter(userAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();

        userAdapter.setOnItemClickListener((position, v) -> {

            final User user = users.get(position);

            fragmentManager.beginTransaction().replace(R.id.flContent, ProfileFragment.newInstance(user))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

}
