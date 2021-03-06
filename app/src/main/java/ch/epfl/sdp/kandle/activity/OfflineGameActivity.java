package ch.epfl.sdp.kandle.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Timer;
import java.util.TimerTask;

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;

public class OfflineGameActivity extends AppCompatActivity {

    public final static int MAX_NB_VIRUS = 10;
    public final static int APPEARING_TIME = 750; //in ms - it will determine the game difficulty

    private ImageButton mVirusButton;
    private ImageButton mStartButton;
    private ImageButton mBackButton;
    private TextView mStartText;
    private TextView mEndText;
    private TextView mScore;
    private TextView mScoreText;
    private TextView mMaxScore;
    private TextView mMaxScoreText;


    /**
     * we create an array in order to be able to modify its values inside a clickListener
     * <p>
     * it contains :
     * - the total number of images already displayed
     * - the number of images we clicked on (thus nb of points)
     * - the max score we have
     */
    private int[] nbPoints = {0, 0, 0};

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_game);

        mVirusButton = findViewById(R.id.virusButton);
        mStartButton = findViewById(R.id.startButton);
        mBackButton = findViewById(R.id.backButton);
        mStartText = findViewById(R.id.startText);
        mEndText = findViewById(R.id.endText);
        mScore = findViewById(R.id.score);
        mScoreText = findViewById(R.id.scoreText);
        mMaxScore = findViewById(R.id.maxScore);
        mMaxScoreText = findViewById(R.id.maxScoreText);

        mVirusButton.setVisibility(View.GONE);
        mStartText.setText(getString(R.string.gameDescription));
        mEndText.setText(getString(R.string.endText));
        mEndText.setVisibility(View.GONE);

        mScoreText.setText(getString(R.string.scoreText));
        mScore.setText(getString(R.string.initialScore));

        if (DependencyManager.getAuthSystem().getCurrentUser() != null) {
            nbPoints[2] = DependencyManager.getAuthSystem().getCurrentUser().getHighScore();
        }
        mMaxScoreText.setText(getString(R.string.recordText));
        mMaxScore.setText(Integer.toString(nbPoints[2]));

        mStartButton.setOnClickListener(v -> {


            mStartText.setVisibility(View.GONE);
            mStartButton.setVisibility(View.GONE);
            mEndText.setVisibility(View.GONE);

            resetScore(nbPoints);
            resetMaxPossibleScore(nbPoints);

            mScore.setText(Integer.toString(nbPoints[1]));

            timer = new Timer();
            timer.schedule(new GameTimerTask(), APPEARING_TIME);

            setRandomVirusButtonPositionAndDisplay();
            incrementMaxPossiblePoints(nbPoints);

        });

        mVirusButton.setOnClickListener(v -> {
            timer.cancel();
            updateScore(nbPoints);
            mScore.setText(Integer.toString(nbPoints[1]));
            handlingVirusDisappearing();
        });

        mBackButton.setOnClickListener(v -> {
            finish();
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

    private void setRandomVirusButtonPositionAndDisplay() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mVirusButton.getLayoutParams();
        double horizontal_pos = Math.random();
        double vertical_pos = Math.random();
        params.horizontalBias = (float) horizontal_pos;
        params.verticalBias = (float) vertical_pos;
        mVirusButton.setLayoutParams(params);
        mVirusButton.setVisibility(View.VISIBLE);
    }

    private void handlingVirusDisappearing() {
        mVirusButton.setVisibility(View.GONE);
        if (getMaxPossiblePoints(nbPoints) < MAX_NB_VIRUS) {
            incrementMaxPossiblePoints(nbPoints);
            timer = new Timer();
            timer.schedule(new GameTimerTask(), APPEARING_TIME);
            setRandomVirusButtonPositionAndDisplay();
        } else {
            if (nbPoints[1] > nbPoints[2]) {
                setRecord(nbPoints, nbPoints[1]);
                mMaxScore.setText(Integer.toString(nbPoints[2]));
                if (DependencyManager.getAuthSystem().getCurrentUser() != null) {
                    DependencyManager.getInternalStorageSystem().getCurrentUser().setHighScore(nbPoints[2]);
                    LoggedInUser.getInstance().setHighScore(nbPoints[2]);
                }
            }
            mEndText.setVisibility(View.VISIBLE);
            mStartButton.setVisibility(View.VISIBLE);
        }
    }

    private class GameTimerTask extends TimerTask {

        @Override
        public void run() {
            // we have to run on UI Thread otherwise we couldn't access the virus button
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handlingVirusDisappearing();
                }
            });
        }
    }
}
