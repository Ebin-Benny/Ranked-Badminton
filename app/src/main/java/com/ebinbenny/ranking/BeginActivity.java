package com.ebinbenny.ranking;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.*;

import java.util.HashMap;


public class BeginActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener
{

    public static final int RC_SIGN_IN = 9001;
    public static final int LEAGUE_ITEM_ID = 0x111111;
    public static final int FOLLOWED_LEAGUE_ITEM_ID = 0x222222;
    public static final String VIEW_FRAGMENT = "View";
    public static final String LEAGUE_FRAGMENT = "League";
    public static final String CREATE_FRAGMENT = "Create";
    private LinearLayout userInfoLayout;
    private TextView userEmail;
    private SignInButton signInButton;
    private String userID = null;
    private GoogleApiClient apiClient;
    private DrawerLayout drawer;
    private FirebaseDatabase database;
    private NavigationView navigationView;
    private HashMap<String, String> leagueKeys;
    private HashMap<String, String[]> followedLeagueKeys;
    private DatabaseReference leagueNameReference, followedLeagueReference;
    private ValueEventListener leagueListener, followedLeagueListener;
    private boolean dataLoadedFully = false, myLeaguesLoaded = false, followedLeaguesLoaded = false;
    private DataSnapshot myLeagues, followedLeagues;
    private GoogleSignInAccount signInAccount;
    private MenuItem createLeague;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database = FirebaseDatabase.getInstance();
        leagueKeys = new HashMap<>();
        followedLeagueKeys = new HashMap<>();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        signInButton = (SignInButton) header.findViewById(R.id.sign_in);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        userInfoLayout = (LinearLayout) header.findViewById(R.id.user_info_layout);
        userEmail = (TextView) userInfoLayout.findViewById(R.id.user_email);
        createLeague = navigationView.getMenu().findItem(R.id.nav_create);
        createLeague.setVisible(false);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait, Signing in");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestId().build();
        apiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        OptionalPendingResult<GoogleSignInResult> silentIntent = Auth.GoogleSignInApi.silentSignIn(apiClient);

        silentIntent.setResultCallback(new ResultCallbacks<GoogleSignInResult>()
        {
            @Override
            public void onSuccess(@NonNull GoogleSignInResult signInResult)
            {
                updateInfo(signInResult.getSignInAccount());
                Intent intent = getIntent();
                if (intent != null)
                {
                    String leagueID = intent.getStringExtra("leagueID");
                    if (leagueID != null)
                        viewLeague(leagueID);
                    else
                    {
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Status status)
            {
                Intent intent = getIntent();
                if (intent != null)
                {
                    String leagueID = intent.getStringExtra("leagueID");
                    if (leagueID != null)
                        viewLeague(leagueID);
                    else
                    {

                    }
                }
            }
        });

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        ViewFragment fragment = new ViewFragment();
        fragment.setDrawer(drawer);
        transaction.add(R.id.container, fragment, VIEW_FRAGMENT);
        transaction.commit();

        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }


    public ValueEventListener createLeagueListener()
    {
        return new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                myLeaguesLoaded = true;
                if (!dataLoadedFully) myLeagues = dataSnapshot;

                if (followedLeaguesLoaded && !dataLoadedFully)
                {
                    doneLoading();
                } else if (dataLoadedFully)
                {
                    leagueKeys.clear();
                    navigationView.getMenu().findItem(R.id.my_leagues).getSubMenu().clear();
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        String name = (String) data.child("Name").getValue();
                        String key = data.getKey();
                        navigationView.getMenu().findItem(R.id.my_leagues).getSubMenu().add(R.id.my_leagues, LEAGUE_ITEM_ID, 1, name);
                        leagueKeys.put(name, key);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
    }

    public ValueEventListener createFollowedLeagueListener()
    {
        return new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                followedLeaguesLoaded = true;
                if (!dataLoadedFully) followedLeagues = dataSnapshot;

                if (myLeaguesLoaded && !dataLoadedFully)
                {
                    doneLoading();
                } else if (dataLoadedFully)
                {
                    followedLeagueKeys.clear();
                    navigationView.getMenu().findItem(R.id.followed_leagues).getSubMenu().clear();
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        String name = (String) data.child("Name").getValue();
                        String userID = (String) data.child("UserID").getValue();
                        String key = data.getKey();
                        navigationView.getMenu().findItem(R.id.followed_leagues).getSubMenu().add(R.id.followed_leagues, FOLLOWED_LEAGUE_ITEM_ID, 1, name);
                        followedLeagueKeys.put(name, new String[]{key, userID});
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
    }

    public String getUserID()
    {
        return userID;
    }

    public void openViewFragment(long delay)
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                ViewFragment fragment = new ViewFragment();
                fragment.setDrawer(drawer);
                transaction.replace(R.id.container, fragment, VIEW_FRAGMENT);
                transaction.commit();
            }
        }, delay);
    }

    public void openLeagueFragment(final String leagueName, final String leagueID, final String userID, long delay)
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                LeagueFragment fragment = new LeagueFragment();
                fragment.setDrawer(drawer);
                Bundle args = new Bundle();
                args.putString(LeagueFragment.USER_ID, userID);
                args.putString(LeagueFragment.LEAGUE_ID, leagueID);
                args.putString(LeagueFragment.LEAGUE_NAME, leagueName);
                args.putInt(LeagueFragment.USER_MODE, LeagueFragment.VIEW_MODE);
                fragment.setArguments(args);
                transaction.replace(R.id.container, fragment, LEAGUE_FRAGMENT);
                transaction.commit();
            }
        }, delay);
    }

    public void openLeagueFragment(final String leagueName, final String leagueID, long delay)
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                LeagueFragment fragment = new LeagueFragment();
                fragment.setDrawer(drawer);
                Bundle args = new Bundle();
                args.putString(LeagueFragment.USER_ID, userID);
                args.putString(LeagueFragment.LEAGUE_ID, leagueID);
                args.putString(LeagueFragment.LEAGUE_NAME, leagueName);
                args.putInt(LeagueFragment.USER_MODE, LeagueFragment.EDIT_MODE);
                fragment.setArguments(args);
                transaction.replace(R.id.container, fragment, LEAGUE_FRAGMENT);
                transaction.commit();
            }
        }, delay);
    }

    public void openCreateFragment(long delay)
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                CreateFragment fragment = new CreateFragment();
                fragment.setDrawer(drawer);
                transaction.replace(R.id.container, fragment, CREATE_FRAGMENT);
                transaction.commit();
            }
        }, delay);
    }

    private void updateInfo(GoogleSignInAccount account)
    {
        dialog.show();
        dataLoadedFully = false;
        myLeagues = null;
        followedLeagues = null;
        followedLeaguesLoaded = false;
        myLeaguesLoaded = false;
        this.signInAccount = account;
        userID = signInAccount != null ? signInAccount.getId() : null;
        leagueNameReference = database.getReference().child("Users").child(userID).child("LeagueInfo");
        leagueListener = createLeagueListener();
        leagueNameReference.addValueEventListener(leagueListener);
        followedLeagueReference = database.getReference().child("Users").child(userID).child("Followed");
        followedLeagueListener = createFollowedLeagueListener();
        followedLeagueReference.addValueEventListener(createFollowedLeagueListener());
    }

    public void doneLoading()
    {
        userInfoLayout.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.INVISIBLE);
        navigationView.getMenu().findItem(R.id.sign_out).setVisible(true);
        userEmail.setText(signInAccount != null ? signInAccount.getEmail() : null);
        followedLeagueKeys.clear();
        navigationView.getMenu().findItem(R.id.followed_leagues).getSubMenu().clear();
        for (DataSnapshot data : followedLeagues.getChildren())
        {
            String name = (String) data.child("Name").getValue();
            String userID = (String) data.child("UserID").getValue();
            String key = data.getKey();
            navigationView.getMenu().findItem(R.id.followed_leagues).getSubMenu().add(R.id.followed_leagues, FOLLOWED_LEAGUE_ITEM_ID, 1, name);
            followedLeagueKeys.put(name, new String[]{key, userID});
        }
        leagueKeys.clear();
        navigationView.getMenu().findItem(R.id.my_leagues).getSubMenu().clear();
        for (DataSnapshot data : myLeagues.getChildren())
        {
            String name = (String) data.child("Name").getValue();
            String key = data.getKey();
            navigationView.getMenu().findItem(R.id.my_leagues).getSubMenu().add(R.id.my_leagues, LEAGUE_ITEM_ID, 1, name);
            leagueKeys.put(name, key);
        }
        LeagueFragment fragment = (LeagueFragment) getFragmentManager().findFragmentByTag(LEAGUE_FRAGMENT);
        if (fragment != null && fragment.isVisible())
        {
            fragment.checkIfFollowed();
        }
        createLeague.setVisible(true);
        myLeagues = null;
        followedLeagues = null;
        dataLoadedFully = true;
        dialog.dismiss();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (signInResult.isSuccess())
            {
                updateInfo(signInResult.getSignInAccount());
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            FragmentManager fragmentManager = getFragmentManager();
            ViewFragment viewFragment = (ViewFragment) fragmentManager.findFragmentByTag(VIEW_FRAGMENT);
            if (viewFragment != null && viewFragment.isVisible())
            {
                super.onBackPressed();

            } else
            {
                openViewFragment(0);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.nav_create)
        {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            openCreateFragment(300);
        } else if (id == R.id.nav_view)
        {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            openViewFragment(300);
        } else if (id == R.id.sign_out)
        {
            Auth.GoogleSignInApi.signOut(apiClient).setResultCallback(new ResultCallbacks<Status>()
            {
                @Override
                public void onSuccess(@NonNull Status status)
                {
                    navigationView.getMenu().findItem(R.id.sign_out).setVisible(false);
                    navigationView.getMenu().findItem(R.id.my_leagues).getSubMenu().clear();
                    navigationView.getMenu().findItem(R.id.followed_leagues).getSubMenu();
                    userInfoLayout.setVisibility(View.INVISIBLE);
                    signInButton.setVisibility(View.VISIBLE);
                    userID = null;
                    userEmail.setText("");
                    leagueKeys = new HashMap<>();
                    followedLeagueKeys = new HashMap<>();
                    leagueNameReference.removeEventListener(leagueListener);
                    followedLeagueReference.removeEventListener(followedLeagueListener);
                    createLeague.setVisible(false);
                    FragmentManager fragmentManager = getFragmentManager();
                    LeagueFragment leagueFragment = (LeagueFragment) fragmentManager.findFragmentByTag(LEAGUE_FRAGMENT);
                    if (leagueFragment != null && leagueFragment.isVisible())
                    {
                        if (leagueFragment.getUserMode() == LeagueFragment.VIEW_MODE)
                            leagueFragment.logOut();
                        else openViewFragment(300);
                    } else openViewFragment(300);
                }

                @Override
                public void onFailure(@NonNull Status status)
                {

                }
            });
            return true;
        } else if (id == LEAGUE_ITEM_ID)
        {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            String leagueID = leagueKeys.get(item.getTitle().toString());
            openLeagueFragment(item.getTitle().toString(), leagueID, 300);
        } else if (id == FOLLOWED_LEAGUE_ITEM_ID)
        {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            String[] data = followedLeagueKeys.get(item.getTitle().toString());
            openLeagueFragment(item.getTitle().toString(), data[0], data[1], 300);
        }
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    public void viewLeague(final String leagueID)
    {
        final DatabaseReference leagueReference = database.getReference().child("Leagues");
        leagueReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(leagueID).exists())
                {
                    String userID = (String) dataSnapshot.child(leagueID).child("ID").getValue();
                    int type = dataSnapshot.child(leagueID).child("Type").getValue(Integer.class);
                    String name = (String) dataSnapshot.child(leagueID).child("Name").getValue();
                    openLeagueFragment(name, leagueID, userID, 0);
                } else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "No such league exists.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
