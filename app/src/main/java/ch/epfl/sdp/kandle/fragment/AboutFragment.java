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
        Element versionElement = new Element().setTitle("Beta Version 1.0.0");
        Element adsElement = new Element().setTitle("Advertises");
        View aboutPage = new AboutPage(this.getContext())
                .isRTL(false)
                .setDescription("Welcome to Kandle ! Share your best photos and jokes with nearby Kandlers :)")
                .setImage(R.drawable.logo)
                .addItem(versionElement)
                .addItem(adsElement)
                .addGroup("Connect with us")
                .addWebsite("https://www.google.ch/")
                .addEmail("yanisepfl@gmail.com")
                .addFacebook("Yanis Berkani")
                .addGitHub("yanisepfl")
                .addInstagram("yanisbrk")
                .create();

        return aboutPage;
    }


}
