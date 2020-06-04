package ch.epfl.sdp.kandle.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import ch.epfl.sdp.kandle.R;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Element versionElement = new Element().setTitle(getString(R.string.appVersion));
        Element adsElement = new Element().setTitle(getString(R.string.advertisements));
        View aboutPage = new AboutPage(this.getContext())
                .isRTL(false)
                .setDescription(getString(R.string.appDescription))
                .setImage(R.drawable.logo)
                .addItem(versionElement)
                .addItem(adsElement)
                .addGroup(getString(R.string.joinUs))
                .addWebsite(getString(R.string.website))
                .addEmail(getString(R.string.email))
                .addFacebook(getString(R.string.facebook))
                .addGitHub(getString(R.string.github))
                .addInstagram(getString(R.string.instagram))
                .create();

        return aboutPage;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        this.getActivity().setTitle(R.string.about_us_item);
    }



}
