package ch.epfl.sdp.kandle.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import ch.epfl.sdp.kandle.R;
import ch.epfl.sdp.kandle.authentification.Authentication;
import ch.epfl.sdp.kandle.entities.user.LoggedInUser;
import ch.epfl.sdp.kandle.fragment.AboutFragment;
import ch.epfl.sdp.kandle.fragment.AchievementFragment;
import ch.epfl.sdp.kandle.fragment.EventListFragment;
import ch.epfl.sdp.kandle.fragment.FollowingPostsFragment;
import ch.epfl.sdp.kandle.fragment.MapViewFragment;
import ch.epfl.sdp.kandle.fragment.PopularUserFragment;
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
            navigationView.getMenu().findItem(R.id.incoming_events).setVisible(false);
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

            case R.id.incoming_events:
                openFragment(EventListFragment.class);
                break;

            case R.id.popularKandlers:
                openFragment(PopularUserFragment.class);
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

    // The PermissionManager works under the hood with `ActivityCompat.requestPermissions(activity, permissions, code)`
    // i.e. the parent activity request the permission to the system, and receives the request result.
    // We cannot get onRequestPermissionResult called on the fragment using the PermissionManager wrapper.
    // (although the fragment can still be the listener to the PermissionManager if it implements PermissionListener,
    // as the PermissionManager calls the specific callback PermissionListener.onPermissionResult)
    // Workaround: receive the result in the activity on behalf of a specific fragment
    //
    // see https://github.com/mapbox/mapbox-events-android/issues/395
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (fragment instanceof MapViewFragment) {
            ((MapViewFragment) fragment).getPermissionsManager().onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}