package ch.epfl.sdp.kandle.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ch.epfl.sdp.kandle.MockInstances.Authentication;
import ch.epfl.sdp.kandle.MockInstances.AuthenticationUser;
import ch.epfl.sdp.kandle.MockInstances.Database;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;
import ch.epfl.sdp.kandle.UserAdapter;

public class SearchFragment extends Fragment {

   // private FirebaseAuth fAuth;

    //private FirebaseDatabase fData;

    private Authentication auth;
    private Database database;

    private RecyclerView mRecyclerView;

    private ArrayList<User> mUsers = new ArrayList<>(0);
    private UserAdapter userAdapter = new UserAdapter(mUsers);

    EditText search_bar;

    // private EditText mSearchText;

    public SearchFragment( ){

    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        auth=Authentication.getAuthenticationSystem();
        database = Database.getDatabaseSystem();

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        //Button postButton = view.findViewById(R.id.postButton);
        //postButton.setVisibility(View.GONE);

        mRecyclerView = view.findViewById(R.id.recycler_view);

        search_bar = view.findViewById(R.id.search_bar);


        final AuthenticationUser authenticationUser = auth.getCurrentUser();

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


                if (!charSequence.toString().isEmpty()) {

                    database.searchUsers(charSequence.toString().toLowerCase().replace(" ", ""), 20).addOnCompleteListener(new OnCompleteListener<List<User>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<User>> task) {

                            if (task.isSuccessful()){

                                mUsers.clear();

                                System.out.println("success");
                                System.out.println(task.getResult().size());

                                for (User user : task.getResult()){
                                    if (!user.getId().equals(authenticationUser.getUid())){
                                        mUsers.add(user);
                                    }
                                }

                                userAdapter.notifyDataSetChanged();
                            }

                            else {
                                System.out.println(task.getException().getMessage());
                            }
                        }
                    });

                }
                else {
                    mUsers.clear();
                    userAdapter.notifyDataSetChanged();
                }


                /*

                if (!charSequence.toString().isEmpty()) {

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
                                user.setUsername(user.getFullname());
                                if (!firebaseUser.getUid().equals(user.getId()))
                                    mUsers.add(user);
                            }

                            userAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                else {
                    mUsers.clear();
                    userAdapter.notifyDataSetChanged();
                }

                 */


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();

        userAdapter.setOnItemClickListener(new UserAdapter.ClickListener(){


            @Override
            public void onItemClick(int position, View v) {


                //closeKeyBoard
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                final User user = mUsers.get(position);

                fragmentManager.beginTransaction().replace(R.id.flContent, ProfileFragment.newInstance(user) ).commit();

                /*
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
                */

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
