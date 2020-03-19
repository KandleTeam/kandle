package ch.epfl.sdp.kandle;

public class User {

    private String id, username, email, fullname , normalizedUsername, profilePicUri;

    public User() {
        // Keep fields null
    }

    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.normalizedUsername = username.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNormalizedUsername() {
        return normalizedUsername;
    }

    public void setProfilePicUri(String profilePicUri) {
        this.profilePicUri = profilePicUri;
    }

    public String getProfilePicUri() {
        return profilePicUri;
    }
}