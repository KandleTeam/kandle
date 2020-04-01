package ch.epfl.sdp.kandle.CustomMarker;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class CustomMarkerItem implements ClusterItem {

    public enum Type {
        USER,
        POST
    }

    private LatLng position;
    private String title;
    private String snippet;
    private String profilePictureUrl;
    private Type type;

    public CustomMarkerItem(LatLng latLng, String title, String snippet, String profilePictureUrl, Type type) {
        this.position = latLng;
        this.title = title;
        this.snippet = snippet;
        this.profilePictureUrl = profilePictureUrl;
        this.type=type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CustomMarkerItem(){
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
