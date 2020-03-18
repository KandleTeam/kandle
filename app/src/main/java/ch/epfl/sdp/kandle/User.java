package ch.epfl.sdp.kandle;

public class User {

    private String id, username, email, normalizedUsername;

    public User() {
        // Keep fields null
    }

    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.normalizedUsername = username.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public String getNormalizedUsername() {
        return normalizedUsername;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



}