package ch.epfl.sdp.kandle.dependencies;


import ch.epfl.sdp.kandle.network.NetworkState;

public class MockNetwork implements NetworkState {
    private static boolean isOnline;

    /**
     * Creates a MockNetwork object
     * @param isOnline
     */
    public MockNetwork(boolean isOnline) {
        MockNetwork.isOnline = isOnline;
    }

    @Override
    public boolean isConnected() {
        return isOnline;
    }

    /**
     * Sets the isOnline parameter
     * @param isOnline
     */
    public void setIsOnline(boolean isOnline) {
        MockNetwork.isOnline = isOnline;
    }
}
