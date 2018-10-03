package com.ebinbenny.ranking.Manager;

import com.ebinbenny.ranking.Breakdown.BadmintonGame;

import java.util.ArrayList;

public class RankCalculator
{

    public static final int BONUS_POINTS_MARGIN = 11;
    public static final double BONUS_POINTS_MULTIPLIER = 1.5;
    public static final int MAXIMUM_RANK_DIFFERENCE = 200;


    public static void rankGame(BadmintonGame game, int playerOneRank, int playerTwoRank)
    {
        game.setPlayerOneStartingRank(playerOneRank);
        game.setPlayerTwoStartingRank(playerTwoRank);
        int pointsDifference = Math.abs(playerOneRank - playerTwoRank);

        if (pointsDifference > MAXIMUM_RANK_DIFFERENCE) pointsDifference = MAXIMUM_RANK_DIFFERENCE;

        double pointsExchanged;

        int highestRankedPlayer = BadmintonGame.DRAW;
        if (playerOneRank > playerTwoRank) highestRankedPlayer = BadmintonGame.PLAYER_ONE;
        else if (playerTwoRank > playerOneRank) highestRankedPlayer = BadmintonGame.PLAYER_TWO;

        if (BadmintonGame.getWinner(game) == highestRankedPlayer)
            pointsExchanged = Math.abs(20 - (double) (pointsDifference / 10));
        else pointsExchanged = Math.abs(-20 - (double) (pointsDifference / 10));

        if (BadmintonGame.getNumberOfSets(game) == 2 && game.getPointsDifference(0) > BONUS_POINTS_MARGIN && game.getPointsDifference(1) > BONUS_POINTS_MARGIN)
            pointsExchanged *= BONUS_POINTS_MULTIPLIER;
        else if (BadmintonGame.getNumberOfSets(game) == 1)
            pointsExchanged /= 2;

        if (BadmintonGame.getWinner(game) == BadmintonGame.PLAYER_ONE)
        {
            playerOneRank += (int) pointsExchanged;
            playerTwoRank -= (int) pointsExchanged;
        } else if (BadmintonGame.getWinner(game) == BadmintonGame.PLAYER_TWO)
        {
            playerOneRank -= (int) pointsExchanged;
            playerTwoRank += (int) pointsExchanged;
        }

        game.setPlayerOneFinishingRank(playerOneRank);
        game.setPlayerTwoFinishingRank(playerTwoRank);

    }
}
