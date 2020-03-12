package ch.epfl.sdp.kandle;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.AppBarLayout;

import androidx.fragment.app.FragmentManager;
import ch.epfl.sdp.kandle.Fragment.MapFragment;
//import ch.epfl.sdp.kandle.Fragment.ProfileFragment;
import ch.epfl.sdp.kandle.Fragment.SearchFragment;



import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView mNavigationView;
    private BottomNavigationView mBottomNavigationView;
   // FirebaseAuth fAuth;

    // Make sure to be using androidx.appcompat.app.ActionBarDrawerToggle version.
    private ActionBarDrawerToggle drawerToggle;

    private Fragment bottomFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        setupDrawerContent(mNavigationView);
        drawerToggle.syncState();
        mDrawerLayout.addDrawerListener(drawerToggle);

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
            /*
            //For fragments
            case R.id.nav_first_fragment:
                fragmentClass = FirstFragment.class;
                break;
            case R.id.nav_second_fragment:
                fragmentClass = SecondFragment.class;
                break;
            case R.id.nav_third_fragment:
                fragmentClass = ThirdFragment.class;
                break;
            default:
                fragmentClass = FirstFragment.class;
             */

            //For activities


                /*
            case R.id.settings :
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
             */
            case R.id.logout :
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;

            case R.id.follow:
                fragmentClass = SearchFragment.class;
                break;


            default: fragmentClass = SearchFragment.class;


        }
        /*
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();


        // Insert the fragment by replacing any existing fragment
        //FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView

        // Set action bar title
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();
        // Close the navigation drawer


    }







}