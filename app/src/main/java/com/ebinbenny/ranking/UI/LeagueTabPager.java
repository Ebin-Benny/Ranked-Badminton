package com.ebinbenny.ranking.UI;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.ebinbenny.ranking.Manager.DBConnection;


public class LeagueTabPager extends FragmentStatePagerAdapter
{

    private int tabCount;
    private RankingTab rankingTab;
    private MatchLogTab matchLogTab;

    public LeagueTabPager(Context context, FragmentManager fragmentManager, int tabCount, DBConnection dbConnection, int userMode)
    {
        super(fragmentManager);

        rankingTab = new RankingTab();
        matchLogTab = new MatchLogTab();
        rankingTab.setDBConnection(dbConnection, context);
        matchLogTab.setDBConnection(dbConnection, context);
        matchLogTab.setUserMode(userMode);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return rankingTab;
            case 1:
                return matchLogTab;
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return tabCount;
    }
}