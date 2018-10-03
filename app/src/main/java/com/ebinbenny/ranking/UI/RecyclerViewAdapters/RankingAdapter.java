package com.ebinbenny.ranking.UI.RecyclerViewAdapters;

import android.content.Context;
import com.ebinbenny.ranking.Breakdown.BadmintonPlayer;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

public class RankingAdapter extends FirebaseRecyclerAdapter<BadmintonPlayer, BadmintonPlayer.RankHolder>
{

    private Context context;

    public RankingAdapter(Class<BadmintonPlayer> modelClass, int modelLayout, Class<BadmintonPlayer.RankHolder> viewHolderClass, Query query, Context context)
    {
        super(modelClass, modelLayout, viewHolderClass, query);
        this.context = context;
    }

    @Override
    protected void populateViewHolder(final BadmintonPlayer.RankHolder holder, final BadmintonPlayer player, final int position)
    {
        BadmintonPlayer.populateRankHolder(holder, player, context);
    }

}
