package ch.epfl.sdp.kandle;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ch.epfl.sdp.kandle.storage.room.Converters;

import static ch.epfl.sdp.kandle.storage.room.PostDao.POSTS_TABLE_NAME;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_DATE;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_DESCRIPTION;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_EDITABLE;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_IMAGE_URL;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_IS_CLOSE_FOLLOWERS;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_LATITUDE;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_LIKERS_LIST;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_LONGITUDE;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_TYPE;
import static ch.epfl.sdp.kandle.storage.room.PostDao.POST_ATTR_USER_ID;

@Entity(tableName = POSTS_TABLE_NAME)
public class Post {
    @Ignore
    public static final int EDITABLE_TIME = 5; //you can edit your posts within 5 minutes
    @Ignore
    public static final int MILLISECONDS_PER_MINUTE = 60000;

    @Ignore
    public static final String MESSAGE = "message";
    @Ignore
    public static final String EVENT = "event";

    @PrimaryKey
    @NonNull
    private String postId;
    @ColumnInfo(name = POST_ATTR_USER_ID)
    @NonNull
    private String userId;
    @ColumnInfo(name = POST_ATTR_LATITUDE)
    private double latitude;
    @ColumnInfo(name = POST_ATTR_LONGITUDE)
    private double longitude;
    @ColumnInfo(name = POST_ATTR_LIKERS_LIST)
    @TypeConverters(Converters.class)
    private List<String> likers;
    @ColumnInfo(name = POST_ATTR_IMAGE_URL)
    private String imageURL;
    @ColumnInfo(name = POST_ATTR_DESCRIPTION)
    private String description;
    @ColumnInfo(name = POST_ATTR_EDITABLE)
    private boolean editable;
    @ColumnInfo(name = POST_ATTR_DATE)
    @TypeConverters(Converters.class)
    private Date date;
    @ColumnInfo(name = POST_ATTR_TYPE)
    private String type;
    @ColumnInfo(name = POST_ATTR_IS_CLOSE_FOLLOWERS)
    private boolean isForCloseFollowers;

    @Ignore
    public Post() {

    }

    public Post(String description, String imageURL, Date date, @NonNull String userId, double longitude, double latitude, boolean isForCloseFollowers) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.date = date;
        this.likers = new ArrayList<String>();
        this.postId = UUID.randomUUID().toString();
        this.userId = userId;
        this.imageURL = imageURL;
        this.editable = true;
        this.type = MESSAGE;
        this.isForCloseFollowers = isForCloseFollowers;
    }

    //Useful for test
    @Ignore
    public Post(String description, String imageURL, Date date, @NonNull String userId, double longitude, double latitude) {
        new Post(description, imageURL, date, userId, longitude, latitude, false);
    }

    //Useful for tests
    @Ignore
    public Post(String description, String imageURL, Date date, @NonNull String userId, @NonNull String postId) {
        this.latitude = 0;
        this.longitude = 0;
        this.description = description;
        this.date = date;
        this.likers = new ArrayList<String>();
        this.postId = postId;
        this.userId = userId;
        this.imageURL = imageURL;
        this.editable = true;
        this.isForCloseFollowers = false;
    }

    public int getLikes() {
        return likers.size();
    }

    @NonNull
    public String getPostId() {
        return postId;
    }

    public void setPostId(@NonNull String postId) {
        this.postId = postId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return (Date) date.clone();
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void likePost(String userId) {
        if (!likers.contains(userId)) {
            likers.add(userId);
        }
    }

    public List<String> getLikers() {
        return Collections.unmodifiableList(likers);
    }

    public void setLikers(List<String> likers) {
        this.likers = likers;
    }

    public void unlikePost(String userId) {
        likers.remove(userId);
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public boolean isEditable() {
        return ((new Date().getTime() - this.getDate().getTime()) / MILLISECONDS_PER_MINUTE) < EDITABLE_TIME;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Post)) {
            return false;
        }
        Post otherPost = (Post) other;
        return otherPost.getPostId().equals(getPostId());
    }

    public boolean getIsForCloseFollowers(){
        return this.isForCloseFollowers;
    }

    public void setIsForCloseFollowers(boolean isForCloseFollowers){
        this.isForCloseFollowers = isForCloseFollowers;
    }
}