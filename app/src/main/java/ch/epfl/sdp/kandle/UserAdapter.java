package ch.epfl.sdp.kandle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ch.epfl.sdp.kandle.DependencyInjection.Authentication;
import ch.epfl.sdp.kandle.DependencyInjection.AuthenticationUser;
import ch.epfl.sdp.kandle.DependencyInjection.Database;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {


    public interface ClickListener {
        void onItemClick(int position, View v);
    }
    private static ClickListener clickListener;
    private List<User> mUsers;

    public UserAdapter(List<User> mUsers) {
        this.mUsers = mUsers;
    }

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

        TextView mFullname = holder.mUsername;
        mFullname.setText(user.getUsername());

        Authentication authentication = Authentication.getAuthenticationSystem();
        final AuthenticationUser authenticationUser = authentication.getCurrentUser();

        final Database database = Database.getDatabaseSystem();


        database.followingList(authenticationUser.getUid()).addOnCompleteListener(new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {

                if (task.isSuccessful()){

                    if (   (task.getResult() == null) || (!task.getResult().contains(user.getId()))   ){
                        holder.mFollowBtn.setText("follow");
                    }

                    else {
                        holder.mFollowBtn.setText("following");
                    }

                }
                /*else {
                    System.out.println(task.getException().getMessage());
                }*/

            }
        });


        holder.mFollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("clickButton");
                if (holder.mFollowBtn.getText().toString().equals("follow")) {

                    database.follow(authenticationUser.getUid(), user.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                holder.mFollowBtn.setText("following");
                            }

                           /* else {
                                System.out.println(task.getException().getMessage());
                            }*/

                        }
                    });

                } else {

                    database.unFollow(authenticationUser.getUid(), user.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                holder.mFollowBtn.setText("follow");
                            }

                            /*else {
                                System.out.println(task.getException().getMessage());
                            }*/
                        }
                    });

                }
            }
        });




    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView mUsername;
        public CircleImageView image_profile;
        public Button mFollowBtn;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mUsername = itemView.findViewById(R.id.search_fullName);
            image_profile = itemView.findViewById(R.id.search_image_user);
            mFollowBtn = itemView.findViewById(R.id.btn_follow);
        }

        @Override
        public void onClick(View v) {
            System.out.println("click");
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }


}
