package ch.epfl.sdp.kandle.DependencyInjection;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthenticationUser extends AuthenticationUser {

    FirebaseAuth fAuth;

    public FirebaseAuthenticationUser(FirebaseAuth fAuth) {

        this.fAuth = fAuth;
    }

    @Override
    public String getUid() {
        return fAuth.getCurrentUser().getUid();
    }
}
