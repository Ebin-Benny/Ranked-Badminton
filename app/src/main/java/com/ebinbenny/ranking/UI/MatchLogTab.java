package com.ebinbenny.ranking.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ebinbenny.ranking.Breakdown.BadmintonGame;
import com.ebinbenny.ranking.Manager.DBConnection;
import com.ebinbenny.ranking.R;
import com.ebinbenny.ranking.UI.RecyclerViewAdapters.MatchLogAdapter;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class MatchLogTab extends Fragment
{

    private DBConnection dbConnection;
    private MatchLogAdapter recyclerAdapter;
    private Context context;
    private int userMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.log_tab, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.logRecyclerView);
        recyclerAdapter = new MatchLogAdapter(BadmintonGame.class, R.layout.full_score_board, MatchLogAdapter.GameHolder.class, dbConnection.gameQuery(), context, dbConnection, userMode);
        recyclerView.setAdapter(recyclerAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new SlideInRightAnimator());
        return rootView;
    }

    void setDBConnection(DBConnection dbConnection, Context context)
    {
        this.dbConnection = dbConnection;
        this.context = context;
    }

    void setUserMode(int userMode)
    {
        this.userMode = userMode;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        recyclerAdapter.cleanup();
    }


}
