package ch.epfl.sdp.kandle.fragment;

import android.content.IntentSender;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ch.epfl.sdp.kandle.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);


        FusedLocationProviderClient loc = LocationServices.getFusedLocationProviderClient(getContext());

        TextView text = view.findViewById(R.id.placeholder);
        Button btn = view.findViewById(R.id.updateLocation);

        btn.setOnClickListener(v -> {
            loc.getLastLocation().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    text.setText(task.getResult().toString());
                } else {
                    Exception e = task.getException();
                    text.setText(e.getMessage());
                    if(task.getException() instanceof ResolvableApiException) {
                        ResolvableApiException ex = (ResolvableApiException) e;
                        try {
                            ex.startResolutionForResult(getActivity(), 1);
                        } catch (IntentSender.SendIntentException exc) {
                            Log.d("error: ", ex.getMessage());
                        }
                    }
                }
            });
        });

        return view;
    }
}
