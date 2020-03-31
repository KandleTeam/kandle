package ch.epfl.sdp.kandle;


public final class LoggedInUser{

    private static User instance = null;


    public static User getInstance() {

        return instance;
    }

    public static User init(User user)  {
        if (instance != null)
            throw new IllegalStateException("Instance already exists");
        instance = user;
        return instance;
    }

    public static void clear()
    {
        instance = null;
    }

}
