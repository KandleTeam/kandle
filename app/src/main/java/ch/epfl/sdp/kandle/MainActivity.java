package ch.epfl.sdp.kandle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;


import androidx.fragment.app.FragmentTransaction;

import ch.epfl.sdp.kandle.ImagePicker.ImagePicker;
import ch.epfl.sdp.kandle.dependencies.Database;
import ch.epfl.sdp.kandle.dependencies.DependencyManager;
import ch.epfl.sdp.kandle.fragment.AboutFragment;
import ch.epfl.sdp.kandle.fragment.MapFragment;
//import ch.epfl.sdp.kandle.Fragment.ProfileFragment;
import ch.epfl.sdp.kandle.fragment.ProfileFragment;
import ch.epfl.sdp.kandle.fragment.YourPostListFragment;
import ch.epfl.sdp.kandle.fragment.SearchFragment;
import ch.epfl.sdp.kandle.fragment.SettingsFragment;
import ch.epfl.sdp.kandle.dependencies.Authentication;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    public final static int PROFILE_PICTURE_TAG = 5;
    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView mNavigationView;
    private BottomNavigationView mBottomNavigationView;
    private Button mPostButton;

    private Authentication auth;
    private Database database;
    private ImageView mProfilePic;
    private TextView mNickname;
    private TextView mUsername;
    private ActionBarDrawerToggle drawerToggle;
    private Fragment bottomFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = DependencyManager.getAuthSystem();
        database = DependencyManager.getDatabaseSystem();
        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        mPostButton = findViewById(R.id.postButton);
        mProfilePic = mNavigationView.getHeaderView(0).findViewById(R.id.profilePicInMenu);
        mUsername = mNavigationView.getHeaderView(0).findViewById(R.id.username);
        mNickname = mNavigationView.getHeaderView(0).findViewById(R.id.nicknameInMenu);
        mUsername = mNavigationView.getHeaderView(0).findViewById(R.id.usernameInMenu);
        database.getUsername().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mUsername.setText("@" + task.getResult());
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        setupDrawerContent(mNavigationView);
        drawerToggle.syncState();
        mDrawerLayout.addDrawerListener(drawerToggle);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PostActivity.class));
            }
        });

        final FragmentManager fragmentManager = getSupportFragmentManager();
        mProfilePic.setOnClickListener(v -> {

            mPostButton.setVisibility(View.GONE);
            fragmentManager.beginTransaction().replace(R.id.flContent, ProfileFragment.newInstance(LoggedInUser.getInstance()))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            setTitle("Your Profile");
            mDrawerLayout.closeDrawers();
        });

        database.getProfilePicture().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                String imageUrl = task.getResult();
                if (imageUrl != null) {
                    mProfilePic.setTag(PROFILE_PICTURE_TAG);
                    Picasso.get().load(imageUrl).into(mProfilePic);
                }
            } else {
                //TODO handle case when user is offline (get picture from cache)
            }
        });

        database.getNickname().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String username = task.getResult();
                if (username != null) {
                    mNickname.setText(username);
                }
                Log.d("testpp", "task is successful " + username + " !");
            } else {
                //TODO handle case when user is offline (get username from cache)
                Log.d("testpp", "task is not successful");
            }
        });

    }

    /**
     * Calls the slectDrawerItem method if one of the items in the drawer menu is selected by the user
     *
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {
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
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;
        Intent intent = null;
        int size = mNavigationView.getMenu().size();
        switch (menuItem.getItemId()) {
            case R.id.logout:
                auth.signOut();

                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;

            case R.id.your_posts:
                mPostButton.setVisibility(View.VISIBLE);
                fragmentClass = YourPostListFragment.class;
                break;

            case R.id.map:
                mPostButton.setVisibility(View.VISIBLE);
                fragmentClass = MapFragment.class;
                break;

            case R.id.settings:
                fragmentClass = SettingsFragment.class;
                break;

            case R.id.about:
                fragmentClass = AboutFragment.class;
                break;

            case R.id.follow:
                mPostButton.setVisibility(View.GONE);
                fragmentClass = SearchFragment.class;
                break;

            default:
                fragmentClass = null;
                break;
        }
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.flContent, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();


            // Insert the fragment by replacing any existing fragment


        }
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();

    }
}
