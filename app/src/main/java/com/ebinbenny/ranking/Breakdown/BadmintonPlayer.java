package com.ebinbenny.ranking.Breakdown;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.ebinbenny.ranking.LeagueFragment;
import com.ebinbenny.ranking.PlayerActivity;
import com.ebinbenny.ranking.R;
import com.google.firebase.database.IgnoreExtraProperties;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@IgnoreExtraProperties
public class BadmintonPlayer extends Player implements Serializable
{
    public static final String TOTAL_POINTS = "Points";
    public static final String TOTAL_GAMES = "Games";
    public static final String TOTAL_WINS = "Wins";
    public static final String TOTAL_LOSSES = "Losses";
    public static final String TOTAL_SETS = "Sets";
    public static final String TOTAL_SETS_WON = "Sets Won";
    public static final String TOTAL_SETS_LOST = "Sets Lost";
    public static final String TOTAL_WIN_MARGIN = "Win Margin";
    public static final String TOTAL_LOSS_MARGIN = "Loss Margin";

    public static final int NUMBER_OF_GAMES_SAVED = 20;
    public static final int NUMBER_OF_CHARTING_POINTS_SAVED = 500;

    public String playerName, playerKey;
    public int playerRankingPoints, latestRankChange, playerRank;
    public ArrayList<Integer> chartPoints;
    public ArrayList<BadmintonGame> recentGames;
    public ArrayList<Long> chartKeys;
    public Map<String, Integer> statistics;

    public BadmintonPlayer()
    {

    }

    public BadmintonPlayer(String playerName, String playerKey, int playerRankingPoints, ArrayList<Integer> chartPoints, ArrayList<BadmintonGame> recentGames, HashMap<String, Integer> statistics)
    {
        this.playerName = playerName;
        this.playerKey = playerKey;
        this.playerRankingPoints = playerRankingPoints;
        this.chartPoints = chartPoints;
        this.recentGames = recentGames;
        this.statistics = statistics;
        chartKeys = new ArrayList<>();
        chartKeys.add(System.currentTimeMillis());
        playerRank = -1;
    }

    public BadmintonPlayer(String playerName, String playerKey, int playerRankingPoints, ArrayList<Integer> chartPoints, ArrayList<BadmintonGame> recentGames, HashMap<String, Integer> statistics, long time)
    {
        this.playerName = playerName;
        this.playerKey = playerKey;
        this.playerRankingPoints = playerRankingPoints;
        this.chartPoints = chartPoints;
        this.recentGames = recentGames;
        this.statistics = statistics;
        chartKeys = new ArrayList<>();
        chartKeys.add(time);
        playerRank = -1;
    }

    public static float getWinRate(BadmintonPlayer player)
    {
        return (float) player.statistics.get(TOTAL_WINS) / (float) player.statistics.get(TOTAL_GAMES);
    }

    public static int getTotalGames(BadmintonPlayer player)
    {
        return player.statistics.get(TOTAL_GAMES);
    }

    public static int getTotalSets(BadmintonPlayer player)
    {
        return player.statistics.get(TOTAL_SETS);
    }

    public static int getTotalWins(BadmintonPlayer player)
    {
        return player.statistics.get(TOTAL_WINS);
    }

    public static int getTotalLosses(BadmintonPlayer player)
    {
        return player.statistics.get(TOTAL_LOSSES);
    }

    public static int getTotalSetsWon(BadmintonPlayer player)
    {
        return player.statistics.get(TOTAL_SETS_WON);
    }

    public static int getTotalSetsLost(BadmintonPlayer player)
    {
        return player.statistics.get(TOTAL_SETS_LOST);
    }

    public static int getTotalPoints(BadmintonPlayer player)
    {
        return player.statistics.get(TOTAL_POINTS);
    }

    private static void updateSmallGraph(GraphView graph, BadmintonPlayer player, Context context)
    {
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHighlightZeroLines(true);
        graph.getGridLabelRenderer().reloadStyles();

        int size = player.chartPoints.size();
        if (size > 25) size = 25;
        DataPoint[] dataPoints = new DataPoint[size];
        int startPoint = 0;
        if (dataPoints.length > 25) startPoint = dataPoints.length - 25;
        for (int i = 0; i < dataPoints.length; i++)
        {
            dataPoints[i] = new DataPoint(i, player.chartPoints.get(i + startPoint));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        graph.getViewport().setMaxX((double) player.chartPoints.size() - 1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getSeries().clear();
        graph.addSeries(series);
    }

    public static void populateRankHolder(final BadmintonPlayer.RankHolder holder, final BadmintonPlayer player, final Context context)
    {
        holder.playerName.setText(player.playerName);
        holder.playerRanking.setText(String.valueOf(player.playerRankingPoints));
        holder.playerPosition.setText(String.valueOf(player.playerRank));
        TextDrawable textDrawable = TextDrawable.builder().buildRound(player.playerName.substring(0, 1), ColorGenerator.MATERIAL.getColor(player.playerName));
        holder.playerImage.setImageDrawable(textDrawable);
        holder.rankIndicator.setVisibility(View.VISIBLE);
        holder.rankChange.setVisibility(View.VISIBLE);
        if (player.latestRankChange > 0)
        {
            holder.rankIndicator.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_up_white_24dp));
            holder.rankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabFirstPosition)));
            holder.rankChange.setText("+" + String.valueOf(player.latestRankChange));
        } else if (player.latestRankChange < 0)
        {
            holder.rankIndicator.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_down_white_24dp));
            holder.rankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabSecondPosition)));
            holder.rankChange.setText(String.valueOf(player.latestRankChange));
        } else if (player.latestRankChange == 0)
        {
            holder.rankIndicator.setVisibility(View.INVISIBLE);
            holder.rankChange.setVisibility(View.INVISIBLE);
        }

        BadmintonPlayer.updateSmallGraph(holder.smallGraphView, player, context);

        holder.cardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra(LeagueFragment.PLAYER_ITEM, player);
                context.startActivity(intent);
            }
        });

    }

    public static double getWinMargin(BadmintonPlayer player)
    {
        int totalWinMargin = player.statistics.get(TOTAL_WIN_MARGIN);
        int totalWins = player.statistics.get(TOTAL_SETS_WON);
        return (double) totalWinMargin / (double) totalWins;
    }

    public static double getLossMargin(BadmintonPlayer player)
    {
        int totalLossMargin = player.statistics.get(TOTAL_LOSS_MARGIN);
        int totalLosses = player.statistics.get(TOTAL_SETS_LOST);
        return (double) totalLossMargin / (double) totalLosses;
    }

    public void setPlayerRankingPoints(int playerRankingPoints)
    {
        this.playerRankingPoints = playerRankingPoints;
    }

    public void setPlayerRank(int playerRank)
    {
        this.playerRank = playerRank;
    }

    public void addGame(BadmintonGame game)
    {
        if (recentGames == null) recentGames = new ArrayList<>();
        recentGames.add(0, game);
        chartPoints.add(BadmintonGame.getFinishingRank(game, this));
        chartKeys.add(game.time);
        int totalPoints = statistics.get(TOTAL_POINTS);
        totalPoints += BadmintonGame.getPointsScored(game, this);
        statistics.put(TOTAL_POINTS, totalPoints);
        int totalLossMargin = statistics.get(TOTAL_LOSS_MARGIN);
        int totalWinMargin = statistics.get(TOTAL_WIN_MARGIN);
        for (Integer margin : BadmintonGame.getMargins(game, this))
        {
            if (margin < 0)
            {
                totalLossMargin += Math.abs(margin);
            } else if (margin > 0)
            {
                totalWinMargin += Math.abs(margin);
            }
        }
        statistics.put(TOTAL_LOSS_MARGIN, totalLossMargin);
        statistics.put(TOTAL_WIN_MARGIN, totalWinMargin);
        int totalWins = statistics.get(TOTAL_WINS);
        if (BadmintonGame.isWinner(game, this)) totalWins++;
        statistics.put(TOTAL_WINS, totalWins);
        int totalLosses = statistics.get(TOTAL_LOSSES);
        if (BadmintonGame.isLoser(game, this)) totalLosses++;
        statistics.put(TOTAL_LOSSES, totalLosses);
        int totalGames = statistics.get(TOTAL_GAMES);
        totalGames++;
        statistics.put(TOTAL_GAMES, totalGames);
        int totalSets = statistics.get(TOTAL_SETS);
        totalSets += BadmintonGame.getNumberOfSets(game);
        statistics.put(TOTAL_SETS, totalSets);
        int totalSetsWon = statistics.get(TOTAL_SETS_WON);
        totalSetsWon += BadmintonGame.getSetsWon(game, this);
        statistics.put(TOTAL_SETS_WON, totalSetsWon);
        int totalSetsLost = statistics.get(TOTAL_SETS_LOST);
        totalSetsLost += BadmintonGame.getSetsLost(game, this);
        statistics.put(TOTAL_SETS_LOST, totalSetsLost);
        latestRankChange = BadmintonGame.getRankChange(game, this);
        playerRankingPoints = BadmintonGame.getFinishingRank(game, this);
        if (recentGames.size() > NUMBER_OF_GAMES_SAVED) recentGames.remove(recentGames.size() - 1);
    }

    public void deleteGame(BadmintonGame game)
    {
        int recentGamesIndex = recentGames.indexOf(game);
        if (recentGamesIndex != -1)
        {
            recentGames.remove(recentGamesIndex);
        }
        int chartIndex = chartKeys.indexOf(game.time);
        if (chartIndex != -1)
        {
            chartPoints.remove(chartIndex);
            chartKeys.remove(chartIndex);

        }
        int totalPoints = statistics.get(TOTAL_POINTS);
        totalPoints -= BadmintonGame.getPointsScored(game, this);
        statistics.put(TOTAL_POINTS, totalPoints);
        int totalLossMargin = statistics.get(TOTAL_LOSS_MARGIN);
        int totalWinMargin = statistics.get(TOTAL_WIN_MARGIN);
        for (Integer margin : BadmintonGame.getMargins(game, this))
        {
            if (margin < 0)
            {
                totalLossMargin -= Math.abs(margin);
            } else if (margin > 0)
            {
                totalWinMargin -= Math.abs(margin);
            }
        }
        statistics.put(TOTAL_LOSS_MARGIN, totalLossMargin);
        statistics.put(TOTAL_WIN_MARGIN, totalWinMargin);
        int totalWins = statistics.get(TOTAL_WINS);
        if (BadmintonGame.isWinner(game, this)) totalWins--;
        statistics.put(TOTAL_WINS, totalWins);
        int totalLosses = statistics.get(TOTAL_LOSSES);
        if (BadmintonGame.isLoser(game, this)) totalLosses--;
        statistics.put(TOTAL_LOSSES, totalLosses);
        int totalGames = statistics.get(TOTAL_GAMES);
        totalGames--;
        statistics.put(TOTAL_GAMES, totalGames);
        int totalSets = statistics.get(TOTAL_SETS);
        totalSets -= BadmintonGame.getNumberOfSets(game);
        statistics.put(TOTAL_SETS, totalSets);
        int totalSetsWon = statistics.get(TOTAL_SETS_WON);
        totalSetsWon -= BadmintonGame.getSetsWon(game, this);
        statistics.put(TOTAL_SETS_WON, totalSetsWon);
        int totalSetsLost = statistics.get(TOTAL_SETS_LOST);
        totalSetsLost -= BadmintonGame.getSetsLost(game, this);
        statistics.put(TOTAL_SETS_LOST, totalSetsLost);
        if (recentGames.size() > 0) latestRankChange = BadmintonGame.getRankChange(recentGames.get(0), this);
        else latestRankChange = 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(playerKey);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof BadmintonPlayer)
        {
            BadmintonPlayer player = (BadmintonPlayer) object;
            if (playerKey != null && player.playerKey != null)
            {
                return playerKey.equals(player.playerKey);
            } else return false;
        } else if (object instanceof String)
        {
            String key = (String) object;
            if (playerKey != null)
            {
                return playerKey.equals(key);
            } else return false;
        }
        return false;
    }

    public static class BadmintonPlayerRanker implements Comparable<BadmintonPlayerRanker>
    {
        public Long playerRankingPoints;
        public String playerKey;

        public BadmintonPlayerRanker(Long playerRankingPoints, String playerKey)
        {
            this.playerKey = playerKey;
            this.playerRankingPoints = playerRankingPoints;
        }

        @Override
        public int compareTo(BadmintonPlayerRanker badmintonPlayerRanker)
        {
            if (badmintonPlayerRanker != null && playerRankingPoints != null)
                return badmintonPlayerRanker.playerRankingPoints.compareTo(playerRankingPoints);
            else return -1;
        }
    }

    public static class RankHolder extends RecyclerView.ViewHolder
    {

        public TextView playerName, playerRanking, playerPosition, rankChange;
        public GraphView smallGraphView;
        public ImageView playerImage;
        public View rankChangeLayout, rankIndicator;
        public CardView cardButton;

        public RankHolder(View itemView)
        {
            super(itemView);
            playerName = (TextView) itemView.findViewById(R.id.playerName);
            playerRanking = (TextView) itemView.findViewById(R.id.playerRanking);
            playerPosition = (TextView) itemView.findViewById(R.id.playerRank);
            smallGraphView = (GraphView) itemView.findViewById(R.id.smallGraph);
            playerImage = (ImageView) itemView.findViewById(R.id.playerPicture);
            rankChangeLayout = itemView.findViewById(R.id.rankChangeLayout);
            rankIndicator = rankChangeLayout.findViewById(R.id.rankChangeIndicator);
            rankChange = (TextView) rankChangeLayout.findViewById(R.id.rankChange);
            cardButton = (CardView) itemView.findViewById(R.id.playerRankLayout);

        }
    }


}
