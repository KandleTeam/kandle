package ch.epfl.sdp.kandle;


public final class LoggedInUser {

    private static final User GUEST = new User("guest", "guest", "guestEmail", "Guest", null);

    private static User instance = null;

    public static User getInstance() {
        return instance;
    }

    public static boolean isGuestMode() {
        return instance == GUEST;
    }

    public static User init(User user) {
        checkLoggedOut();
        instance = user;
        return instance;
    }

    public static void initGuestMode() {
        checkLoggedOut();
        instance = GUEST;
    }

    private static void checkLoggedOut() {
        if (instance != null)
            throw new IllegalStateException("Instance already exists");
    }

    public static void clear() {
        instance = null;
    }

}
