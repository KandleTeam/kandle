package ch.epfl.sdp.kandle;

import java.nio.channels.AlreadyBoundException;
import java.util.ArrayList;

public class LoggedInUser extends User {

    private ArrayList<String> postsIds = new ArrayList<>();
    private final String id,username,email;
    private String  nickname , imageURL;
    private static LoggedInUser instance = null;

    private LoggedInUser(String id, String username, String email,String nickname, String imageURL){
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.imageURL = imageURL;
    }

    public static LoggedInUser getInstance() {
        return instance;
    }

    public synchronized static LoggedInUser init(String id, String username, String email, String nickname, String imageURL) throws Exception {
        if (instance != null)
            throw new AssertionError("Instance already exists");
        instance = new LoggedInUser(id,username,email,nickname,imageURL);

        return instance;
    }

    public void clear()
    {
        instance = null;
    }

    @Override
    public String getId() {
        return id;
    }
    @Override
    public String getUsername() {
        return username;
    }
    @Override
    public String getEmail() {
        return email;
    }
    @Override
    public String getNickname() {
        return nickname;
    }
    @Override
    public void setNickname(String nickname) {

        this.nickname = nickname;
    }
    @Override
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    @Override
    public String getImageURL() {
        return imageURL;
    }
    @Override
    public ArrayList<String> getPosts(){
        return postsIds;
    }
    @Override
    public void addPostId(String postId){
        postsIds.add(postId);
    }
    @Override
    public void removePostId(String postId){
        postsIds.remove(postId);
    }

}
