package ch.epfl.sdp.kandle;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;

import ch.epfl.sdp.kandle.Fragment.AboutFragment;
import ch.epfl.sdp.kandle.Fragment.MapFragment;
//import ch.epfl.sdp.kandle.Fragment.ProfileFragment;
import ch.epfl.sdp.kandle.Fragment.YourPostListFragment;
import ch.epfl.sdp.kandle.Fragment.SearchFragment;
import ch.epfl.sdp.kandle.Fragment.SettingsFragment;
import ch.epfl.sdp.kandle.DependencyInjection.Authentication;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView mNavigationView;
    private BottomNavigationView mBottomNavigationView;
    private Button mPostButton;
    private Authentication auth;

    // Make sure to be using androidx.appcompat.app.ActionBarDrawerToggle version.
    private ActionBarDrawerToggle drawerToggle;

    private Fragment bottomFragment = null;

    //



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = Authentication.getAuthenticationSystem();
        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        mPostButton = findViewById(R.id.postButton);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        setupDrawerContent(mNavigationView);
        drawerToggle.syncState();
        mDrawerLayout.addDrawerListener(drawerToggle);


        //





       /* mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.nav_map:
                                bottomFragment = new MapFragment();
                                break;
                            case R.id.nav_search:
                                try {
                                    bottomFragment = SearchFragment.class.newInstance();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case R.id.nav_addPost:
                                startActivity(new Intent(MainActivity.this, PostActivity.class));
                                break;
                            case R.id.nav_profile:
                                //bottomFragment = new PostFragment();
                                break;
                        }

                            if (bottomFragment!=null) {
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,bottomFragment).commit();
                            }

                        return true;
                    }
                }
        );

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();

*/


    }

    /*Listens if a navigation item is selected
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked

        Fragment fragment = null;
        Class fragmentClass = null;
        Intent intent = null;
        int size = mNavigationView.getMenu().size();

        switch(menuItem.getItemId()) {






            //For activities


                /*
            case R.id.settings :
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
             */
            case R.id.logout :
                auth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();

                break;


            case R.id.your_posts:
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



        if (fragment!=null) {


            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();


            // Insert the fragment by replacing any existing fragment

        }

        // Highlight the selected item has been done by NavigationView

        // Set action bar title
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();
        // Close the navigation drawer


    }







}