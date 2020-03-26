package ch.epfl.sdp.kandle;

import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.dependencies.InternalStorage;
import ch.epfl.sdp.kandle.dependencies.InternalStorageHandler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private EditText mFullName, mEmail, mPassword, mPasswordConfirm;
    private Button mSignUpBtn;
    private TextView mSignInLink;
    private Authentication auth;
    private InternalStorageHandler internalStorageHandler;
    private Database database;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPasswordConfirm = findViewById(R.id.passwordConfirm);
        mSignUpBtn = findViewById(R.id.loginBtn);
        mSignInLink = findViewById(R.id.signInLink);
        database = DependencyManager.getDatabaseSystem();
        auth = DependencyManager.getAuthSystem();
        internalStorageHandler = new InternalStorageHandler(this);
        mSignInLink.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });


        mSignUpBtn.setOnClickListener(v -> {
            final String fullName = mFullName.getText().toString();
            final String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            String passwordConfirm = mPasswordConfirm.getText().toString().trim();

            if (!checkFields(fullName, email, password, passwordConfirm)) {
                return;
            }
            performRegisterViaFirebase(fullName, email, password);
        });


    }

    private void performRegisterViaFirebase(final String fullName, final String email, String password) {
        ProgressDialog pd = new ProgressDialog(RegisterActivity.this);
        pd.setMessage("Your account is being created");
        pd.show();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userID = auth.getCurrentUser().getUid();
                User user = new User(userID, email, email, null);
                user.setFullname(fullName);
                database.createUser(user);
                InternalStorage internalStorage = DependencyManager.getInternalStorageSystem(getApplicationContext());
                internalStorage.saveUserAtLoginOrRegister(user);
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, "User has been created", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), CustomAccountActivity.class));
                finishAffinity();
            } else {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, "An error has occurred : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean checkFields(String fullName, String email, String password, String passwordConfirm) {

        boolean bool = true;

        if (fullName.isEmpty()) {
            mFullName.setError("Your full name is required !");
            bool = false;
        } else if (email.isEmpty()) {
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


}
