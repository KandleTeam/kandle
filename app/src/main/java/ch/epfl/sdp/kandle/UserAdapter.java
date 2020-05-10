package ch.epfl.sdp.kandle;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import de.hdodenhof.circleimageview.CircleImageView;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getAuthSystem;
import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getInternalStorageSystem;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final static int PROFILE_PICTURE_TAG = 9;
    private static ClickListener clickListener;
    private List<User> mUsers;

    /**
     * Creates a UserAdapter object
     * @param mUsers
     */
    public UserAdapter(List<User> mUsers) {
        this.mUsers = mUsers;
    }

    /**
     * Sets a clickLister
     * @param clickListener
     */
    public void setOnItemClickListener(ClickListener clickListener) {
        UserAdapter.clickListener = clickListener;
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
        mUsername.setText(String.format("@%s", user.getUsername()));

        ImageView mImageProfile = holder.image_profile;
        if (user.getImageURL() != null) {
            mImageProfile.setTag(PROFILE_PICTURE_TAG);
            File image = getInternalStorageSystem().getImageFileById(user.getId());
            if (image != null) {
                Picasso.get().load(image).into(mImageProfile);
            } else {
                Picasso.get().load(user.getImageURL()).into(mImageProfile);
            }
        }

        final Authentication authentication = getAuthSystem();
        final User currentUser = authentication.getCurrentUser();
        final CachedFirestoreDatabase database = new CachedFirestoreDatabase();


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

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mNickname;
        private TextView mUsername;
        private CircleImageView image_profile;
        private Button mFollowBtn;


        /**
         * Creates a ViewHolder for the RecycleView
         * @param itemView
         */
        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mNickname = itemView.findViewById(R.id.search_fullName);
            mUsername = itemView.findViewById(R.id.search_username);
            image_profile = itemView.findViewById(R.id.search_image_user);
            mFollowBtn = itemView.findViewById(R.id.btn_follow);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }


}