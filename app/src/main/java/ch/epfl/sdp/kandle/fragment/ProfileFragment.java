package ch.epfl.sdp.kandle.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.util.List;

import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.AuthenticationUser;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.User;


public class ProfileFragment extends Fragment {

    User user;

    ImageView mProfilePicture;
    TextView mNumberOfFollowers, mNumberOfFollowing, mUsername;
    Button mFollowButton;
    Authentication auth;
    Database database;

    public final static int PROFILE_PICTURE_TAG = 6;

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

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();


        final AuthenticationUser authenticationUser = auth.getCurrentUser();

        mProfilePicture = view.findViewById(R.id.profilePicture);
        mNumberOfFollowers = view.findViewById(R.id.profileNumberOfFollowers);
        mNumberOfFollowing = view.findViewById(R.id.profileNumberOfFollowing);
        mUsername = view.findViewById(R.id.profileUsername);
        mFollowButton = view.findViewById(R.id.profileFollowButton);


        mUsername.setText(user.getUsername());
        if(user.getImageURL() != null) {
            mProfilePicture.setTag(PROFILE_PICTURE_TAG);
            Picasso.get().load(user.getImageURL()).into(mProfilePicture);
        }

        setNumberOfFollowers();
        setNumberOfFollowing();







        database.followingList(authenticationUser.getUid()).addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {

                if (task.isSuccessful()){

                    if (   (task.getResult() == null) || (!task.getResult().contains(user.getId()))   ){
                        mFollowButton.setText("follow");
                    }

                    else {
                        mFollowButton.setText("following");
                    }

                }
              /*  else {
                    System.out.println(task.getException().getMessage());
                }*/

            }
        });


        /*
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
         */

        mFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFollowButton.getText().toString().equals("follow")){

                    database.follow(authenticationUser.getUid(), user.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFollowButton.setText("following");
                                mFollowButton.setText("following");
                                setNumberOfFollowers();
                            }

                          /*  else {
                                System.out.println(task.getException().getMessage());
                            }*/

                        }
                    });


                    /*FirebaseDatabase.getInstance().getReference().child("Follow").child(authenticationUser.getUid())
                            .child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(authenticationUser.getUid()).setValue(true);

                     */



                }
                else {


                    database.unFollow(authenticationUser.getUid(), user.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                setNumberOfFollowers();
                                mFollowButton.setText("follow");
                                mFollowButton.setText("follow");
                            }

                            /*else {
                                System.out.println(task.getException().getMessage());
                            }*/
                        }
                    });
                    /*
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(authenticationUser.getUid())
                            .child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(authenticationUser.getUid()).removeValue();

                     */

                }
            }
        });


        return view;
    }


    private void setNumberOfFollowing() {
        database.followingList(user.getId()).addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        mNumberOfFollowing.setText(  Integer.toString(task.getResult().size()));
                    }
                }
               /* else {
                    System.out.println(task.getException().getMessage());
                }*/
            }
        });
    }

    private void setNumberOfFollowers(){
        database.followersList(user.getId()).addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        mNumberOfFollowers.setText(  Integer.toString(task.getResult().size()));
                    }
                }
                /*else {
                    System.out.println(task.getException().getMessage());
                }*/
            }
        });
    }
}
