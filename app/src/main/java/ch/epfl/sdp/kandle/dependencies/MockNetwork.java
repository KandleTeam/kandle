package ch.epfl.sdp.kandle.dependencies;


import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.network.NetworkState;

public class MockNetwork implements NetworkState {
    private static boolean isOnline;
    private static boolean previouslyOnline;

    public MockNetwork(boolean isOnline) {
        MockNetwork.isOnline = isOnline;
        MockNetwork.previouslyOnline = true;
    }

    @Override
    public boolean isConnected() {
        if(!previouslyOnline && isOnline){
            DependencyManager.getDatabaseSystem().updateHighScore(LoggedInUser.getInstance().getHighScore());
        }
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        MockNetwork.isOnline = isOnline;
    }

    public void setPreviouslyOnline(boolean previouslyOnline){
        MockNetwork.previouslyOnline = previouslyOnline;
    }
}
