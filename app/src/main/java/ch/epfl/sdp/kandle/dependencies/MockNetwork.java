package ch.epfl.sdp.kandle.dependencies;


import ch.epfl.sdp.kandle.network.NetworkState;

public class MockNetwork implements NetworkState {
    private static boolean isOnline;

    public MockNetwork(boolean isOnline) {
        MockNetwork.isOnline = isOnline;
    }

    @Override
    public boolean isConnected() {
        return isOnline;
    }


    public void setIsOnline(boolean isOnline) {
        MockNetwork.isOnline = isOnline;
    }
}
