package com.ebinbenny.ranking.UI.RecyclerViewAdapters;

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
import android.view.View;
import android.widget.*;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.ebinbenny.ranking.Breakdown.BadmintonGame;
import com.ebinbenny.ranking.LeagueFragment;
import com.ebinbenny.ranking.Manager.DBConnection;
import com.ebinbenny.ranking.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import static com.ebinbenny.ranking.Breakdown.BadmintonGame.PLAYER_TWO;

public class MatchLogAdapter extends FirebaseRecyclerAdapter<BadmintonGame, MatchLogAdapter.GameHolder>
{

    private Context context;
    private Drawable arrowUp, arrowDown;
    private DBConnection dbConnection;
    private int userMode;
    private int lastPosition = -1;

    public MatchLogAdapter(Class<BadmintonGame> modelClass, int modelLayout, Class<MatchLogAdapter.GameHolder> viewHolderClass, Query query, Context context, DBConnection dbConnection, int userMode)
    {
        super(modelClass, modelLayout, viewHolderClass, query);
        this.context = context;
        arrowUp = context.getDrawable(R.drawable.ic_keyboard_arrow_up_white_24dp);
        arrowDown = context.getDrawable(R.drawable.ic_keyboard_arrow_down_white_24dp);
        this.dbConnection = dbConnection;
        this.userMode = userMode;
    }

    @Override
    protected void populateViewHolder(final MatchLogAdapter.GameHolder holder, final BadmintonGame game, final int position)
    {
        holder.buttonHolder.setVisibility(View.GONE);
        holder.player1Image.setImageDrawable(TextDrawable.builder().buildRound(game.playerOneName.substring(0, 1), ColorGenerator.MATERIAL.getColor(game.playerOneName)));
        holder.player2Image.setImageDrawable(TextDrawable.builder().buildRound(game.playerTwoName.substring(0, 1), ColorGenerator.MATERIAL.getColor(game.playerTwoName)));
        holder.player1Name.setText(game.playerOneName);
        holder.player2Name.setText(game.playerTwoName);

        if (BadmintonGame.getWinner(game) == BadmintonGame.PLAYER_ONE)
        {
            holder.player1WinIndicator.setVisibility(View.VISIBLE);
            holder.player2WinIndicator.setVisibility(View.INVISIBLE);
            holder.player1Name.setTextColor(Color.BLACK);
            holder.player2Name.setTextColor(Color.GRAY);
            holder.player1Name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            holder.player2Name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            holder.player1RankIndicator.setBackground(arrowUp);
            holder.player1RankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabFirstPosition)));
            holder.player2RankIndicator.setBackground(arrowDown);
            holder.player2RankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabSecondPosition)));

        } else if (BadmintonGame.getWinner(game) == PLAYER_TWO)
        {
            holder.player1WinIndicator.setVisibility(View.INVISIBLE);
            holder.player2WinIndicator.setVisibility(View.VISIBLE);
            holder.player1Name.setTextColor(Color.GRAY);
            holder.player2Name.setTextColor(Color.BLACK);
            holder.player1Name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            holder.player2Name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            holder.player2RankIndicator.setBackground(arrowUp);
            holder.player2RankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabFirstPosition)));
            holder.player1RankIndicator.setBackground(arrowDown);
            holder.player1RankIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabSecondPosition)));
        }

        String player1RankChangeText = (BadmintonGame.getRankChange(game, game.playerOneName) > 0 ? "+" : "") + String.valueOf(BadmintonGame.getRankChange(game, game.playerOneName));
        String player2RankChangeText = (BadmintonGame.getRankChange(game, game.playerTwoName) > 0 ? "+" : "") + String.valueOf(BadmintonGame.getRankChange(game, game.playerTwoName));
        holder.player1RankChange.setText(player1RankChangeText);
        holder.player2RankChange.setText(player2RankChangeText);

        for (int i = 0; i < holder.player1SetBoards.size(); i++)
        {
            holder.player1SetBoards.get(i).setVisibility(View.INVISIBLE);
            holder.player2SetBoards.get(i).setVisibility(View.INVISIBLE);
        }

        int setCount = 0;
        for (int i = holder.player1SetBoards.size() - BadmintonGame.getNumberOfSets(game); i < holder.player1SetBoards.size(); i++)
        {
            int player1Score = game.playerOneScores.get(setCount);
            int player2Score = game.playerTwoScores.get(setCount);
            setCount++;
            View player1Set = holder.player1SetBoards.get(i);
            View player2Set = holder.player2SetBoards.get(i);
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
                player2Display.setTextColor(holder.defaultTextColour);
                player2Display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            } else if (player2Score > player1Score)
            {
                player2Display.setTextColor(Color.BLACK);
                player2Display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                player1Display.setTextColor(holder.defaultTextColour);
                player1Display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            }
        }

        if (userMode == LeagueFragment.EDIT_MODE)
        {
            holder.fullScoreBoard.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (lastPosition >= 0 && lastPosition != position)
                        notifyItemChanged(lastPosition);
                    lastPosition = position;
                    if (holder.buttonHolder.getVisibility() == View.GONE)
                    {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        holder.buttonHolder.setVisibility(View.VISIBLE);
                    } else if (holder.buttonHolder.getVisibility() == View.VISIBLE)
                    {
                        Fade fade = new Fade();
                        fade.setDuration(100);
                        TransitionManager.beginDelayedTransition(holder.buttonHolder, fade);
                        holder.buttonHolder.setVisibility(View.GONE);
                    }
                }
            });


            holder.deleteGameButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm Delete?")
                            .setMessage("Are you sure you want to delete the game?")
                            .setPositiveButton("Delete Game", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    dbConnection.deleteGame(game);
                                }
                            })
                            .setNegativeButton("Cancel", null).show();
                }
            });

            holder.editGameButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    dbConnection.getInputInterface().editGame(game);
                }
            });
        }
    }

    public static class GameHolder extends RecyclerView.ViewHolder
    {
        View player1Board, player2Board, player1ScoreDisplay, player2ScoreDisplay, player1WinIndicator, player2WinIndicator,
                player1RankLayout, player1RankIndicator, player2RankLayout, player2RankIndicator;
        ImageView player1Image, player2Image;
        TextView player1Name, player2Name, player1RankChange, player2RankChange;
        LinearLayout player1Sets, player2Sets;
        CardView fullScoreBoard;
        RelativeLayout buttonHolder;
        Button deleteGameButton, editGameButton;
        ArrayList<View> player1SetBoards, player2SetBoards;
        ColorStateList defaultTextColour;

        public GameHolder(View itemView)
        {
            super(itemView);
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
            fullScoreBoard = (CardView) itemView.findViewById(R.id.fullScoreLayout);
            buttonHolder = (RelativeLayout) itemView.findViewById(R.id.deleteButtonHolder);
            deleteGameButton = (Button) itemView.findViewById(R.id.deleteGameButton);
            editGameButton = (Button) itemView.findViewById(R.id.editGameButton);
            defaultTextColour = player1Name.getTextColors();
        }

    }

}
