package ch.epfl.sdp.kandle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    EditText mPostText;
    Button mPostButton;
    ImageButton mGaleryButton, mCameraButton;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mPostText = findViewById(R.id.postText);
        mPostButton =findViewById(R.id.postButton);
        mGaleryButton =findViewById(R.id.galeryButton);
        mCameraButton =findViewById(R.id.cameraButton);

        fStore = FirebaseFirestore.getInstance();

        fAuth = FirebaseAuth.getInstance();

        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postText  = mPostText.getText().toString().trim();

                if(postText.isEmpty()){
                    mPostText.setError("Your post is empty...");
                    return;
                }
                Toast.makeText(PostActivity.this, "You have successfully posted : " + postText, Toast.LENGTH_LONG ).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();

            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostActivity.this, "Doesn't work for now... ", Toast.LENGTH_LONG ).show();
            }
        });


        mGaleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostActivity.this, "Doesn't work for now... ", Toast.LENGTH_LONG ).show();
            }
        });
    }
}

//store user in the database

                /*userID = fAuth.getCurrentUser().getUid();
                DocumentReference documentReference = fStore.collection("users").document(userID);
                Map<String,Object> post = new HashMap<>();
                post.put("posts",postText);
                documentReference.set(post, SetOptions.merge());*/