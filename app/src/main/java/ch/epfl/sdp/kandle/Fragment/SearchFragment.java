package ch.epfl.sdp.kandle.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

    private FirebaseAuth fAuth;

    private FirebaseDatabase fData;

    private RecyclerView mRecyclerView;

    private ArrayList<User> mUsers = new ArrayList<>(0);
    private UserAdapter userAdapter = new UserAdapter(mUsers);

    EditText search_bar;

    // private EditText mSearchText;

    public SearchFragment( FirebaseAuth fAuth, FirebaseDatabase fData){
        this.fAuth=fAuth;
        this.fData=fData;
    }

    public static SearchFragment newInstance( FirebaseAuth fAuth, FirebaseDatabase fData) {
        return new SearchFragment(fAuth, fData);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {




        View view = inflater.inflate(R.layout.fragment_search, container, false);

        //Button postButton = view.findViewById(R.id.postButton);
        //postButton.setVisibility(View.GONE);

        mRecyclerView = view.findViewById(R.id.recycler_view);

        search_bar = view.findViewById(R.id.search_bar);


        final FirebaseUser firebaseUser = fAuth.getInstance().getCurrentUser();

        mRecyclerView.setAdapter(userAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

/*
        DatabaseReference reference = fData.getReference("Users");
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

 */

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                //if (!charSequence.toString().isEmpty()) {

                    String q = charSequence.toString().toLowerCase().replace(" ", "");
                    Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("fullnameSearch")
                            .startAt(q)
                            .endAt(q + "\uf8ff");



                    //System.out.println(charSequence.toString());

                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mUsers.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);

                                if (!firebaseUser.getUid().equals(user.getId()))
                                    mUsers.add(user);
                            }

                            userAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
               // }

               /* else {
                    mUsers.clear();
                    userAdapter.notifyDataSetChanged();
                }*/
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        userAdapter.setOnItemClickListener(new UserAdapter.ClickListener(){

            @Override
            public void onItemClick(int position, View v) {


                final User user = mUsers.get(position);

                DatabaseReference reference = fData.getReference()
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
