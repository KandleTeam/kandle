package ch.epfl.sdp.kandle.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class LoginActivity extends AppCompatActivity {


    private TextView mSignIn;
    private EditText mEmail, mPassword;
    private Button mSignUpBtn;
    private Authentication auth;
    private CoordinatorLayout CNetworkBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = DependencyManager.getAuthSystem();

        if (auth.getCurrentUserAtApplicationStart()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mSignIn = findViewById(R.id.signUpLink);
        mSignIn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mSignUpBtn = findViewById(R.id.loginBtn);
        TextView mGuestMode = findViewById(R.id.guestModeLink);

        mSignUpBtn.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            if (checkFields(email, password) && checkForInternetConnection()) {
                attemptLogin(email, password);
            }
        });

        mGuestMode.setOnClickListener(v -> {
            LoggedInUser.initGuestMode();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });

    }


    private boolean checkFields(String email, String password) {

        if (email.isEmpty()) {
            mEmail.setError(getString(R.string.login_email_required));
            return false;
        }
        if (password.isEmpty()) {
            mPassword.setError(getString(R.string.login_password_required));
            return false;
        }

        return true;

    }

    private boolean checkForInternetConnection() {
        if (!DependencyManager.getNetworkStateSystem().isConnected()) {
            CNetworkBar = (CoordinatorLayout) findViewById(R.id.connectionBar);
            Snackbar snackbar = Snackbar.make(CNetworkBar, R.string.no_connexion, Snackbar.LENGTH_SHORT);
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
        pd.setMessage(getString(R.string.login_in_progress));
        pd.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            } else {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "An error has occurred : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                task.getException().printStackTrace();
            }
        });

    }


}
