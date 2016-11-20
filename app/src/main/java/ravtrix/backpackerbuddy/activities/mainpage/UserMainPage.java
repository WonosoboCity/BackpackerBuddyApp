package ravtrix.backpackerbuddy.activities.mainpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import ravtrix.backpackerbuddy.R;
import ravtrix.backpackerbuddy.activities.SettingsActivity;
import ravtrix.backpackerbuddy.fragments.ausercountryposts.AUserCountryPostsFragment;
import ravtrix.backpackerbuddy.fragments.destination.DestinationFragment;
import ravtrix.backpackerbuddy.fragments.findbuddy.FindBuddyTabFragment;
import ravtrix.backpackerbuddy.fragments.mainfrag.CountryTabFragment;
import ravtrix.backpackerbuddy.fragments.message.MessagesFragment;
import ravtrix.backpackerbuddy.fragments.userprofile.UserProfileFragment;
import ravtrix.backpackerbuddy.helpers.Helpers;
import ravtrix.backpackerbuddy.interfacescom.FragActivityProgressBarInterface;
import ravtrix.backpackerbuddy.interfacescom.FragActivityResetDrawer;
import ravtrix.backpackerbuddy.interfacescom.FragActivitySetDrawerInterface;
import ravtrix.backpackerbuddy.interfacescom.FragActivityUpdateProfilePic;
import ravtrix.backpackerbuddy.models.UserLocalStore;

/**
 * Created by Ravinder on 3/29/16.
 */
public class UserMainPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener, FragActivitySetDrawerInterface, FragActivityProgressBarInterface,
        FragActivityResetDrawer, FragActivityUpdateProfilePic {

    private FragmentManager fragmentManager;
    private List<Fragment> fragmentList;
    private int currentPos;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    @BindView(R.id.drawer_layout)
    protected DrawerLayout drawerLayout;
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.nav_view)
    protected NavigationView navigationView;
    private ImageButton settingsButton;
    private CircleImageView profilePic;
    @BindView(R.id.spinner_main)
    protected ProgressBar progressBar;
    private RefWatcher refWatcher; // Leakcanary memory leak watcher for fragments
    private UserLocalStore userLocalStore;
    private boolean refreshProfilePic = true;
    private boolean userHitHome = false;
    private BroadcastReceiver broadcastReceiver;
    private CountryTabFragment countryTabFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //LeakCanary.install(getApplication());
        ButterKnife.bind(this);
        userLocalStore = new UserLocalStore(this);

        //Check for Google Play Services APK
        //If the device doesn’t have a compatible Google Play services APK, your app can call GooglePlayServicesUtil.getErrorDialog()
        //checkRuntimePermissionAvail();

        View header = navigationView.inflateHeaderView(R.layout.nav_header_main);
        settingsButton = (ImageButton) header.findViewById(R.id.settingsButton);
        profilePic = (CircleImageView) header.findViewById(R.id.profile_image);

        if ((userLocalStore.getLoggedInUser().getUserImageURL().equals("0"))) {
            Picasso.with(this)
                    .load("http://s3.amazonaws.com/37assets/svn/765-default-avatar.png")
                    .noFade()
                    .into(profilePic);
        } else {
            Picasso.with(this).load("http://backpackerbuddy.net23.net/profile_pic/" +
                    userLocalStore.getLoggedInUser().getUserID() + ".JPG").noFade().into(profilePic);
        }

        // If bundle is not null. The filter fragment sent a bundle to update main
        this.countryTabFragment = new CountryTabFragment();

        if (!Helpers.isBundleNull(this)) {
            // Pass bundle onto the tab fragment
            this.countryTabFragment.setHasBundle(getIntent().getExtras());
        }

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        profilePic.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        setSupportActionBar(toolbar);
        setUpFragments();
        screenStartUpState();
        setNavigationDrawerIcons();
        toggleListener();

        String token = FirebaseInstanceId.getInstance().getToken();
        System.out.println("TOKEN IS: " + token);

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.navActivity:
                navigationView.getMenu().getItem(0).setChecked(true);
                currentPos = 0;
                setTitle("Activity");
                break;
            case R.id.navFindBuddy:
                currentPos = 1;
                setTitle("Find Buddy");
                break;
            case R.id.navInbox:
                currentPos = 2;
                setTitle("Inbox");
                break;
            case R.id.navDestination:
                currentPos = 3;
                setTitle("Manage Destination");
                break;
            default:
        }
        changeFragment(this.currentPos);

        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.settingsButton:
                startActivityForResult(new Intent(this, SettingsActivity.class), 2);
                break;
            case R.id.profile_image:
                currentPos = 4;
                setTitle("User Profile");
                changeFragment(this.currentPos);

                // Removed all checked items from navigation because profile is not on it
                resetNavigationDrawer();
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            refreshProfilePic = false;
        }
    }

    // Set up the fragments
    private void setUpFragments() {
        fragmentList = new ArrayList<>();
        fragmentList.add(this.countryTabFragment);
        fragmentList.add(new FindBuddyTabFragment());
        fragmentList.add(new MessagesFragment());
        fragmentList.add(new AUserCountryPostsFragment());
        fragmentList.add(new UserProfileFragment());
        fragmentList.add(new DestinationFragment());
    }

    // Start up state
    //Title Idex, with closed navigation drawer and default fragment 1
    private void screenStartUpState() {
        setTitle("Activity");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragmentList.get(0)).addToBackStack(null).commit();
    }

    private void changeFragment(final int currentPos) {
        drawerLayout.closeDrawer(GravityCompat.START);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container,
                fragmentList.get(currentPos)).commit();
        // Delay to avoid lag when changing between option in navigation drawer
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container,
                        fragmentList.get(currentPos)).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        }, 150);*/
    }

    // Listens to when drawer navigation is opened or closed.
    // Disabled menu items on action bar
    private void toggleListener() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawerOpened, R.string.drawerClosed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync it based on if navigation drawer is selected or not.
        actionBarDrawerToggle.syncState();
    }

    public static RefWatcher getRefWatcher(Context context) {
        UserMainPage userMainPage = (UserMainPage) context.getApplicationContext();
        return userMainPage.refWatcher; // Can access private data. Fragments share the same context
    }

    private void setNavigationDrawerIcons() {
        navigationView.getMenu().getItem(0).setIcon(R.drawable.ic_record_voice_over_black_24dp);
        navigationView.getMenu().getItem(1).setIcon(R.drawable.ic_person_pin_circle_black_24dp);
        navigationView.getMenu().getItem(2).setIcon(R.drawable.ic_chat_bubble_black_24dp);
        navigationView.getMenu().getItem(3).setIcon(R.drawable.ic_assignment_black_24dp);
    }

    private void resetNavigationDrawer() {
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    /*
    private void checkRuntimePermissionAvail() {
        // Check for user's SDK Version. SDK version >= Marshmallow need permission access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 1);
            }
        } else {
            startService();
        }
    }


    // Case for users with grant access needed. Location access granted.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService();
                }
                break;
        }
    }


    // User with granted access can access the location now. Start the location background service
    private void startService() {
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        startService(intent);
    }*/

    public List<Fragment> getFragList() {
        return this.fragmentList;
    }

    public DrawerLayout getDrawerLayout() {
        return this.drawerLayout;
    }

    @Override
    public void onBackPressed() {
        Helpers.showAlertDialogWithTwoOptions(UserMainPage.this, this, "Are you sure you want to exit?", "Yes", "No");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // If user hit or hold home button, do not refresh profile picture
        this.userHitHome = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshProfilePic = !userHitHome;

        if ((userLocalStore.getLoggedInUser().getUserImageURL() == null) ||
                (userLocalStore.getLoggedInUser().getUserImageURL().equals("0"))) {
            Picasso.with(this).load("http://i.imgur.com/268p4E0.jpg").noFade().into(profilePic);
        } else {
            if (refreshProfilePic) {
                Picasso.with(this)
                        .load("http://backpackerbuddy.net23.net/profile_pic/" +
                                userLocalStore.getLoggedInUser().getUserID() + ".JPG")
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(profilePic);
            } else {
                Picasso.with(this)
                        .load("http://backpackerbuddy.net23.net/profile_pic/" +
                                userLocalStore.getLoggedInUser().getUserID() + ".JPG")
                        .into(profilePic);
            }
        }
    }

    // Below override methods are called from fragment child to access activity
    @Override
    public void setDrawerSelected(int position) {
        navigationView.getMenu().getItem(position).setChecked(true);
    }

    @Override
    public void onResetDrawer() {
        resetNavigationDrawer();
        setTitle("User Profile");
    }

    @Override
    public void setProgressBarVisible() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setProgressBarInvisible() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUpdateProfilePic() {
        // User hitting back button from changing profile picture in EditPhotoActivity.
        // Invoked to set new profile picture
        this.userHitHome = false;
    }

}
