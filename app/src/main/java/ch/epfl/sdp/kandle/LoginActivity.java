package ch.epfl.sdp.kandle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ch.epfl.sdp.kandle.db.Authentication;
import ch.epfl.sdp.kandle.db.DependencyManager;

public class LoginActivity extends AppCompatActivity {


    TextView mSignIn;
    EditText mEmail, mPassword;
    Button mSignUpBtn;
    Authentication auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = DependencyManager.getAuthSystem();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        mSignIn = findViewById(R.id.signUpLink);
        mSignIn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        });


        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mSignUpBtn = findViewById(R.id.loginBtn);




        mSignUpBtn.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();

            if (!checkFields(email, password))  {
                return;
            }
            loginWithEmailAndPassword(email, password);
        });

    }


    private boolean checkFields (String email, String password){

        if (email.isEmpty()) {
            mEmail.setError(getString(R.string.login_email_required));
            return false ;
        }
        if (password.isEmpty()) {
            mPassword.setError(getString(R.string.login_password_required));
            return false ;
        }

        return true;

    }



    private void loginWithEmailAndPassword(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();

            } else {
                Toast.makeText(LoginActivity.this, "An error has occurred : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }
}
