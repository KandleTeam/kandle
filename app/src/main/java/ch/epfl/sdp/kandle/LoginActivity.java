package ch.epfl.sdp.kandle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.CachedDatabase;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import io.grpc.Internal;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;

public class LoginActivity extends AppCompatActivity {


    private TextView mSignIn;
    private EditText mEmail, mPassword;
    private Button mSignUpBtn;
    private Authentication auth;
    private Database database;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = DependencyManager.getAuthSystem();
        database = new CachedDatabase();


        if (auth.getCurrentUserAtApplicationStart()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }


        mSignIn = findViewById(R.id.signUpLink);

        mSignIn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mSignUpBtn = findViewById(R.id.loginBtn);

        mSignUpBtn.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            if (!checkFields(email, password)) {
                return;
            }
            attemptLogin(email, password);
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

    private void attemptLogin(String email, String password) {
        final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage(getString(R.string.login_in_progress));
        pd.show();
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
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
            }
        });
    }

    /**
     * @Author Marc
     * This task can't fail as it retrieves the uid only if the signIn doesn't fail which guarantes
     * that there exists a user with this uid
     * @param id
     */
    /*
    private void storeUserLocallywithId(String id) {
        database.getUserById(id).addOnCompleteListener(task -> {
                User user = task.getResult();
                internalStorage.saveUserAtLoginOrRegister(user);
        });
    }

     */

}
