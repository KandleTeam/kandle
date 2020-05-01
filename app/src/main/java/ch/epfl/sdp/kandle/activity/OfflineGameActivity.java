package ch.epfl.sdp.kandle.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import ch.epfl.sdp.kandle.R;

public class OfflineGameActivity extends AppCompatActivity {

    public final int MAX_POINTS = 2;
    public final int APPEARING_TIME = 3000; //in miliseconds


    private ImageButton mVirusButton;
    private ImageButton mStartButton;
    private TextView mStartText;
    private TextView mScoreText;
    private TextView mEndText;
    private TextView mMaxScoreText;

    private int[] nbPoints = {0, 0, 0}; //max nb we could have at a given time, actual nb we have, and Max score

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_game);

        mVirusButton = findViewById(R.id.virusButton);
        mStartButton = findViewById(R.id.startButton);
        mStartText = findViewById(R.id.startText);
        mScoreText = findViewById(R.id.scoreText);
        mEndText = findViewById(R.id.endText);
        mMaxScoreText = findViewById(R.id.maxScoreText);

        mVirusButton.setVisibility(View.GONE);
        mStartText.setText("Stay at home and click on the virus to kill it and thus limit the spread of the pandemic ! Ready ?");
        mScoreText.setText("0");
        mMaxScoreText.setText("0");
        mEndText.setText("FINISH");

        timer = new Timer();

        mStartButton.setOnClickListener(v -> {


            mStartText.setVisibility(View.GONE);
            mStartButton.setVisibility(View.GONE);
            mEndText.setVisibility(View.GONE);

            resetScore(nbPoints);
            resetMaxPossibleScore(nbPoints);

            mScoreText.setText(Integer.toString(nbPoints[1]));

            timer.schedule(new GameTimerTask(), APPEARING_TIME);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mVirusButton.getLayoutParams();
            params.horizontalBias = 0.0f;
            params.verticalBias = 0.0f;
            mVirusButton.setLayoutParams(params);
            mVirusButton.setVisibility(View.VISIBLE);

            //augmenter le numéro de l'image, faire apparaitre l'image a un endroit random pdt temps
            //random, et au bout d'un certain temps ou au click, image disparait, si clique : +1 pt
            //sinon rien.

        });

        mVirusButton.setOnClickListener(v2 -> {
            timer.cancel();
            mVirusButton.setVisibility(View.GONE);
            updateScore(nbPoints);
            mScoreText.setText(Integer.toString(nbPoints[1]));
            if (getMaxPossiblePoints(nbPoints) < MAX_POINTS) {
                System.out.println(nbPoints[0]);
                incrementMaxPossiblePoints(nbPoints);
                System.out.println(nbPoints[0]);
                timer = new Timer();
                timer.schedule(new GameTimerTask(), APPEARING_TIME);
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mVirusButton.getLayoutParams();
                params.horizontalBias = 0.2f;
                params.verticalBias = 0.1f;
                mVirusButton.setLayoutParams(params);
                mVirusButton.setVisibility(View.VISIBLE);
            } else {
                System.out.println(nbPoints[0]);
                if (nbPoints[1] > nbPoints[2]) {
                    nbPoints[2] = nbPoints[1];
                    mMaxScoreText.setText(Integer.toString(nbPoints[2]));
                }
                mEndText.setVisibility(View.VISIBLE);
                mStartButton.setVisibility(View.VISIBLE);
            }
        });


    }

    private int getMaxPossiblePoints(int[] nbPoints) {
        return nbPoints[0];
    }

    private void incrementMaxPossiblePoints(int[] nbPoints) {
        nbPoints[0]++;
    }

    private void resetMaxPossibleScore(int[] nbPoints) {
        nbPoints[0] = 0;
    }

    private void updateScore(int[] nbPoints) {
        nbPoints[1]++;
    }

    private void resetScore(int[] nbPoints) {
        nbPoints[1] = 0;
    }

    private void setRecord(int[] nbPoints, int max) {
        nbPoints[2] = max;
    }


    private class GameTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVirusButton.setVisibility(View.GONE);
                    if (getMaxPossiblePoints(nbPoints) < MAX_POINTS) {
                        incrementMaxPossiblePoints(nbPoints);
                        timer = new Timer();
                        timer.schedule(new GameTimerTask(), APPEARING_TIME);

                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mVirusButton.getLayoutParams();
                        params.horizontalBias = 0.5f;
                        params.verticalBias = 0.5f;
                        mVirusButton.setLayoutParams(params);
                        mVirusButton.setVisibility(View.VISIBLE);
                    }else{
                        if (nbPoints[1] > nbPoints[2]) {
                            nbPoints[2] = nbPoints[1];
                            mMaxScoreText.setText(Integer.toString(nbPoints[2]));
                        }
                        mEndText.setVisibility(View.VISIBLE);
                        mStartButton.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }
}
