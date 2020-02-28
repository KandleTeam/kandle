package ch.epfl.sdp.kandle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText mFullName,mEmail,mPassword, mPasswordConfirm;
    Button mSignInBtn;
    TextView mSignUp;
    FirebaseAuth fAuth;
    ProgressBar mProgressBar;

    //FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mFullName   = findViewById(R.id.fullName);
        mEmail      = findViewById(R.id.email);
        mPassword   = findViewById(R.id.password);
        mPasswordConfirm = findViewById(R.id.passwordConfirm);
        mSignInBtn= findViewById(R.id.signUpBtn);
        mProgressBar = findViewById(R.id.progressBar);
        mSignUp=findViewById(R.id.signUpLink);
       // mLoginBtn   = findViewById(R.id.createText);

        fAuth = FirebaseAuth.getInstance();

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = mFullName.getText().toString();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String passwordConfirm = mPasswordConfirm.getText().toString().trim();


                //Check every field

                if (fullName.isEmpty() ){
                    mFullName.setError("Your full name is required !");
                    return;
                }

                if (email.isEmpty() ){
                    mEmail.setError("Your email is required !" );
                    return;
                }

                if (password.length()<8){
                    mPassword.setError("Please choose a password of more than 8 characters !");
                    return;
                }

                if (!password.equals(passwordConfirm)){
                    mPasswordConfirm.setError("Your passwords do not match !");
                    return;
                }


                //Sign in Process

              //  mProgressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "User has been created", Toast.LENGTH_LONG ).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        }

                        else {
                            Toast.makeText(RegisterActivity.this, "An error has occurred : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                         //   mProgressBar.setVisibility(View.GONE);

                        }
                    }
                });


            }
        });


    }
}
