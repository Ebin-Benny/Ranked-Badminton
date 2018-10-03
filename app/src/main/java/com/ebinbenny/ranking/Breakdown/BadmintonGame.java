package com.ebinbenny.ranking.Breakdown;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.transition.Fade;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.ebinbenny.ranking.LeagueFragment;
import com.ebinbenny.ranking.Manager.DBConnection;
import com.ebinbenny.ranking.R;
import com.ebinbenny.ranking.UI.RecyclerViewAdapters.MatchLogAdapter;

import java.io.Serializable;
import java.util.ArrayList;


public class BadmintonGame implements Serializable
{
    public static int PLAYER_ONE = 0;
    public static int PLAYER_TWO = 1;
    public static int DRAW = 2;

    public String playerOneName, playerTwoName, playerOneKey, playerTwoKey;
    public ArrayList<Integer> playerOneScores, playerTwoScores;
    public int playerOneStartingRank, playerTwoStartingRank, playerOneFinishingRank, playerTwoFinishingRank;
    public String gameKey;
    public long time;

    public BadmintonGame()
    {

    }

    public BadmintonGame(String playerOneName, String playerTwoName, ArrayList<Integer> playerOneScores, ArrayList<Integer> playerTwoScores)
    {
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.playerOneScores = playerOneScores;
        this.playerTwoScores = playerTwoScores;
        this.time = System.currentTimeMillis();
    }

    public BadmintonGame(String playerOneName, String playerTwoName, ArrayList<Integer> playerOneScores, ArrayList<Integer> playerTwoScores, long time)
    {
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.playerOneScores = playerOneScores;
        this.playerTwoScores = playerTwoScores;
        this.time = time;
    }

    public static int getFinishingRank(BadmintonGame game, BadmintonPlayer player)
    {
        if (player.playerName.equals(game.playerOneName)) return game.playerOneFinishingRank;
        else if (player.playerName.equals(game.playerTwoName)) return game.playerTwoFinishingRank;
        else return -1;
    }

    public static int getStartingRank(BadmintonGame game, BadmintonPlayer player)
    {
        if (player.playerName.equals(game.playerOneName)) return game.playerOneStartingRank;
        else if (player.playerName.equals(game.playerTwoName)) return game.playerTwoStartingRank;
        else return -1;
    }

    public static int getPointsScored(BadmintonGame game, BadmintonPlayer player)
    {
        if (player.playerName.equals(game.playerOneName))
        {
            int totalPoints = 0;
            for (Integer i : game.playerOneScores)
                totalPoints += i;
            return totalPoints;
        } else if (player.playerName.equals(game.playerTwoName))
        {
            int totalPoints = 0;
            for (Integer i : game.playerTwoScores)
                totalPoints += i;
            return totalPoints;
        } else return 0;
    }

    public static int getRankChange(BadmintonGame game, BadmintonPlayer player)
    {
        return getRankChange(game, player.playerName);
    }

    public static int getRankChange(BadmintonGame game, String playerName)
    {
        if (playerName.equals(game.playerOneName))
            return game.playerOneFinishingRank - game.playerOneStartingRank;
        else if (playerName.equals(game.playerTwoName))
            return game.playerTwoFinishingRank - game.playerTwoStartingRank;
        else return 0;

    }

    public static boolean isWinner(BadmintonGame game, BadmintonPlayer player)
    {
        int winner = getWinner(game);
        if (player.playerName.equals(game.playerOneName)) return winner == PLAYER_ONE;
        else if (player.playerName.equals(game.playerTwoName)) return winner == PLAYER_TWO;
        else return false;
    }

    public static boolean isLoser(BadmintonGame game, BadmintonPlayer player)
    {
        int winner = getWinner(game);
        if (player.playerName.equals(game.playerOneName)) return winner == PLAYER_TWO;
        else if (player.playerName.equals(game.playerTwoName)) return winner == PLAYER_ONE;
        else return false;
    }

    public static ArrayList<Integer> getMargins(BadmintonGame game, BadmintonPlayer player)
    {
        ArrayList<Integer> margins = new ArrayList<>();
        for (int i = 0; i < game.playerOneScores.size(); i++)
        {
            if (player.playerName.equals(game.playerOneName))
            {
                margins.add(game.playerOneScores.get(i) - game.playerTwoScores.get(i));
            } else if (player.playerName.equals(game.playerTwoName))
            {
                margins.add(game.playerTwoScores.get(i) - game.playerOneScores.get(i));
            }
        }
        return margins;
    }

    public static int getSetsWon(BadmintonGame game, BadmintonPlayer player)
    {
        if (player.playerName.equals(game.playerOneName))
        {
            int setsWon = 0;
            if (game.playerOneScores != null)
            {
                for (int i = 0; i < game.playerOneScores.size(); i++)
                {
                    if (game.playerOneScores.get(i) > game.playerTwoScores.get(i))
                        setsWon++;
                }
            }
            return setsWon;
        } else if (player.playerName.equals(game.playerTwoName))
        {
            int setsWon = 0;
            if (game.playerTwoScores != null)
            {
                for (int i = 0; i < game.playerTwoScores.size(); i++)
                {
                    if (game.playerTwoScores.get(i) > game.playerOneScores.get(i))
                        setsWon++;
                }
            }
            return setsWon;
        } else return 0;
    }

    public static int getSetsLost(BadmintonGame game, BadmintonPlayer player)
    {
        if (player.playerName.equals(game.playerOneName))
        {
            int setsLost = 0;
            if (game.playerOneScores != null)
            {
                for (int i = 0; i < game.playerOneScores.size(); i++)
                {
                    if (game.playerOneScores.get(i) < game.playerTwoScores.get(i))
                        setsLost++;
                }
            }
            return setsLost;
        } else if (player.playerName.equals(game.playerTwoName))
        {
            int setsLost = 0;
            if (game.playerTwoScores != null)
            {
                for (int i = 0; i < game.playerTwoScores.size(); i++)
                {
                    if (game.playerTwoScores.get(i) < game.playerOneScores.get(i))
                        setsLost++;
                }
            }
            return setsLost;
        } else return 0;
    }

    public static int getNumberOfSets(BadmintonGame game)
    {
        return game.playerOneScores.size();
    }

    public static int getWinner(BadmintonGame game)
    {
        if (game.playerOneScores != null && game.playerTwoScores != null)
        {
            int playerOneSets = 0;
            int playerTwoSets = 0;

            for (int i = 0; i < game.playerOneScores.size(); i++)
            {
                if (game.playerOneScores.get(i) > game.playerTwoScores.get(i)) playerOneSets++;
                else if (game.playerOneScores.get(i) < game.playerTwoScores.get(i)) playerTwoSets++;
            }

            if (playerOneSets > playerTwoSets) return PLAYER_ONE;
            else if (playerOneSets < playerTwoSets) return PLAYER_TWO;
            else return DRAW;
        }
        return -1;
    }

    public static View inflateGameView(LayoutInflater inflater, BadmintonGame game, Drawable arrowUp, Drawable arrowDown, Context context)
    {
        View player1Board, player2Board, player1ScoreDisplay, player2ScoreDisplay, player1WinIndicator, player2WinIndicator,
                player1RankLayout, player1RankIndicator, player2RankLayout, player2RankIndicator;
        ImageView player1Image, player2Image;
        TextView player1Name, player2Name, player1RankChange, player2RankChange;
        LinearLayout player1Sets, player2Sets;
        RelativeLayout buttonHolder;
        ArrayList<View> player1SetBoards, player2SetBoards;
        ColorStateList defaultTextColour;

        View itemView = inflater.inflate(R.layout.full_score_board, null);
        player1Board = itemView.findViewById(R.id.player1Board);
        player2Board = itemView.findViewById(R.id.player2Board);
        player1ScoreDisplay = player1Board.findViewById(R.id.playerScoreDisplay);
        player2ScoreDisplay = player2Board.findViewById(R.id.playerScoreDisplay);
        player1Image = (ImageView) player1ScoreDisplay.findViewById(R.id.scorePlayerImage);
        player2Image = (ImageView) player2ScoreDisplay.findViewById(R.id.scorePlayerImage);
        player1Name = (TextView) player1ScoreDisplay.findViewById(R.id.scorePlayerName);
        player2Name = (TextView) player2ScoreDisplay.findViewById(R.id.scorePlayerName);
        player1RankLayout = player1Board.findViewById(R.id.rankChangeLayout);
        player2RankLayout = player2Board.findViewById(R.id.rankChangeLayout);
        player1RankIndicator = player1RankLayout.findViewById(R.id.rankChangeIndicator);
        player2RankIndicator = player2RankLayout.findViewById(R.id.rankChangeIndicator);
        player1RankChange = (TextView) player1RankLayout.findViewById(R.id.rankChange);
        player2RankChange = (TextView) player2RankLayout.findViewById(R.id.rankChange);
        player1WinIndicator = player1Board.findViewById(R.id.winIndicator);
        player2WinIndicator = player2Board.findViewById(R.id.winIndicator);
        player1Sets = (LinearLayout) player1Board.findViewById(R.id.scoreHolder);
        player1SetBoards = new ArrayList<>();
        player1SetBoards.add(player1Sets.findViewById(R.id.board0));
        player1SetBoards.add(player1Sets.findViewById(R.id.board1));
        player1SetBoards.add(player1Sets.findViewById(R.id.board2));
        player2Sets = (LinearLayout) player2Board.findViewById(R.id.scoreHolder);
        player2SetBoards = new ArrayList<>();
        player2SetBoards.add(player2Sets.findViewById(R.id.board0));
        player2SetBoards.add(player2Sets.findViewById(R.id.board1));
        player2SetBoards.add(player2Sets.findViewById(R.id.board2));
        buttonHolder = (RelativeLayout) itemView.findViewById(R.id.deleteButtonHolder);
        defaultTextColour = player1Name.getTextColors();

        buttonHolder.setVisibility(View.GONE);
        player1Image.setImageDrawable(TextDrawable.builder().buildRound(game.playerOneName.substring(0, 1), ColorGenerator.MATERIAL.getColor(game.playerOneName)));
        player2Image.setImageDrawable(TextDrawable.builder().buildRound(game.playerTwoName.substring(0, 1), ColorGenerator.MATERIAL.getColor(game.playerTwoName)));
        player1Name.setText(game.playerOneName);
        player2Name.setText(game.playerTwoName);

        if (BadmintonGame.getWinner(game) == PLAYER_ONE)
        {
            player1WinIndicator.setVisibility(View.VISIBLE);
            player2WinIndicator.setVisibility(View.INVISIBLE);
            player1Name.setTextColor(Color.BLACK);
            player2Name.setTextColor(Color.GRAY);
            player1Name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            player2Name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            player1RankIndicator.setBackground(arrowUp);
            player1RankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabFirstPosition)));
            player2RankIndicator.setBackground(arrowDown);
            player2RankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabSecondPosition)));

        } else if (BadmintonGame.getWinner(game) == PLAYER_TWO)
        {
            player1WinIndicator.setVisibility(View.INVISIBLE);
            player2WinIndicator.setVisibility(View.VISIBLE);
            player1Name.setTextColor(Color.GRAY);
            player2Name.setTextColor(Color.BLACK);
            player1Name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            player2Name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            player2RankIndicator.setBackground(arrowUp);
            player2RankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabFirstPosition)));
            player1RankIndicator.setBackground(arrowDown);
            player1RankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabSecondPosition)));
        }

        String player1RankChangeText = (BadmintonGame.getRankChange(game, game.playerOneName) > 0 ? "+" : "") + String.valueOf(BadmintonGame.getRankChange(game, game.playerOneName));
        String player2RankChangeText = (BadmintonGame.getRankChange(game, game.playerTwoName) > 0 ? "+" : "") + String.valueOf(BadmintonGame.getRankChange(game, game.playerTwoName));
        player1RankChange.setText(player1RankChangeText);
        player2RankChange.setText(player2RankChangeText);

        for (int i = 0; i < player1SetBoards.size(); i++)
        {
            player1SetBoards.get(i).setVisibility(View.INVISIBLE);
            player2SetBoards.get(i).setVisibility(View.INVISIBLE);
        }

        int setCount = 0;
        for (int i = player1SetBoards.size() - BadmintonGame.getNumberOfSets(game); i < player1SetBoards.size(); i++)
        {
            int player1Score = game.playerOneScores.get(setCount);
            int player2Score = game.playerTwoScores.get(setCount);
            setCount++;
            View player1Set = player1SetBoards.get(i);
            View player2Set = player2SetBoards.get(i);
            player1Set.setVisibility(View.VISIBLE);
            player2Set.setVisibility(View.VISIBLE);
            TextView player1Display = (TextView) player1Set.findViewById(R.id.playerSetScore);
            TextView player2Display = (TextView) player2Set.findViewById(R.id.playerSetScore);
            player1Display.setText(String.valueOf(player1Score));
            player2Display.setText(String.valueOf(player2Score));

            if (player1Score > player2Score)
            {
                player1Display.setTextColor(Color.BLACK);
                player1Display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                player2Display.setTextColor(defaultTextColour);
                player2Display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            } else if (player2Score > player1Score)
            {
                player2Display.setTextColor(Color.BLACK);
                player2Display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                player1Display.setTextColor(defaultTextColour);
                player1Display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            }
        }
        return itemView;
    }

    public static int getPlayer(BadmintonGame game, BadmintonPlayer player)
    {
        if (player.playerName.equals(game.playerOneName)) return PLAYER_ONE;
        else if (player.playerName.equals(game.playerTwoName)) return PLAYER_TWO;
        else return -1;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof BadmintonGame)
        {
            BadmintonGame game = (BadmintonGame) object;
            if (gameKey != null && game.gameKey != null)
            {
                return gameKey.equals(game.gameKey);
            } else return false;
        }
        return false;
    }

    public void setGameKey(String gameKey)
    {
        this.gameKey = gameKey;
    }

    public void setPlayerOneKey(String playerOneKey)
    {
        this.playerOneKey = playerOneKey;
    }

    public void setPlayerTwoKey(String playerTwoKey)
    {
        this.playerTwoKey = playerTwoKey;
    }

    public void setPlayerOneStartingRank(int playerOneStartingRank)
    {
        this.playerOneStartingRank = playerOneStartingRank;
    }

    public void setPlayerTwoStartingRank(int playerTwoStartingRank)
    {
        this.playerTwoStartingRank = playerTwoStartingRank;
    }

    public void setPlayerOneFinishingRank(int playerOneFinishingRank)
    {
        this.playerOneFinishingRank = playerOneFinishingRank;
    }

    public void setPlayerTwoFinishingRank(int playerTwoFinishingRank)
    {
        this.playerTwoFinishingRank = playerTwoFinishingRank;
    }

    public int getPointsDifference(int setIndex)
    {
        if (setIndex < playerOneScores.size())
        {
            return Math.abs(playerOneScores.get(setIndex) - playerTwoScores.get(setIndex));
        }
        return -1;
    }


}
