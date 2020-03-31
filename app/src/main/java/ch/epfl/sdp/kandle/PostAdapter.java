package ch.epfl.sdp.kandle;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.fragment.ListUsersFragment;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
    private static ClickListener clickListener;
    private List<Post> mPosts;
    private Context mContext;
    private  ViewHolder viewHolder;

    private String userId;

    private Authentication auth;
    private Database database;

    public PostAdapter(List<Post> posts, Context context) {
        mPosts = posts;
        mContext = context;
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

        userId = auth.getCurrentUser().getUid();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        // Set item views based on your views and data model
        TextView titleView = holder.mtitleText;
        titleView.setText(String.valueOf(post.getDescription()));
        TextView dateView = holder.mdate;
        dateView.setText((dateFormat.format(post.getDate())));
        final TextView likeView = holder.mlikes;
        likeView.setText(String.valueOf(post.getLikes()));

        holder.mlikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(post.getLikers().contains(userId)){
                    database.unlikePost(userId, post.getPostId());
                    post.unlikePost(userId);
                }else{
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
                database.deletePost(userId, post);
                mPosts.remove(post);
                notifyDataSetChanged();
            }
            ;
        });
        final FragmentManager fragmentManager =   ((AppCompatActivity) mContext).getSupportFragmentManager();

        holder.mlikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getLikers(post.getPostId()).addOnCompleteListener(new OnCompleteListener<List<User>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<User>> task) {
                        if (task.isSuccessful()){
                            fragmentManager.beginTransaction().replace( R.id.flContent, ListUsersFragment.newInstance(
                                    task.getResult(),
                                    "Likes",
                                    Integer.toString(task.getResult().size())
                            )).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .addToBackStack(null)
                                    .commit();

                        }

                        else {
                            System.out.println(task.getException().getMessage());
                        }
                    }
                });
            }
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
            clickListener.onItemClick(getAdapterPosition(),v);
        }

    }

}