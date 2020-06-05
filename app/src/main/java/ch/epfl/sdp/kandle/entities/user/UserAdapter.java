package ch.epfl.sdp.kandle.entities.user;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public final static int PROFILE_PICTURE_TAG = 9;
    private static ClickListener clickListener;
    private List<User> mUsers;
    private boolean isFollowerList = false;


    public UserAdapter(List<User> mUsers) {
        this.mUsers = mUsers;
    }


    public void setOnItemClickListener(ClickListener clickListener) {
        UserAdapter.clickListener = clickListener;
    }

    public void setIsFollowersList(boolean isFollowerList) {
        this.isFollowerList = isFollowerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View userView = inflater.inflate(R.layout.search_user, parent, false);
        return new ViewHolder(userView);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final User user = mUsers.get(position);
        TextView mFullname = holder.mNickname;
        mFullname.setText(user.getNickname());

        TextView mUsername = holder.mUsername;
        mUsername.setText(String.format("@%s", user.getUsername()));


        final User currentUser = DependencyManager.getAuthSystem().getCurrentUser();
        final CachedFirestoreDatabase database =(CachedFirestoreDatabase) DependencyManager.getCachedDatabase();

        setupUserImage(holder, user);

        setupCloseFriends(holder, user, currentUser, database);

        setupFollow(holder, user, currentUser, database);

    }

    private void setupUserImage(@NonNull ViewHolder holder, User user) {
        CircleImageView mImageProfile = holder.image_profile;
        if (user.getImageURL() != null) {
            mImageProfile.setBackgroundColor(Color.TRANSPARENT);
            mImageProfile.setTag(PROFILE_PICTURE_TAG);
            File image = DependencyManager.getInternalStorageSystem().getImageFileById(user.getId());
            if (image != null) {
                Picasso.get().load(image).into(mImageProfile);
            } else {
                Picasso.get().load(user.getImageURL()).into(mImageProfile);
            }
        } else {
            mImageProfile.setImageDrawable(Kandle.getContext().getDrawable(R.drawable.ic_launcher_foreground));
            mImageProfile.setBackground(Kandle.getContext().getDrawable(R.drawable.ic_launcher_circle_background));
        }
    }


    private void setupFollow(@NonNull ViewHolder holder, User user, User currentUser, CachedFirestoreDatabase database) {

        if (user.getId().equals(currentUser.getId())) {
            holder.mFollowBtn.setVisibility(View.GONE);
        } else {
            database.userIdFollowingList(currentUser.getId()).addOnSuccessListener(list -> {
                if ((list == null) || (!list.contains(user.getId()))) {
                    holder.mFollowBtn.setText(R.string.followBtnNotFollowing);
                } else {
                    holder.mFollowBtn.setText(R.string.followBtnAlreadyFollowing);
                }
            });


            holder.mFollowBtn.setOnClickListener(v -> {
                if (holder.mFollowBtn.getText().toString().equals(Kandle.getContext().getString(R.string.followBtnNotFollowing))) {
                    database.follow(currentUser.getId(), user.getId()).addOnSuccessListener(task -> {
                        holder.mFollowBtn.setText(R.string.followBtnAlreadyFollowing);


                    });
                } else {
                    database.unFollow(currentUser.getId(), user.getId()).addOnSuccessListener(task -> {
                        holder.mFollowBtn.setText(R.string.followBtnNotFollowing);
                    });
                }
            });
        }
    }

    private void setupCloseFriends(@NonNull ViewHolder holder, User user, User currentUser, CachedFirestoreDatabase database) {
        if (isFollowerList) {
            holder.mIsCloseFriend.setVisibility(View.VISIBLE);
            database.userCloseFollowersList(currentUser.getId()).addOnSuccessListener(closeFollowers -> {
                boolean found_user = false;
                for (User closeFollower : closeFollowers) {
                    if (closeFollower.getId().equals(user.getId())) {
                        holder.mIsCloseFriend.setBackground(Kandle.getContext().getDrawable(R.drawable.button_background));
                        holder.mIsCloseFriend.setContentDescription(Kandle.getContext().getString(R.string.userAdapterHolderDescriptionCloseFriend));
                        found_user = true;
                    }
                }
                if (!found_user) {
                    holder.mIsCloseFriend.setBackground(Kandle.getContext().getDrawable(R.drawable.add_background_grey));
                    holder.mIsCloseFriend.setContentDescription(Kandle.getContext().getString(R.string.userAdapterHolderDescriptionNotCloseFriend));
                }

            });
            holder.mIsCloseFriend.setOnClickListener(v ->
                    database.userCloseFollowersList(currentUser.getId()).addOnSuccessListener(list -> {
                        boolean foundUser = false;
                        for (User user1 : list) {
                            if (user1.getId().equals(user.getId())) {
                                holder.mIsCloseFriend.setBackground(Kandle.getContext().getDrawable(R.drawable.add_background_grey));
                                holder.mIsCloseFriend.setContentDescription(Kandle.getContext().getString(R.string.userAdapterHolderDescriptionNotCloseFriend));
                                database.unsetCloseFollower(user.getId(), currentUser.getId());
                                foundUser = true;
                            }
                        }
                        if (!foundUser) {
                            holder.mIsCloseFriend.setBackground(Kandle.getContext().getDrawable(R.drawable.button_background));
                            holder.mIsCloseFriend.setContentDescription(Kandle.getContext().getString(R.string.userAdapterHolderDescriptionCloseFriend));
                            database.setCloseFollower(user.getId(), currentUser.getId());
                        }

                    }));
        } else {
            holder.mIsCloseFriend.setVisibility(View.GONE);
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

        TextView mNickname;
        TextView mUsername;
        CircleImageView image_profile;
        Button mFollowBtn;
        ImageButton mIsCloseFriend;

        ViewHolder(@NonNull View itemView) {
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
