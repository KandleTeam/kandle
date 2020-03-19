package ch.epfl.sdp.kandle.db;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public interface Authentication {

    public abstract Task<AuthResult> createUserWithEmailAndPassword(String email, String password) ;

    public abstract Task<AuthResult> signInWithEmailAndPassword(String email, String password);

    public abstract void signOut();

    public abstract AuthenticationUser getCurrentUser();

}