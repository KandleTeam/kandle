package ch.epfl.sdp.kandle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder>{
    private List<Achievement> mAchievemnts;
    private Context mContext;
    private  ViewHolder viewHolder;

    public AchievementAdapter(List<Achievement> achievements, Context context) {
        mAchievemnts = achievements;
        mContext = context;
    }


    @NonNull
    @Override
    public AchievementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View achievementsView = inflater.inflate(R.layout.achievement_item, parent, false);

        // Return a new holder instance
        viewHolder = new ViewHolder(achievementsView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Achievement achievement = mAchievemnts.get(position);
        achievement.checkAchievement();

        // Set item views based on your views and data model
        ImageView imageView = holder.mImageView;
        if(achievement.checkAchievementState()){
            imageView.setImageResource(R.drawable.ic_mood_black_24dp);
        }
        TextView titleView = holder.mtitleText;
        titleView.setText(String.valueOf(achievement.getDescription()));
        TextView dateView = holder.mHowToGetIt;
        dateView.setText((String.valueOf(achievement.getWayToComplete())));
    }

    @Override
    public int getItemCount() {
        //if (mPosts == null) return 0;
        return mAchievemnts.size();
    }

    public void notifyChange(){
        notifyDataSetChanged();
    }

    public void changeList(List<Achievement> achievements){
        this.mAchievemnts = achievements;
        notifyChange();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mtitleText;
        public TextView mHowToGetIt;
        public ImageView mImageView;


        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mtitleText = (TextView) itemView.findViewById(R.id.achievement_description);
            mHowToGetIt = (TextView) itemView.findViewById(R.id.achievement_title);
            mImageView = (ImageView) itemView.findViewById(R.id.achievement_trophy);

        }

        @Override
        public void onClick(View v) {
        }

    }

}