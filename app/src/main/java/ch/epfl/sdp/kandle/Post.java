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

import ch.epfl.sdp.kandle.Storage.room.Converters;

import static ch.epfl.sdp.kandle.Storage.room.PostDao.POSTS_TABLE_NAME;
import static ch.epfl.sdp.kandle.Storage.room.PostDao.POST_ATTR_DATE;
import static ch.epfl.sdp.kandle.Storage.room.PostDao.POST_ATTR_DESCRIPTION;
import static ch.epfl.sdp.kandle.Storage.room.PostDao.POST_ATTR_EDITABLE;
import static ch.epfl.sdp.kandle.Storage.room.PostDao.POST_ATTR_IMAGE_URL;
import static ch.epfl.sdp.kandle.Storage.room.PostDao.POST_ATTR_LATITUDE;
import static ch.epfl.sdp.kandle.Storage.room.PostDao.POST_ATTR_LIKERS_LIST;
import static ch.epfl.sdp.kandle.Storage.room.PostDao.POST_ATTR_LONGITUDE;
import static ch.epfl.sdp.kandle.Storage.room.PostDao.POST_ATTR_USER_ID;

@Entity(tableName = POSTS_TABLE_NAME)
public class Post {


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

    @Ignore
    public Post() {

    }

    public Post(String description, String imageURL, Date date, @NonNull String userId, double longitude, double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.date = date;
        this.likers = new ArrayList<String>();
        this.postId = UUID.randomUUID().toString();
        this.userId = userId;
        this.imageURL = imageURL;
        this.editable = true;
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

    public boolean isEditable() {
        return editable;
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

}