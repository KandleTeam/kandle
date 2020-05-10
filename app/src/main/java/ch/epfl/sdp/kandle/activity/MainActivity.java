package ch.epfl.sdp.kandle.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import ch.epfl.sdp.kandle.LoggedInUser;
import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.dependencies.Authentication;
import ch.epfl.sdp.kandle.fragment.AboutFragment;
import ch.epfl.sdp.kandle.fragment.AchievementFragment;
import ch.epfl.sdp.kandle.fragment.FollowingPostsFragment;
import ch.epfl.sdp.kandle.fragment.MapViewFragment;
import ch.epfl.sdp.kandle.fragment.ProfileFragment;
import ch.epfl.sdp.kandle.fragment.SearchFragment;
import ch.epfl.sdp.kandle.fragment.SettingsFragment;
import ch.epfl.sdp.kandle.fragment.YourPostListFragment;

import static ch.epfl.sdp.kandle.dependencies.DependencyManager.getAuthSystem;

public class MainActivity extends AppCompatActivity {


    public final static int PROFILE_PICTURE_TAG = 5;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private ImageView mProfilePic;
    private TextView mNickname;
    private Authentication auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = getAuthSystem();
        Toolbar toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        mProfilePic = mNavigationView.getHeaderView(0).findViewById(R.id.profilePicInMenu);
        mNickname = mNavigationView.getHeaderView(0).findViewById(R.id.nicknameInMenu);
        TextView mUsername = mNavigationView.getHeaderView(0).findViewById(R.id.usernameInMenu);
        mUsername.setText(String.format("@%s", auth.getCurrentUser().getUsername()));


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        setupDrawerContent(mNavigationView);
        drawerToggle.syncState();
        mDrawerLayout.addDrawerListener(drawerToggle);

        createNewFragmentInstance(MapViewFragment.class);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        setTitle(mNavigationView.getCheckedItem().getTitle());

        mProfilePic.setOnClickListener(v -> {

            fragmentManager.beginTransaction().replace(R.id.flContent, ProfileFragment.newInstance(auth.getCurrentUser()))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            setTitle(getString(R.string.yourProfile));
            mNavigationView.getCheckedItem().setChecked(false);
            mDrawerLayout.closeDrawers();
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        String imageUrl = auth.getCurrentUser().getImageURL();

        if (imageUrl != null) {
            mProfilePic.setTag(PROFILE_PICTURE_TAG);
            Picasso.get().load(imageUrl).into(mProfilePic);
        }

        String username = auth.getCurrentUser().getNickname();
        if (username != null) {
            mNickname.setText(username);
        }

    }

    /**
     * Hides unavailable menus from navigation, and sets the selection listener
     *
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {

        if (LoggedInUser.isGuestMode()) {
            navigationView.getMenu().findItem(R.id.your_posts).setVisible(false);
            navigationView.getMenu().findItem(R.id.follow).setVisible(false);
            navigationView.getMenu().findItem(R.id.following_posts).setVisible(false);
            navigationView.getMenu().findItem(R.id.achievements).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }

    /**
     * This method allows to navigate between different fragment from the main activity
     *
     * @param menuItem
     */
    private void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.logout:
                auth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;

            case R.id.your_posts:
                openFragment(YourPostListFragment.class);
                break;

            case R.id.map_support:
                openFragment(MapViewFragment.class);
                break;

            case R.id.settings:
                openFragment(SettingsFragment.class);
                break;

            case R.id.about:
                openFragment(AboutFragment.class);
                break;

            case R.id.follow:
                openFragment(SearchFragment.class);
                break;

            case R.id.achievements:
                openFragment(AchievementFragment.class);
                break;

            case R.id.following_posts:
                openFragment(FollowingPostsFragment.class);
                break;

            default:
                throw new IllegalArgumentException("There is a missing MenuItem case!");
        }
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();
    }

    private void openFragment(Class fragmentClass) {

        createNewFragmentInstance(fragmentClass);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.flContent, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Returns the current fragment
     *
     * @return the current fragment
     */
    public Fragment getCurrentFragment() {
        return fragment;
    }


    private void createNewFragmentInstance(Class fragmentClass) {
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}