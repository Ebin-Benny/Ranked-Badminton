package com.ebinbenny.ranking;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.firebase.database.*;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class ViewFragment extends Fragment
{
    private static final String REGEX = "[.#$\\[\\]]";
    private DrawerLayout drawer;
    private BeginActivity activity;
    private int[] colors;
    private String[] types;

    public void setDrawer(DrawerLayout drawer)
    {
        this.drawer = drawer;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.view_league, container, false);
        activity = (BeginActivity) getActivity();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        activity.setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        colors = activity.getResources().getIntArray(R.array.game_colors);
        types = activity.getResources().getStringArray(R.array.game_types);


        final EditText leagueIDInput = (EditText) rootView.findViewById(R.id.league_id);
        Button viewButton = (Button) rootView.findViewById(R.id.view_button);

        final DatabaseReference leagueReference = database.getReference().child("Leagues");

        viewButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                String leagueID = leagueIDInput.getText().toString();
                leagueID = leagueID.replaceAll(REGEX, "");
                final String finalLeagueID = leagueID;
                final ProgressDialog dialog = new ProgressDialog(activity);
                dialog.setMessage("Loading League");
                dialog.show();
                leagueReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child(finalLeagueID).exists())
                        {
                            String userID = (String) dataSnapshot.child(finalLeagueID).child("ID").getValue();
                            int type = dataSnapshot.child(finalLeagueID).child("Type").getValue(Integer.class);
                            String name = (String) dataSnapshot.child(finalLeagueID).child("Name").getValue();
                            activity.openLeagueFragment(name, finalLeagueID, userID, 0);
                        } else
                        {
                            Toast toast = Toast.makeText(activity, "No such league with that id exists.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        dialog.dismiss();
                        Toast toast = Toast.makeText(activity, "Error connecting to database.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });
        return rootView;
    }

    public View inflateLeagueView(LayoutInflater inflater, final String leagueID, final String userID, final String leagueName, int type, int view, ArrayList<Integer> views, ArrayList<Long> weeks)
    {
        View leagueView = inflater.inflate(R.layout.league_views, null);
        String leagueType = types[type];
        int color = colors[type];

        TextView leagueNameView = (TextView) leagueView.findViewById(R.id.league_name);
        leagueNameView.setText(leagueName);
        TextView leagueTypeView = (TextView) leagueView.findViewById(R.id.league_type);
        leagueTypeView.setText(leagueType);
        leagueTypeView.setTextColor(ColorStateList.valueOf(color));
        TextView leagueViewsView = (TextView) leagueView.findViewById(R.id.league_views);
        leagueViewsView.setText(String.valueOf(view));
        CardView leagueViewLayout = (CardView) leagueView.findViewById(R.id.league_view_layout);
        leagueViewLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.openLeagueFragment(leagueName, leagueID, userID, 0);
            }
        });

        GraphView graph = (GraphView) leagueView.findViewById(R.id.view_graph);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHighlightZeroLines(true);
        graph.getGridLabelRenderer().reloadStyles();

        int size = views.size();
        if (size > 10) size = 10;
        DataPoint[] dataPoints = new DataPoint[size];

        int startPoint = 0;
        if (dataPoints.length > 10) startPoint = dataPoints.length - 10;
        for (int i = 0; i < dataPoints.length; i++)
        {
            dataPoints[i] = new DataPoint(i, views.get(i + startPoint));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setColor(ContextCompat.getColor(activity, R.color.colorAccent));
        graph.getViewport().setMaxX((views.size() - 1));
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getSeries().clear();
        graph.addSeries(series);

        return leagueView;
    }

}
