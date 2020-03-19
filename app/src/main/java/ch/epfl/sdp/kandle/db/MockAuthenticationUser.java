package ch.epfl.sdp.kandle.db;

public class MockAuthenticationUser extends AuthenticationUser {

    @Override
    public String getUid() {
        return "mockId";
    }

}