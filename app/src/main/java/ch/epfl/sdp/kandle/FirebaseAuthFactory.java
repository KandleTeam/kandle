package ch.epfl.sdp.kandle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.internal.InternalAuthProvider;
import com.google.firestore.v1.FirestoreGrpc;

public class FirebaseAuthFactory {
    private static FirebaseAuth dependency = FirebaseAuth.getInstance();

    public static void setDependency(FirebaseAuth fAuth){
        dependency = fAuth;
        //dependency = FirebaseAuth.getInstance();
    }

    public static FirebaseAuth getDependency(){
        return dependency;
    }
}
