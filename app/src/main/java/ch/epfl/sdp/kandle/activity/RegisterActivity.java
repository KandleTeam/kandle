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

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.authentification.Authentication;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class RegisterActivity extends AppCompatActivity {


    private EditText mUsername, mEmail, mPassword, mPasswordConfirm;
    private Button mSignUpBtn;
    private TextView mSignInLink;
    private Authentication auth;
    private CoordinatorLayout CNetworkBar;
    private Database database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mUsername = findViewById(R.id.username);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        mPasswordConfirm = findViewById(R.id.passwordConfirm);
        mSignUpBtn = findViewById(R.id.loginBtn);
        mSignInLink = findViewById(R.id.signInLink);
        database = DependencyManager.getCachedDatabase();
        auth = DependencyManager.getAuthSystem();
        mSignInLink.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });


        mSignUpBtn.setOnClickListener(v -> {
            final String username = mUsername.getText().toString();
            final String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            String passwordConfirm = mPasswordConfirm.getText().toString().trim();
            if (checkFields(username, email, password, passwordConfirm) && checkForInternetConnection()) {
                database.getUserByName(username).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            mUsername.setError(("This username is already used !"));
                        } else {
                            performRegisterViaFirebase(username, email, password);
                        }
                    }
                });
            }
        });


    }


    private void performRegisterViaFirebase(final String username, final String email, String password) {

        ProgressDialog pd = new ProgressDialog(RegisterActivity.this);
        pd.setMessage("Your account is being created");
        pd.show();
        auth.createUserWithEmailAndPassword(username, email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, "User has been created", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), CustomAccountActivity.class));
                finishAffinity();
            } else {
                pd.dismiss();
                System.out.println("Task creation was sucessfull" + task.getException().getMessage());
                task.getException().printStackTrace();
                Toast.makeText(RegisterActivity.this, "An error has occurred : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean checkFields(String username, String email, String password, String passwordConfirm) {

        boolean bool = true;

        if (username.isEmpty()) {
            mUsername.setError("Your username is required !");
            bool = false;
        }

        if (email.isEmpty()) {
            mEmail.setError("Your email is required !");
            bool = false;
        } else if (password.length() < 8) {

            mPassword.setError("Please choose a password of more than 8 characters !");
            bool = false;
        } else if (!password.equals(passwordConfirm)) {
            mPasswordConfirm.setError("Your passwords do not match !");
            bool = false;
        }

        return bool;

    }

    private boolean checkForInternetConnection() {
        if (!DependencyManager.getNetworkStateSystem().isConnected()) {
            CNetworkBar = findViewById(R.id.connectionBar);
            Snackbar snackbar = Snackbar.make(CNetworkBar, R.string.noConnexion, Snackbar.LENGTH_SHORT);
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white));
            CNetworkBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
            snackbar.show();
            CNetworkBar.bringToFront();
            return false;
        }
        return true;

    }


}
