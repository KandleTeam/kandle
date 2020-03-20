package ch.epfl.sdp.kandle.dependencies;

public class MockAuthenticationUser extends AuthenticationUser {


    @Override
    public String getUid() {
        return "user1Id";
    }
}
