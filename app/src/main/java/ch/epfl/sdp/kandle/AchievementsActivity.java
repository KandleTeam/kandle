package ch.epfl.sdp.kandle;

import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

public class AchievementsActivity extends AppCompatActivity {

    private final double REDUCTION_FACTOR = 0.8;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * REDUCTION_FACTOR) , (int)(height * REDUCTION_FACTOR));
    }
}
