package ch.epfl.sdp.kandle.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.UserAdapter;

public class SearchFragment extends Fragment {

    private RecyclerView mRecyclerView;

    private ArrayList<User> mUsers = new ArrayList<>(0);
    private UserAdapter userAdapter = new UserAdapter(mUsers);
   // private EditText mSearchText;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

       // mUsers.add(new User("45","full", "email" ));
        //final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        User user = snapshot.getValue(User.class);

                        if (!firebaseUser.getUid().equals(user.getId()))
                        mUsers.add(user);
                        //System.out.println("check");

                    }

                userAdapter.notifyDataSetChanged();

                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        userAdapter.setOnItemClickListener(new UserAdapter.ClickListener(){

            @Override
            public void onItemClick(int position, View v) {


                final User user = mUsers.get(position);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("Follow").child(firebaseUser.getUid()).child("following");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(user.getId()).exists()){
                            Toast.makeText(getContext(), "Visiting profile...",  Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(getContext(), "You must follow before you can visit the profile",  Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });



        mRecyclerView.setAdapter(userAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return view;
    }

    public ArrayList<User> getUserList() {
        return mUsers;
    }

    public void putInUserList(User u) {
        mUsers.add(u);
        userAdapter.notifyDataSetChanged();
    }

    public void removeUser(User u){
        mUsers.remove(u);
        userAdapter.notifyDataSetChanged();
    }

}
