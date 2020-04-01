package ch.epfl.sdp.kandle.CustomMarker;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import ch.epfl.sdp.kandle.R;

public class ClusterManagerRenderer extends DefaultClusterRenderer<CustomMarkerItem> {



    private final IconGenerator iconGenerator;
    private ImageView profileImage;
    private final int markerWidth, markerHeight;

    public ClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<CustomMarkerItem> clusterManager) {
        super(context, map, clusterManager);
        // initialize cluster item icon generator
        iconGenerator = new IconGenerator(context.getApplicationContext());
        profileImage = new ImageView(context.getApplicationContext());
        markerWidth = 50;
        markerHeight = 50;
        profileImage.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        iconGenerator.setContentView(profileImage);

    }

    @Override
    protected void onBeforeClusterItemRendered(CustomMarkerItem item, MarkerOptions markerOptions) {

        //Picasso.with(MainActivity.this).load("http://assets3.parliament.uk/iv/main-large//ImageVault/Images/id_7382/scope_0/ImageVaultHandler.aspx.jpg").into(imageView,new InfoWindowRefresher(marker));



            //profileImage.setTag(13);
            //Picasso.get().load(item.getProfilePictureUrl()).into(profileImage);
            //Uri url = Uri.parse(item.getProfilePictureUrl());
            //profileImage.setImageURI(url);
        if (item.getType()==CustomMarkerItem.Type.USER) {
            System.out.println(item.getProfilePictureUrl());
            profileImage.setImageResource(R.drawable.ic_profil_24dp);
        }
        else {
            profileImage.setImageResource(R.drawable.ic_whatshot_24dp);
        }

        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());


    }



    //only one item so far
    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return false;
    }


}
