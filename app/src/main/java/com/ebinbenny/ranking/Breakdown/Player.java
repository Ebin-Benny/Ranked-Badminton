package com.ebinbenny.ranking.Breakdown;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

abstract public class Player
{

    public void addGame(Game game)
    {

    }

    public void deleteGame(Game game)
    {

    }

    public static float getWinRate(Player player)
    {
        return 0;
    }

    public static void populateRankHolder(final Player.RankHolder holder, final BadmintonPlayer player, final Context context)
    {

    }

    public void setPlayerRankingPoints()
    {

    }

    public void setPlayerRank(int playerRank)
    {

    }

    public static class RankHolder extends RecyclerView.ViewHolder
    {

        public RankHolder(View itemView)
        {
            super(itemView);
        }
    }


}
