package ch.epfl.sdp.kandle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText mFullName,mEmail,mPassword, mPasswordConfirm;
    Button mSignUpBtn;
    TextView mSignInLink;
    FirebaseAuth fAuth;
    //ProgressDialog pd;

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
        mSignUpBtn = findViewById(R.id.loginBtn);
        mSignInLink = findViewById(R.id.signInLink);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        mSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });



        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = mFullName.getText().toString();
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String passwordConfirm = mPasswordConfirm.getText().toString().trim();

                if (!checkFields(fullName,email, password, passwordConfirm)){
                    return;
                }

                performRegisterViaFirebase(fullName, email, password);

            }
        });


    }

    private void performRegisterViaFirebase (final String fullName, final String email, String password) {

       // pd = new ProgressDialog(RegisterActivity.this);
       // pd.setMessage("Connection...");
        //pd.show();

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "User has been created", Toast.LENGTH_LONG ).show();


                    userID = fAuth.getCurrentUser().getUid();


                    {

                    //store user in the firestore
                    DocumentReference documentReference = fStore.collection("users").document(userID);
                    Map<String,Object> user = new HashMap<>();
                    user.put("fullName",fullName);
                    user.put("email",email);
                    documentReference.set(user);




                    //store user in realtimedatabase
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", userID);
                    map.put("fullname", fullName);
                   // map.put ("fullnameWithoutSpace", fullName.replace(" ", ""));
                    map.put("email",email);
                    map.put("fullnameSearch", fullName.toLowerCase().trim().replace(" ", ""));

                    databaseReference.setValue(map);}

                   // pd.dismiss();

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();

                }

                else {
                    //pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "An error has occurred : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
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
