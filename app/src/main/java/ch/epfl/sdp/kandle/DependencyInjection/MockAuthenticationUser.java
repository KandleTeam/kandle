package ch.epfl.sdp.kandle.DependencyInjection;

public class MockAuthenticationUser extends AuthenticationUser {


    @Override
    public String getUid() {
        return "user1Id";
    }
}
