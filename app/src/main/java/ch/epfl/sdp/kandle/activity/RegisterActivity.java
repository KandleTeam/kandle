package ch.epfl.sdp.kandle.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.storage.Database;
import ch.epfl.sdp.kandle.storage.caching.CachedFirestoreDatabase;

public class RegisterActivity extends AppCompatActivity {


    private EditText mUsername, mEmail, mPassword, mPasswordConfirm;
    private Database database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mUsername = findViewById(R.id.username);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPasswordConfirm = findViewById(R.id.passwordConfirm);
        Button mSignUpBtn = findViewById(R.id.loginBtn);
        TextView mSignInLink = findViewById(R.id.signInLink);
        database = DependencyManager.getCachedDatabase();

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
                            mUsername.setError((getString(R.string.registerUsernameAlreadyUsed)));
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
        pd.setMessage(getString(R.string.registerAccountBeingCreated));
        pd.show();
        DependencyManager.getAuthSystem().createUserWithEmailAndPassword(username, email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, getString(R.string.registerAccountCreated), Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), CustomAccountActivity.class));
                finishAffinity();
            } else {
                pd.dismiss();
                String errorMsg = String.format("%s : %s", getString(R.string.ErrorMessage), Objects.requireNonNull(task.getException()).getMessage());
                Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                task.getException().printStackTrace();
            }
        });

    }

    private boolean checkFields(String username, String email, String password, String passwordConfirm) {

        boolean isValid = true;

        if (username.isEmpty()) {
            mUsername.setError(getString(R.string.registerUsernameRequired));
            isValid = false;
        }

        if (email.isEmpty()) {
            mEmail.setError(getString(R.string.registerEmailRequired));
            isValid = false;
        }

        if (password.length() < 8) {

            mPassword.setError(getString(R.string.registerPasswordLengthError));
            isValid = false;
        } else if (!password.equals(passwordConfirm)) {
            mPasswordConfirm.setError(getString(R.string.registerPasswordsMatchingError));
            isValid = false;
        }

        return isValid;

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


}
