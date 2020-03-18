package ch.epfl.sdp.kandle.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ch.epfl.sdp.kandle.MockInstances.Authentication;
import ch.epfl.sdp.kandle.MockInstances.AuthenticationUser;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;


public class ProfileFragment extends Fragment {

    User user;

    ImageView mProfilePicture;
    TextView mNumberOfFollowers, mNumberOfFollowing, mUsername;
    Button mFollowButton;
    Authentication auth;


    public ProfileFragment (User user){
        this.user = user;
    }


    public static ProfileFragment newInstance(User user) {
        return new ProfileFragment(user);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth=Authentication.getAuthenticationSystem();

        final AuthenticationUser authenticationUser = auth.getCurrentUser();

        mProfilePicture = view.findViewById(R.id.profilePicture);
        mNumberOfFollowers = view.findViewById(R.id.profileNumberOfFollowers);
        mNumberOfFollowing = view.findViewById(R.id.profileNumberOfFollowing);
        mUsername = view.findViewById(R.id.profileUsername);
        mFollowButton = view.findViewById(R.id.profileFollowButton);


        mUsername.setText(user.getUsername());


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(authenticationUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(user.getId()).exists()){
                    mFollowButton.setText("following");
                } else{
                    mFollowButton.setText("follow");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFollowButton.getText().toString().equals("follow")){

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(authenticationUser.getUid())
                            .child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(authenticationUser.getUid()).setValue(true);


                    mFollowButton.setText("following");
                }
                else {

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(authenticationUser.getUid())
                            .child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(authenticationUser.getUid()).removeValue();
                    mFollowButton.setText("follow");
                }
            }
        });


        return view;
    }
}
