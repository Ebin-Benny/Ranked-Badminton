package com.ebinbenny.ranking;

import android.app.Fragment;
import android.content.*;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.Toast;
import com.ebinbenny.ranking.Breakdown.Game;
import com.ebinbenny.ranking.Manager.DBConnection;
import com.ebinbenny.ranking.UI.InputInterfaces.Input;
import com.ebinbenny.ranking.UI.LeagueTabPager;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;

public class LeagueFragment extends Fragment implements TabLayout.OnTabSelectedListener, Toolbar.OnMenuItemClickListener
{

    public static final String PLAYER_ITEM = "com.ebinbenny.ranking.PLAYER";
    public static final String USER_ID = "com.ebinbenny.ranking.USER_ID";
    public static final String LEAGUE_ID = "com.ebinbenny.ranking.LEAGUE_ID";
    public static final String LEAGUE_NAME = "com.ebinbenny.ranking.LEAGUE_NAME";
    public static final int EDIT_MODE = 0;
    public static final int VIEW_MODE = 1;
    public static final String USER_MODE = "com.ebinbenny.ranking.USER_PERMISSION";
    private FirebaseDatabase database;
    private String userID, leagueID, leagueName;
    private ViewPager viewPager;
    private DrawerLayout drawer;
    private int userMode;
    private BeginActivity activity;
    private boolean followed = false;
    private Toolbar toolbar;

    public void setDrawer(DrawerLayout drawer)
    {
        this.drawer = drawer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        activity = (BeginActivity) getActivity();
        Bundle bundle = getArguments();
        userID = bundle.getString(USER_ID);
        leagueID = bundle.getString(LEAGUE_ID);
        leagueName = bundle.getString(LEAGUE_NAME);
        userMode = bundle.getInt(USER_MODE);
        database = FirebaseDatabase.getInstance();
        DBConnection dbConnection = new DBConnection(database, userID, leagueID);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(leagueName);
        activity.setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        toolbar.setOnMenuItemClickListener(this);

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Rankings"));
        tabLayout.addTab(tabLayout.newTab().setText("Match Log"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) rootView.findViewById(R.id.pager);

        final LeagueTabPager adapter = new LeagueTabPager(activity, activity.getSupportFragmentManager(), tabLayout.getTabCount(), dbConnection, userMode);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(this);

        if (userMode == EDIT_MODE)
        {
            Input input = new Input(Game.BADMINTON, dbConnection, activity, rootView, activity);
            dbConnection.addInputInterface(input);
        } else
        {
            final DatabaseReference leagueReference = database.getReference().child("Leagues").child(leagueID);
            leagueReference.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.clear(Calendar.MINUTE);
                    cal.clear(Calendar.SECOND);
                    cal.clear(Calendar.MILLISECOND);
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                    if (dataSnapshot.child("Week").exists())
                    {
                        long lastWeek = dataSnapshot.child("Week").getValue(Long.class);
                        ArrayList<Long> weeks = (ArrayList<Long>) dataSnapshot.child("Weeks").getValue();
                        ArrayList<Integer> counts = (ArrayList<Integer>) dataSnapshot.child("Views").getValue();
                        if (lastWeek == cal.getTimeInMillis())
                        {
                            int views = dataSnapshot.child("Count").getValue(Integer.class);
                            views++;
                            counts.set(counts.size() - 1, views);
                            leagueReference.child("Count").setValue(views);
                            leagueReference.child("Views").setValue(counts);
                        } else
                        {
                            weeks.add(cal.getTimeInMillis());
                            counts.add(0);
                            leagueReference.child("Week").setValue(cal.getTimeInMillis());
                            leagueReference.child("Count").setValue(0);
                            leagueReference.child("Weeks").setValue(weeks);
                            leagueReference.child("Views").setValue(counts);
                        }
                    } else
                    {
                        ArrayList<Long> weeks = new ArrayList<>();
                        weeks.add(cal.getTimeInMillis());
                        ArrayList<Integer> counts = new ArrayList<>();
                        counts.add(0);
                        leagueReference.child("Week").setValue(cal.getTimeInMillis());
                        leagueReference.child("Count").setValue(0);
                        leagueReference.child("Weeks").setValue(weeks);
                        leagueReference.child("Views").setValue(counts);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

            FloatingActionButton inputButton = (FloatingActionButton) rootView.findViewById(R.id.input_button);
            inputButton.setVisibility(View.GONE);
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator);
            coordinatorLayout.setVisibility(View.GONE);
            checkIfFollowed();
        }
        return rootView;
    }

    public void checkIfFollowed()
    {
        if (activity.getUserID() != null)
        {
            final DatabaseReference followedLeagues = database.getReference().child("Users").child(activity.getUserID()).child("Followed");
            followedLeagues.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot data : dataSnapshot.getChildren())
                    {
                        if (data.getKey().equals(leagueID))
                        {
                            followed = true;
                            break;
                        }
                    }

                    MenuItem follow = toolbar.getMenu().findItem(R.id.action_follow);
                    if (follow != null)
                    {
                        if (followed)
                            follow.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_star_white_24dp));
                        else
                            follow.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_star_border_white_24dp));
                        if (userMode == VIEW_MODE) follow.setVisible(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    public int getUserMode()
    {
        return userMode;
    }

    public void logOut()
    {
        followed = false;
        MenuItem follow = toolbar.getMenu().findItem(R.id.action_follow);
        if (follow != null)
        {
            if (userMode == VIEW_MODE) follow.setVisible(false);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        menuInflater.inflate(R.menu.toolbar_menu, menu);
        if (userMode == EDIT_MODE)
        {
            MenuItem followLeague = menu.findItem(R.id.action_follow);
            followLeague.setVisible(false);
        } else
        {
            MenuItem deleteLeague = menu.findItem(R.id.action_delete);
            deleteLeague.setVisible(false);
            MenuItem follow = menu.findItem(R.id.action_follow);
            if (activity.getUserID() != null)
            {
                if (followed)
                    follow.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_star_white_24dp));
                else
                    follow.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_star_border_white_24dp));
            } else follow.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_delete)
        {
            new AlertDialog.Builder(activity)
                    .setTitle("Confirm Delete?")
                    .setMessage("Are you sure you want to delete this league?")
                    .setPositiveButton("Delete League", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            database.getReference().child("Users").child(userID).child("LeagueInfo").child(leagueID).removeValue();
                            database.getReference().child("Users").child(userID).child("Leagues").child(leagueID).removeValue();
                            database.getReference().child("Leagues").child(leagueID).removeValue();
                            activity.openViewFragment(0);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        } else if (id == R.id.action_copy_id)
        {
            ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("LeagueID", leagueID);
            clipboardManager.setPrimaryClip(clipData);
            Toast toast = Toast.makeText(activity, "League ID copied to clipboard", Toast.LENGTH_SHORT);
            toast.show();
        } else if (id == R.id.action_follow)
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference followedLeagues = database.getReference().child("Users").child(activity.getUserID()).child("Followed");
            if (followed)
            {
                followedLeagues.child(leagueID).removeValue();
                item.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_star_border_white_24dp));
                followed = false;
            } else
            {
                followedLeagues.child(leagueID).child("Name").setValue(leagueName);
                followedLeagues.child(leagueID).child("UserID").setValue(userID);
                item.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_star_white_24dp));
                followed = true;
            }
        } else if (id == R.id.action_share)
        {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            String dynamicLink = "https://uce33.app.goo.gl/?apn=com.ebinbenny.ranking&sd=Ranked&st=Ranked&link=https://uce33.app.goo.gl/league?leagueID=" + leagueID;
            share.putExtra(Intent.EXTRA_SUBJECT, leagueName);
            share.putExtra(Intent.EXTRA_TEXT, dynamicLink);
            startActivity(Intent.createChooser(share, "Share League!"));
        }
        return false;
    }
}
