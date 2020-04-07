package ch.epfl.sdp.kandle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private static ClickListener clickListener;
    private List<Post> mPosts;
    private ViewHolder viewHolder;

    private String userId;

    private Authentication auth;
    private Database database;

    public PostAdapter(List<Post> posts) {
        mPosts = posts;
    }


    public void setOnItemClickListener(ClickListener clickListener) {
        PostAdapter.clickListener = clickListener;
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

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Post post = mPosts.get(position);

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();

        userId = auth.getCurrentUser().getId();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        // Set item views based on your views and data model
        TextView titleView = holder.mtitleText;
        titleView.setText(String.valueOf(post.getPostId()));
        TextView dateView = holder.mdate;
        dateView.setText((dateFormat.format(post.getDate())));
        final TextView likeView = holder.mlikes;
        likeView.setText(String.valueOf(post.getLikes()));

        holder.mlikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (post.getLikers().contains(userId)) {
                    database.unlikePost(userId, post.getPostId());
                    post.unlikePost(userId);
                } else {
                    database.likePost(userId, post.getPostId());
                    post.likePost(userId);

                }
                likeView.setText(String.valueOf(post.getLikes()));
            }

            ;
        });

        holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.deletePost(post);
                mPosts.remove(post);
                notifyDataSetChanged();
            }

            ;
        });
    }

    @Override
    public int getItemCount() {
        //if (mPosts == null) return 0;
        return mPosts.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mtitleText;
        public TextView mlikes;
        public TextView mdate;
        public ImageButton mlikeButton;
        public ImageButton mDeleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mtitleText = (TextView) itemView.findViewById(R.id.title);
            mlikes = (TextView) itemView.findViewById(R.id.flames);
            mdate = (TextView) itemView.findViewById(R.id.date_and_time);
            mlikeButton = itemView.findViewById(R.id.likeButton);
            mDeleteButton = itemView.findViewById(R.id.deleteButton);

        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }

    }

}