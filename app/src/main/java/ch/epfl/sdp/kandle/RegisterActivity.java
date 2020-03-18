package ch.epfl.sdp.kandle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ch.epfl.sdp.kandle.db.Database;
import ch.epfl.sdp.kandle.db.DatabaseManager;

public class RegisterActivity extends AppCompatActivity {

    EditText mFullName,mEmail,mPassword, mPasswordConfirm;
    Button mSignUpBtn;
    TextView mSignInLink;
    FirebaseAuth fAuth;
    Database db;

    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFullName   = findViewById(R.id.fullName);
        mEmail      = findViewById(R.id.email);
        mPassword   = findViewById(R.id.password);
        mPasswordConfirm = findViewById(R.id.passwordConfirm);
        mSignUpBtn = findViewById(R.id.signUpBtn);
        mSignInLink = findViewById(R.id.signInLink);
        db = DatabaseManager.getDatabaseSystem();
        fAuth = FirebaseAuth.getInstance();

        mSignInLink.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            finish();
        });



        mSignUpBtn.setOnClickListener(v -> {
            final String fullName = mFullName.getText().toString();
            final String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            String passwordConfirm = mPasswordConfirm.getText().toString().trim();

            if (!checkFields(fullName,email, password, passwordConfirm)){
                return;
            }

            registerUser(fullName, email, password);

        });


    }

    private void registerUser(final String fullName, final String email, String password) {
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()){
                Toast.makeText(RegisterActivity.this, "User has been created", Toast.LENGTH_LONG ).show();

                //store user in the database
                userID = fAuth.getCurrentUser().getUid();

                User newUser = new User(userID, email, email);
                db.createUser(newUser).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()){
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "An error has occurred : " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            else {
                Toast.makeText(RegisterActivity.this, "An error has occurred : " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }


    private boolean checkFields (String fullName, String email, String password, String passwordConfirm){

        boolean bool = true;

        if (fullName.isEmpty() ){
            mFullName.setError("Your full name is required !");
            bool = false;
        }

         else if (email.isEmpty() ){
            mEmail.setError("Your email is required !" );
            bool =  false;
        }

        else if (password.length()<8){
            mPassword.setError("Please choose a password of more than 8 characters !");
            bool = false;
        }

        else if (!password.equals(passwordConfirm)){
            mPasswordConfirm.setError("Your passwords do not match !");
            bool = false;
        }

        return bool;

    }


}
