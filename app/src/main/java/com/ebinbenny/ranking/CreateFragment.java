package com.ebinbenny.ranking;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;

public class CreateFragment extends Fragment
{

    private DrawerLayout drawer;
    private BeginActivity activity;

    public void setDrawer(DrawerLayout drawer)
    {
        this.drawer = drawer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.create_league, container, false);
        activity = (BeginActivity) getActivity();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle("Create League");
        activity.setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final EditText leagueNameInput = (EditText) rootView.findViewById(R.id.league_name);

        final Spinner gameTypeInput = (Spinner) rootView.findViewById(R.id.game_type_spinner);

        Button createButton = (Button) rootView.findViewById(R.id.create_button);

        createButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                if (activity.getUserID() != null)
                {
                    final String leagueName = leagueNameInput.getText().toString().trim();
                    final DatabaseReference leagueNameReference = database.getReference().child("Users").child(activity.getUserID()).child("LeagueInfo");
                    leagueNameReference.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            boolean leagueNameExists = false;
                            for (DataSnapshot data : dataSnapshot.getChildren())
                            {
                                if (data.child("Name").getValue().equals(leagueName))
                                {
                                    leagueNameExists = true;
                                    break;
                                }
                            }

                            if (!leagueNameExists)
                            {
                                String leagueKey = leagueNameReference.push().getKey();
                                int gameType = gameTypeInput.getSelectedItemPosition();
                                leagueNameReference.child(leagueKey).child("Name").setValue(leagueName);
                                leagueNameReference.child(leagueKey).child("Type").setValue(gameType);
                                DatabaseReference leagueReference = database.getReference().child("Leagues").child(leagueKey);
                                leagueReference.child("ID").setValue(activity.getUserID());
                                leagueReference.child("Name").setValue(leagueName);
                                leagueReference.child("Type").setValue(gameType);
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.HOUR_OF_DAY, 0);
                                calendar.clear(Calendar.MINUTE);
                                calendar.clear(Calendar.SECOND);
                                calendar.clear(Calendar.MILLISECOND);
                                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                                leagueReference.child("Week").setValue(calendar.getTimeInMillis());
                                leagueReference.child("Count").setValue(0);
                                ArrayList<Integer> counts = new ArrayList<>();
                                counts.add(0);
                                counts.add(0);
                                leagueReference.child("Views").setValue(counts);
                                ArrayList<Long> weeks = new ArrayList<>();
                                weeks.add(calendar.getTimeInMillis() - 604800000);
                                weeks.add(calendar.getTimeInMillis());
                                leagueReference.child("Weeks").setValue(weeks);
                                activity.openLeagueFragment(leagueName, leagueKey, 0);
                            } else
                            {
                                Toast toast = Toast.makeText(activity, "A league with that name already exists on this account.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                } else
                {
                    Toast toast = Toast.makeText(activity, "Please sign in before creating a league", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        return rootView;
    }
}
