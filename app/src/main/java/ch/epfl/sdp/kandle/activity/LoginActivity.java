package ch.epfl.sdp.kandle.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import java.util.Objects;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (DependencyManager.getAuthSystem().getCurrentUserAtApplicationStart()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        TextView mSignIn = findViewById(R.id.signUpLink);
        mSignIn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        Button mSignUpBtn = findViewById(R.id.loginBtn);
        ImageButton mGameButton = findViewById(R.id.startOfflineGameButton);
        TextView mGuestMode = findViewById(R.id.guestModeLink);

        mSignUpBtn.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            if (checkFields(email, password) && checkForInternetConnection()) {
                attemptLogin(email, password);
            }
        });

        if (!checkForInternetConnection()) {
            mGameButton.setVisibility(View.VISIBLE);
            mGameButton.setOnClickListener(v -> {
                startActivity(new Intent(getApplicationContext(), OfflineGameActivity.class));
            });
        }

        mGuestMode.setOnClickListener(v -> {
            LoggedInUser.initGuestMode();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });

    }


    private boolean checkFields(String email, String password) {

        if (email.isEmpty()) {
            mEmail.setError(getString(R.string.loginEmailRequired));
            return false;
        }
        if (password.isEmpty()) {
            mPassword.setError(getString(R.string.loginPasswordRequired));
            return false;
        }

        return true;

    }

    private boolean checkForInternetConnection() {
        if (!DependencyManager.getNetworkStateSystem().isConnected()) {
            CoordinatorLayout CNetworkBar = findViewById(R.id.connectionBar);
            Snackbar snackbar = Snackbar.make(CNetworkBar, R.string.noConnexion, Snackbar.LENGTH_SHORT);
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white));
            CNetworkBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            CNetworkBar.bringToFront();
            return false;
        }
        return true;

    }

    private void attemptLogin(String email, String password) {
        final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage(getString(R.string.loginInProgress));
        pd.show();
        DependencyManager.getAuthSystem().signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, getString(R.string.loginSuccess), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            } else {
                pd.dismiss();
                String errorMsg = String.format("%s : %s", getString(R.string.ErrorMessage), Objects.requireNonNull(task.getException()).getMessage());
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                task.getException().printStackTrace();
            }
        });

    }

}
