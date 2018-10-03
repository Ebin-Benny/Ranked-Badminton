package com.ebinbenny.ranking.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ebinbenny.ranking.Breakdown.BadmintonPlayer;
import com.ebinbenny.ranking.Manager.DBConnection;
import com.ebinbenny.ranking.R;
import com.ebinbenny.ranking.UI.RecyclerViewAdapters.RankingAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class RankingTab extends Fragment
{

    private RankingAdapter recyclerAdapter;

    private DBConnection dbConnection;

    void setDBConnection(DBConnection dbConnection, Context context)
    {
        this.dbConnection = dbConnection;
        recyclerAdapter = new RankingAdapter(BadmintonPlayer.class, R.layout.player_rank, BadmintonPlayer.RankHolder.class, dbConnection.playerRankQuery(), context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View rootView = inflater.inflate(R.layout.rank_tab, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setAdapter(recyclerAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new SlideInUpAnimator());

        //final AddGame addGame = new AddGame(getContext(), getActivity(), rootView, dbConnection);

        return rootView;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        recyclerAdapter.cleanup();
    }
}
