package ch.epfl.sdp.kandle.entities.user;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.authentification.Authentication;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public final static int PROFILE_PICTURE_TAG = 9;
    private static ClickListener clickListener;
    private List<User> mUsers;
    public UserAdapter(List<User> mUsers) {
        this.mUsers = mUsers;
    }
    private boolean isFollowerList = false;

    public void setOnItemClickListener(ClickListener clickListener) {
        UserAdapter.clickListener = clickListener;
    }

    public void setIsFollowersList(boolean isFollowerList){
        this.isFollowerList = isFollowerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View userView = inflater.inflate(R.layout.search_user, parent, false);
        ViewHolder viewHolder = new ViewHolder(userView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final User user = mUsers.get(position);
        TextView mFullname = holder.mNickname;
        mFullname.setText(user.getNickname());

        TextView mUsername = holder.mUsername;
        mUsername.setText("@" + user.getUsername());

        CircleImageView mImageProfile = holder.image_profile;
        if (user.getImageURL() != null) {
            mImageProfile.setBackgroundColor(Color.TRANSPARENT);
            mImageProfile.setTag(PROFILE_PICTURE_TAG);
            File image = DependencyManager.getInternalStorageSystem().getImageFileById(user.getId());
            if (image != null) {
                System.out.println("Fetched from internal storage in UserAdatper");
                Picasso.get().load(image).into(mImageProfile);
            } else {
                Picasso.get().load(user.getImageURL()).into(mImageProfile);
            }
        }
        else{
            mImageProfile.setImageDrawable(Kandle.getContext().getDrawable(R.drawable.ic_launcher_foreground));
            mImageProfile.setBackground(Kandle.getContext().getDrawable(R.drawable.ic_launcher_circle_background));
        }
        final Authentication authentication = DependencyManager.getAuthSystem();
        final User currentUser = authentication.getCurrentUser();
        final Database database =  DependencyManager.getCachedDatabase();

        if(isFollowerList){
            holder.mIsCloseFriend.setVisibility(View.VISIBLE);
            database.userCloseFollowersList(currentUser.getId()).addOnCompleteListener(task -> {
               boolean found_user = false;
               if(task.isSuccessful()){
                   for(User user1 : task.getResult()){
                       if(user1.getId().equals(user.getId())) {
                           holder.mIsCloseFriend.setBackground(Kandle.getContext().getDrawable(R.drawable.button_background));
                           holder.mIsCloseFriend.setContentDescription("Is close friend");
                           found_user = true;
                       }
                   }
                   if(!found_user) {
                       holder.mIsCloseFriend.setBackground(Kandle.getContext().getDrawable(R.drawable.add_background_grey));
                       holder.mIsCloseFriend.setContentDescription("Is not close friend");
                   }
               }
            });
            holder.mIsCloseFriend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    database.userCloseFollowersList(currentUser.getId()).addOnCompleteListener(task -> {
                        boolean found_user = false;
                        if(task.isSuccessful()){
                            for(User user1 : task.getResult()){
                                if(user1.getId().equals(user.getId())) {
                                    holder.mIsCloseFriend.setBackground(Kandle.getContext().getDrawable(R.drawable.add_background_grey));
                                    holder.mIsCloseFriend.setContentDescription("Is not close friend");
                                    database.unsetCloseFollower(user.getId(), currentUser.getId());
                                    found_user = true;
                                }
                            }
                            if(!found_user) {
                                holder.mIsCloseFriend.setBackground(Kandle.getContext().getDrawable(R.drawable.button_background));
                                holder.mIsCloseFriend.setContentDescription("Is close friend");
                                database.setCloseFollower(user.getId(), currentUser.getId());
                            }
                        }
                        else{
                            Toast.makeText(Kandle.getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
        else{
            holder.mIsCloseFriend.setVisibility(View.GONE);
        }
        Log.i("TAG", Thread.currentThread().getName());
        if (user.getId().equals(currentUser.getId())) {
            holder.mFollowBtn.setVisibility(View.GONE);
        } else {

            database.userIdFollowingList(currentUser.getId()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if ((task.getResult() == null) || (!task.getResult().contains(user.getId()))) {
                        holder.mFollowBtn.setText("follow");
                    } else {
                        holder.mFollowBtn.setText("following");
                    }

                }

            });


            holder.mFollowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.mFollowBtn.getText().toString().equals("follow")) {

                        database.follow(currentUser.getId(), user.getId()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                holder.mFollowBtn.setText("following");
                            }
                        });

                    } else {

                        database.unFollow(currentUser.getId(), user.getId()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                holder.mFollowBtn.setText("follow");
                            }

                        });

                    }
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void notifyDataChange(List<User> mUsers) {
        this.mUsers = mUsers;
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mNickname;
        public TextView mUsername;
        public CircleImageView image_profile;
        public Button mFollowBtn;
        public ImageButton mIsCloseFriend;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mNickname = itemView.findViewById(R.id.search_fullName);
            mUsername = itemView.findViewById(R.id.search_username);
            image_profile = itemView.findViewById(R.id.search_image_user);
            mFollowBtn = itemView.findViewById(R.id.btn_follow);
            mIsCloseFriend = itemView.findViewById(R.id.userCloseFriends);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

}