package com.ebinbenny.ranking;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.ebinbenny.ranking.Breakdown.BadmintonGame;
import com.ebinbenny.ranking.Breakdown.BadmintonPlayer;
import com.ebinbenny.ranking.Other.Utility;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;

public class PlayerActivity extends AppCompatActivity
{

    private Drawable arrowUp, arrowDown;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        arrowUp = getDrawable(R.drawable.ic_keyboard_arrow_up_white_24dp);
        arrowDown = getDrawable(R.drawable.ic_keyboard_arrow_down_white_24dp);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Intent intent = getIntent();
        final BadmintonPlayer player = (BadmintonPlayer) intent.getSerializableExtra(LeagueFragment.PLAYER_ITEM);

        ImageView playerImage = (ImageView) findViewById(R.id.player_image);
        TextDrawable textDrawable = TextDrawable.builder().buildRound(player.playerName.substring(0, 1), ColorGenerator.MATERIAL.getColor(player.playerName));
        playerImage.setImageDrawable(textDrawable);
        TextView playerRank = (TextView) findViewById(R.id.player_rank);
        playerRank.setText(String.valueOf(player.playerRank));
        TextView playerName = (TextView) findViewById(R.id.player_name);
        playerName.setText(player.playerName);

        final LinearLayout playerWinLayout = (LinearLayout) findViewById(R.id.player_win_indicator);

        final CardView nameHolder = (CardView) findViewById(R.id.name_holder);

        nameHolder.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
        {
            @Override
            public void onLayoutChange(View view, int left, int i1, int i2, int i3, int i4, int i5, int i6, int i7)
            {

                nameHolder.removeOnLayoutChangeListener(this);
                for (int i = 0; i < 8 && i < (Utility.pxToDp(playerWinLayout.getMeasuredWidth(), getApplicationContext()) / 40) - 1 && i < player.recentGames.size(); i++)
                {
                    final View winIndicator = getLayoutInflater().inflate(R.layout.win_indicator, null);
                    final ImageView winImage = (ImageView) winIndicator.findViewById(R.id.indicator_image);
                    int color;
                    String text;


                    if (BadmintonGame.getPlayer(player.recentGames.get(i), player) == BadmintonGame.getWinner(player.recentGames.get(i)))
                    {
                        color = ContextCompat.getColor(getApplicationContext(), R.color.fabFirstPosition);
                        text = "W";
                    } else if (BadmintonGame.getPlayer(player.recentGames.get(i), player) == BadmintonGame.DRAW)
                    {
                        color = ContextCompat.getColor(getApplicationContext(), R.color.gray);
                        text = "D";
                    } else
                    {
                        color = ContextCompat.getColor(getApplicationContext(), R.color.fabSecondPosition);
                        text = "L";
                    }
                    TextDrawable textDrawableWin = TextDrawable.builder().buildRound(text, color);
                    winImage.setImageDrawable(textDrawableWin);
                    playerWinLayout.addView(winIndicator);
                }
            }
        });

        TextView playerWinRate = (TextView) findViewById(R.id.player_winrate);
        NumberFormat format = new DecimalFormat("##.#%");
        playerWinRate.setText(format.format(BadmintonPlayer.getWinRate(player)));
        TextView playerPoints = (TextView) findViewById(R.id.player_points);
        playerPoints.setText(String.valueOf(player.playerRankingPoints));
        View indicatorView = findViewById(R.id.player_rank_indicator);
        View playerRankChangeIndicator = indicatorView.findViewById(R.id.rankChangeIndicator);
        TextView playerRankChange = (TextView) indicatorView.findViewById(R.id.rankChange);

        if (player.latestRankChange > 0)
        {
            playerRankChangeIndicator.setBackground(arrowUp);
            playerRankChangeIndicator.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fabFirstPosition)));
            playerRankChange.setText("+" + String.valueOf(player.latestRankChange));
        } else if (player.latestRankChange < 0)
        {
            playerRankChangeIndicator.setBackground(arrowDown);
            playerRankChangeIndicator.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fabSecondPosition)));
            playerRankChange.setText(String.valueOf(player.latestRankChange));
        } else if (player.latestRankChange == 0)
        {
            playerRankChangeIndicator.setVisibility(View.INVISIBLE);
            playerRankChange.setVisibility(View.INVISIBLE);
        }

        TextView totalGames = (TextView) findViewById(R.id.total_games);
        totalGames.setText(String.valueOf(BadmintonPlayer.getTotalGames(player)));
        TextView totalSets = (TextView) findViewById(R.id.total_sets);
        totalSets.setText(String.valueOf(BadmintonPlayer.getTotalSets(player)));

        TextView totalGamesWon = (TextView) findViewById(R.id.games_won);
        totalGamesWon.setText(String.valueOf(BadmintonPlayer.getTotalWins(player)));
        TextView totalGamesLost = (TextView) findViewById(R.id.games_lost);
        totalGamesLost.setText(String.valueOf(BadmintonPlayer.getTotalLosses(player)));

        TextView totalSetsWon = (TextView) findViewById(R.id.sets_won);
        totalSetsWon.setText(String.valueOf(BadmintonPlayer.getTotalSetsWon(player)));
        TextView totalSetsLost = (TextView) findViewById(R.id.sets_lost);
        totalSetsLost.setText(String.valueOf(BadmintonPlayer.getTotalSetsLost(player)));

        TextView totalPoints = (TextView) findViewById(R.id.total_points);
        totalPoints.setText(String.valueOf(BadmintonPlayer.getTotalPoints(player)));

        DecimalFormat marginFormat = new DecimalFormat("#.##");
        TextView winMargin = (TextView) findViewById(R.id.win_margin);
        winMargin.setText(marginFormat.format(BadmintonPlayer.getWinMargin(player)));

        TextView lossMargin = (TextView) findViewById(R.id.loss_margin);
        lossMargin.setText(marginFormat.format(BadmintonPlayer.getLossMargin(player)));

        TextView maxRank = (TextView) findViewById(R.id.max_rank);
        maxRank.setText(String.valueOf(Collections.max(player.chartPoints)));

        TextView minRank = (TextView) findViewById(R.id.min_rank);
        minRank.setText(String.valueOf(Collections.min(player.chartPoints)));

        GraphView graphView = (GraphView) findViewById(R.id.graph);
        updateGraph(graphView, player);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout recentGames = (LinearLayout) findViewById(R.id.recent_games_list);
        for (BadmintonGame game : player.recentGames)
            recentGames.addView(BadmintonGame.inflateGameView(inflater, game, arrowUp, arrowDown, this));

    }


    public void updateGraph(GraphView graph, BadmintonPlayer player)
    {
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setHighlightZeroLines(false);
        graph.getGridLabelRenderer().setTextSize(20);
        graph.getGridLabelRenderer().setLabelsSpace(30);

        DataPoint[] dataPoints = new DataPoint[player.chartPoints.size()];

        for (int i = 0; i < dataPoints.length; i++)
        {
            dataPoints[i] = new DataPoint(i, player.chartPoints.get(i));
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        graph.getSeries().clear();
        series.setDrawDataPoints(false);
        graph.addSeries(series);
        series.setColor(ContextCompat.getColor(this, R.color.colorAccent));
        graph.getViewport().setMaxX((double) player.chartPoints.size() - 1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().reloadStyles();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                super.onBackPressed();
            default:
                break;
        }
        return true;
    }


}
