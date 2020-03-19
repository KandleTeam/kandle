package ch.epfl.sdp.kandle;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private Toolbar toolbar;
    private NavigationView mNavigationView;
    private Button mPostButton;
   // FirebaseAuth fAuth;

    // Make sure to be using androidx.appcompat.app.ActionBarDrawerToggle version.
    private ActionBarDrawerToggle drawerToggle;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        Class fragmentClass;
        Intent intent = null;
        int size = mNavigationView.getMenu().size();

        switch(menuItem.getItemId()) {




            case R.id.your_posts :
                fragmentClass = PostFragment.class;
                break;


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


            default:
                fragmentClass = PostFragment.class;
                break;



        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }



        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment,"hey").commit();

        // Highlight the selected item has been done by NavigationView

        // Set action bar title
            menuItem.setChecked(true);
            setTitle(menuItem.getTitle());
            mDrawerLayout.closeDrawers();
            // Close the navigation drawer


    }


}