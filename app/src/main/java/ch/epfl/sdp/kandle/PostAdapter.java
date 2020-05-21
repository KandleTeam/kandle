package ch.epfl.sdp.kandle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import ch.epfl.sdp.kandle.activity.PostActivity;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.fragment.ListUsersFragment;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public final static int POST_IMAGE = 10;

    private List<Post> mPosts;
    private Context mContext;
    private ViewHolder viewHolder;

    private String userId;

    private Authentication auth;
    private Database database;

    public PostAdapter(List<Post> posts, Context context) {
        mPosts = posts;
        mContext = context;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View postsView = inflater.inflate(R.layout.post_item, parent, false);

        // Return a new holder instance
        viewHolder = new ViewHolder(postsView);
        return viewHolder;
    }

    public void setPost(List<Post> posts) {
        this.mPosts = posts;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Post post = mPosts.get(position);

        auth = DependencyManager.getAuthSystem();
        database = new CachedFirestoreDatabase();

        userId = auth.getCurrentUser().getId();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        // Set item views based on your views and data model
        TextView titleView = holder.mtitleText;
        titleView.setText(String.valueOf(post.getDescription()));
        TextView dateView = holder.mdate;
        dateView.setText((dateFormat.format(post.getDate())));
        final TextView likeView = holder.mlikes;
        likeView.setText(String.valueOf(post.getLikes()));
        CircleImageView profilePicView = holder.mProfilePic;
        TextView usernameView = holder.mUsername;
        TextView nicknameView = holder.mNickname;
        final ImageButton editPostView = holder.mEditButton;
        final ImageButton deletePostView = holder.mDeleteButton;

        if (post.getType()!=null && post.getType().equals(Post.EVENT)) {
            holder.mlikeButton.setBackgroundResource(R.drawable.ic_event_black_24dp);
            holder.mlikeButton.setOnClickListener(v -> {

                if (post.getLikers().contains(userId)) {
                    database.unlikePost(userId, post.getPostId()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            post.unlikePost(userId);
                            likeView.setText(String.valueOf(post.getLikes()));
                        } else {
                            Toast.makeText(this.mContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    database.likePost(userId, post.getPostId()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            post.likePost(userId);
                            likeView.setText(String.valueOf(post.getLikes()));
                        } else {
                            Toast.makeText(this.mContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        }

        if(post.getIsForCloseFollowers() != null && post.getIsForCloseFollowers().equals(Post.CLOSE_FOLLOWER)){
            holder.mIsForCloseFollowers.setVisibility(View.VISIBLE);
        }
        else{
            holder.mIsForCloseFollowers.setVisibility(View.GONE);
        }

        ImageView postImageView = holder.mPostImage;
        if (post.getImageURL() != null) {
            postImageView.setVisibility(View.VISIBLE);
            postImageView.setTag(POST_IMAGE);
            Picasso.get().load(post.getImageURL()).into(postImageView);
        }
        else{
            postImageView.setVisibility(View.GONE);
        }

        database.getUserById(post.getUserId()).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();

                if (user.getImageURL() != null) {
                    File image = DependencyManager.getInternalStorageSystem().getImageFileById(user.getId());
                    if(image != null) {
                        Picasso.get().load(image).into(profilePicView);
                    }else {
                        Picasso.get().load(user.getImageURL()).into(profilePicView);
                    }
                }
                else {
                    profilePicView.setImageDrawable(Kandle.getContext().getDrawable(R.drawable.ic_launcher_foreground));
                    profilePicView.setBackground(Kandle.getContext().getDrawable(R.drawable.ic_launcher_circle_background));
                }
                usernameView.setText("@" + user.getUsername());
                nicknameView.setText(user.getNickname());

                //milliseconds


                if (user.getId().equals(userId)) {
                    deletePostView.setVisibility(View.VISIBLE);
                    if (post.isEditable()) {
                        editPostView.setVisibility(View.VISIBLE);
                    }
                }


            }
        });

        editPostView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, PostActivity.class);
            intent.putExtra("postId", post.getPostId());
            mContext.startActivity(intent);
        });

        deletePostView.setOnClickListener(v -> {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setMessage("Do you really want to delete this post ?");
            alertDialog.setCancelable(false);
            alertDialog.setNegativeButton("No", (dialog, which) -> {

            });
            alertDialog.setPositiveButton("Yes", (dialog, which) -> database.deletePost(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mPosts.remove(post);
                    holder.mEditButton.setVisibility(View.GONE);
                    notifyDataSetChanged();
                }
            }));
            alertDialog.create().show();

        });
        final FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();


        holder.mlikes.setOnClickListener(v -> database.getLikers(post.getPostId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fragmentManager.beginTransaction().replace(R.id.flContent, ListUsersFragment.newInstance(
                        task.getResult(),
                        post.getType() != null && post.getType().equals(Post.EVENT)? "Participants" : "Likes",
                        Integer.toString(task.getResult().size())
                )).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();

            } else {
                Toast.makeText(this.mContext, task.getException().getMessage(), Toast.LENGTH_LONG).show();

            }
        }));
    }

    @Override
    public int getItemCount() {
        //if (mPosts == null) return 0;
        return mPosts.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mtitleText;
        public TextView mlikes;
        public TextView mdate;
        public ImageButton mlikeButton;
        public ImageButton mDeleteButton;
        public ImageButton mEditButton;
        public CircleImageView mProfilePic;
        public ImageView mPostImage;
        public TextView mUsername;
        public TextView mNickname;
        public ImageView mIsForCloseFollowers;


        public ViewHolder(View itemView) {
            super(itemView);
            mtitleText = itemView.findViewById(R.id.title);
            mlikes = itemView.findViewById(R.id.flames);
            mdate = itemView.findViewById(R.id.date_and_time);
            mlikeButton = itemView.findViewById(R.id.likeButton);
            mDeleteButton = itemView.findViewById(R.id.deleteButton);
            mEditButton = itemView.findViewById(R.id.editButton);
            mProfilePic = itemView.findViewById(R.id.profilePicInPost);
            mPostImage = itemView.findViewById(R.id.postImageInPost);
            mUsername = itemView.findViewById(R.id.usernameinPost);
            mNickname = itemView.findViewById(R.id.nicknameInPost);
            mIsForCloseFollowers = itemView.findViewById(R.id.isPostForCloseFollowers);
        }

    }





}
