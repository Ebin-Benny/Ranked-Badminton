package com.ebinbenny.ranking.Manager;

import com.ebinbenny.ranking.Breakdown.BadmintonGame;
import com.ebinbenny.ranking.Breakdown.BadmintonPlayer;
import com.ebinbenny.ranking.UI.InputInterfaces.Input;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DBConnection
{

    private static final int STARTING_RANK = 1000;

    private DatabaseReference playerNameReference, playerReference, gameReference;
    private HashMap<String, Integer> emptyStatistics;
    private Input input;

    public DBConnection(FirebaseDatabase database, String userID, String leagueID)
    {

        emptyStatistics = new HashMap<>();
        emptyStatistics.put(BadmintonPlayer.TOTAL_GAMES, 0);
        emptyStatistics.put(BadmintonPlayer.TOTAL_WINS, 0);
        emptyStatistics.put(BadmintonPlayer.TOTAL_LOSSES, 0);
        emptyStatistics.put(BadmintonPlayer.TOTAL_SETS, 0);
        emptyStatistics.put(BadmintonPlayer.TOTAL_SETS_WON, 0);
        emptyStatistics.put(BadmintonPlayer.TOTAL_SETS_LOST, 0);
        emptyStatistics.put(BadmintonPlayer.TOTAL_POINTS, 0);
        emptyStatistics.put(BadmintonPlayer.TOTAL_WIN_MARGIN, 0);
        emptyStatistics.put(BadmintonPlayer.TOTAL_LOSS_MARGIN, 0);

        playerNameReference = database.getReference().child("Users").child(userID).child("Leagues").child(leagueID).child("Player Names");
        playerReference = database.getReference().child("Users").child(userID).child("Leagues").child(leagueID).child("Player Data");
        gameReference = database.getReference().child("Users").child(userID).child("Leagues").child(leagueID).child("Games");

        playerNameReference.keepSynced(true);
        playerReference.keepSynced(true);
        gameReference.keepSynced(true);

    }

    public Input getInputInterface()
    {
        return input;
    }

    public void addInputInterface(final Input input)
    {
        this.input = input;
        playerNameReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<String> playerNames = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    playerNames.add((String) data.getValue());
                }
                input.setPlayerTokens(playerNames.toArray(new String[0]));
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public Query playerRankQuery()
    {
        return playerReference.orderByChild("playerRank").limitToFirst(100);
    }

    private void addPointsChangeListener()
    {
        playerRankingPointsQuery().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<BadmintonPlayer.BadmintonPlayerRanker> playerRankers = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    playerRankers.add(new BadmintonPlayer.BadmintonPlayerRanker((Long) data.child("playerRankingPoints").getValue(), data.getKey()));
                }

                if (playerRankers.size() > 1)
                {
                    Collections.sort(playerRankers);

                    for (int i = 0; i < playerRankers.size(); i++)
                    {
                        playerReference.child(playerRankers.get(i).playerKey).child("playerRank").setValue(i + 1);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void editGame(final BadmintonGame oldGame, final BadmintonGame newGame)
    {
        playerNameReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                boolean newPlayerOneExists = false, newPlayerTwoExists = false;
                String newPlayerOneKey = null, newPlayerTwoKey = null;

                newGame.setGameKey(oldGame.gameKey);
                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    if (data.getValue().equals(newGame.playerOneName))
                    {
                        newPlayerOneExists = true;
                        newPlayerOneKey = data.getKey();
                    }

                    if (data.getValue().equals(newGame.playerTwoName))
                    {
                        newPlayerTwoExists = true;
                        newPlayerTwoKey = data.getKey();
                    }

                    if (newPlayerOneExists && newPlayerTwoExists) break;
                }

                if (!newPlayerOneExists)
                {
                    newPlayerOneKey = playerNameReference.push().getKey();
                    playerNameReference.child(newPlayerOneKey).setValue(newGame.playerOneName);
                    ArrayList<Integer> chartingData = new ArrayList<>();
                    chartingData.add(STARTING_RANK);
                    playerReference.child(newPlayerOneKey).setValue(new BadmintonPlayer(newGame.playerOneName, newPlayerOneKey,
                            STARTING_RANK, chartingData, new ArrayList<BadmintonGame>(), (HashMap<String, Integer>) emptyStatistics.clone(), oldGame.time - 1));
                }

                if (!newPlayerTwoExists)
                {
                    newPlayerTwoKey = playerNameReference.push().getKey();
                    playerNameReference.child(newPlayerTwoKey).setValue(newGame.playerTwoName);
                    ArrayList<Integer> chartingData = new ArrayList<>();
                    chartingData.add(STARTING_RANK);
                    playerReference.child(newPlayerTwoKey).setValue(new BadmintonPlayer(newGame.playerTwoName, newPlayerTwoKey,
                            STARTING_RANK, chartingData, new ArrayList<BadmintonGame>(), (HashMap<String, Integer>) emptyStatistics.clone(), oldGame.time - 1));
                }

                newGame.setPlayerOneKey(newPlayerOneKey);
                newGame.setPlayerTwoKey(newPlayerTwoKey);

                newGame.setGameKey(oldGame.gameKey);

                final ArrayList<String> affectedPlayerKeys = new ArrayList<>();
                final ArrayList<BadmintonGame> affectedGames = new ArrayList<>();
                final ArrayList<String> changedPlayers = new ArrayList<>();

                affectedPlayerKeys.add(oldGame.playerOneKey);
                affectedPlayerKeys.add(oldGame.playerTwoKey);
                affectedPlayerKeys.add(newPlayerOneKey);
                affectedPlayerKeys.add(newPlayerTwoKey);

                final String finalNewPlayerOneKey = newPlayerOneKey;
                final String finalNewPlayerTwoKey = newPlayerTwoKey;
                queryGamesAfter(oldGame.gameKey).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            BadmintonGame game = snapshot.getValue(BadmintonGame.class);

                            if (game != null && !game.gameKey.equals(oldGame.gameKey))
                            {
                                if (affectedPlayerKeys.contains(game.playerOneKey) && affectedPlayerKeys.contains(game.playerTwoKey))
                                {
                                    affectedGames.add(game);
                                } else if (affectedPlayerKeys.contains(game.playerOneKey))
                                {
                                    affectedPlayerKeys.add(game.playerTwoKey);
                                    affectedGames.add(game);
                                } else if (affectedPlayerKeys.contains(game.playerTwoKey))
                                {
                                    affectedPlayerKeys.add(game.playerOneKey);
                                    affectedGames.add(game);
                                }
                            }
                        }

                        playerReference.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                ArrayList<BadmintonPlayer> players = new ArrayList<>();

                                for (String key : affectedPlayerKeys)
                                {
                                    players.add(dataSnapshot.child(key).getValue(BadmintonPlayer.class));
                                }

                                for (int i = 0; i < 2; i++)
                                {
                                    BadmintonPlayer player = players.get(i);
                                    player.setPlayerRankingPoints(BadmintonGame.getStartingRank(oldGame, player));
                                    player.deleteGame(oldGame);
                                    changedPlayers.add(players.get(i).playerKey);
                                    players.set(i, player);
                                }

                                for (int i = 2; i < 4; i++)
                                {
                                    int playerRankingPoints = 1000;

                                    for (int j = players.get(i).chartKeys.size() - 1; j >= 0; j--)
                                    {
                                        if (players.get(i).chartKeys.get(j) < oldGame.time)
                                        {
                                            playerRankingPoints = players.get(i).chartPoints.get(j);
                                            break;
                                        }
                                    }
                                    players.get(i).setPlayerRankingPoints(playerRankingPoints);
                                }

                                RankCalculator.rankGame(newGame, players.get(2).playerRankingPoints, players.get(3).playerRankingPoints);

                                if (players.get(2).playerKey.equals(players.get(0).playerKey))
                                    players.set(2, players.get(0));
                                else if (players.get(2).playerKey.equals(players.get(1).playerKey))
                                    players.set(2, players.get(1));


                                if (players.get(3).playerKey.equals(players.get(0).playerKey))
                                    players.set(3, players.get(0));
                                else if (players.get(3).playerKey.equals(players.get(1).playerKey))
                                    players.set(3, players.get(1));

                                players.get(2).addGame(newGame);
                                players.get(3).addGame(newGame);

                                if (!changedPlayers.contains(finalNewPlayerOneKey))
                                    changedPlayers.add(finalNewPlayerOneKey);
                                if (!changedPlayers.contains(finalNewPlayerTwoKey))
                                    changedPlayers.add(finalNewPlayerTwoKey);

                                gameReference.child(newGame.gameKey).setValue(newGame);

                                for (BadmintonGame laterGame : affectedGames)
                                {
                                    BadmintonPlayer laterPlayerOne = null;
                                    BadmintonPlayer laterPlayerTwo = null;

                                    for (BadmintonPlayer player : players)
                                    {
                                        if (player.playerKey.equals(laterGame.playerOneKey))
                                        {
                                            laterPlayerOne = player;
                                        }

                                        if (player.playerKey.equals(laterGame.playerTwoKey))
                                        {
                                            laterPlayerTwo = player;
                                        }

                                        if (laterPlayerOne != null && laterPlayerTwo != null)
                                            break;
                                    }

                                    if (laterGame != null && laterPlayerTwo != null)
                                    {
                                        if (changedPlayers.contains(laterGame.playerOneKey) && changedPlayers.contains(laterGame.playerTwoKey))
                                        {
                                            laterPlayerOne.deleteGame(laterGame);
                                            laterPlayerTwo.deleteGame(laterGame);
                                            RankCalculator.rankGame(laterGame, laterPlayerOne.playerRankingPoints, laterPlayerTwo.playerRankingPoints);
                                            laterPlayerOne.addGame(laterGame);
                                            laterPlayerTwo.addGame(laterGame);
                                        } else if (changedPlayers.contains(laterGame.playerOneKey))
                                        {
                                            laterPlayerOne.deleteGame(laterGame);
                                            laterPlayerTwo.deleteGame(laterGame);
                                            laterPlayerTwo.setPlayerRankingPoints(BadmintonGame.getStartingRank(laterGame, laterPlayerTwo));
                                            changedPlayers.add(laterPlayerTwo.playerKey);
                                            RankCalculator.rankGame(laterGame, laterPlayerOne.playerRankingPoints, laterPlayerTwo.playerRankingPoints);
                                            laterPlayerOne.addGame(laterGame);
                                            laterPlayerTwo.addGame(laterGame);
                                        } else if (changedPlayers.contains(laterGame.playerTwoKey))
                                        {
                                            laterPlayerOne.deleteGame(laterGame);
                                            laterPlayerTwo.deleteGame(laterGame);
                                            laterPlayerOne.setPlayerRankingPoints(BadmintonGame.getStartingRank(laterGame, laterPlayerOne));
                                            changedPlayers.add(laterPlayerOne.playerKey);
                                            RankCalculator.rankGame(laterGame, laterPlayerOne.playerRankingPoints, laterPlayerTwo.playerRankingPoints);
                                            laterPlayerOne.addGame(laterGame);
                                            laterPlayerTwo.addGame(laterGame);
                                        }
                                    }
                                    gameReference.child(laterGame.gameKey).setValue(laterGame);
                                }

                                for (BadmintonPlayer player : players)
                                {
                                    if (player.statistics == null || player.statistics.get(BadmintonPlayer.TOTAL_GAMES) == 0)
                                    {
                                        playerReference.child(player.playerKey).removeValue();
                                        playerNameReference.child(player.playerKey).removeValue();
                                    } else playerReference.child(player.playerKey).setValue(player);
                                }

                                addPointsChangeListener();


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private Query queryGamesAfter(String key)
    {
        return gameReference.orderByKey().startAt(key);
    }

    public void addGame(final BadmintonGame game)
    {
        playerNameReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                boolean playerOneExists = false, playerTwoExists = false;
                String playerOneKey = null, playerTwoKey = null;

                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    if (data.getValue().equals(game.playerOneName))
                    {
                        playerOneExists = true;
                        playerOneKey = data.getKey();
                    }

                    if (data.getValue().equals(game.playerTwoName))
                    {
                        playerTwoExists = true;
                        playerTwoKey = data.getKey();
                    }

                    if (playerOneExists && playerTwoExists) break;
                }

                if (!playerOneExists)
                {
                    playerOneKey = playerNameReference.push().getKey();
                    playerNameReference.child(playerOneKey).setValue(game.playerOneName);
                    ArrayList<Integer> chartingData = new ArrayList<>();
                    chartingData.add(STARTING_RANK);
                    playerReference.child(playerOneKey).setValue(new BadmintonPlayer(game.playerOneName, playerOneKey, STARTING_RANK, chartingData, new ArrayList<BadmintonGame>(), (HashMap<String, Integer>) emptyStatistics.clone()));
                }

                if (!playerTwoExists)
                {
                    playerTwoKey = playerNameReference.push().getKey();
                    playerNameReference.child(playerTwoKey).setValue(game.playerTwoName);
                    ArrayList<Integer> chartingData = new ArrayList<>();
                    chartingData.add(STARTING_RANK);
                    playerReference.child(playerTwoKey).setValue(new BadmintonPlayer(game.playerTwoName, playerTwoKey, STARTING_RANK, chartingData, new ArrayList<BadmintonGame>(), (HashMap<String, Integer>) emptyStatistics.clone()));
                }

                game.setPlayerOneKey(playerOneKey);
                game.setPlayerTwoKey(playerTwoKey);

                final String gameKey = gameReference.push().getKey();

                final String finalPlayerOneKey = playerOneKey;
                final String finalPlayerTwoKey = playerTwoKey;

                playerReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        BadmintonPlayer player1 = dataSnapshot.child(finalPlayerOneKey).getValue(BadmintonPlayer.class);
                        BadmintonPlayer player2 = dataSnapshot.child(finalPlayerTwoKey).getValue(BadmintonPlayer.class);
                        RankCalculator.rankGame(game, player1.playerRankingPoints, player2.playerRankingPoints);
                        game.setGameKey(gameKey);
                        gameReference.child(gameKey).setValue(game);
                        player1.addGame(game);
                        player2.addGame(game);
                        playerReference.child(finalPlayerOneKey).setValue(player1);
                        playerReference.child(finalPlayerTwoKey).setValue(player2);
                        addPointsChangeListener();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public Query playerRankingPointsQuery()
    {
        return playerReference.orderByChild("playerRankingPoints");
    }

    public Query gameQuery()
    {
        return gameReference.orderByChild("time").limitToLast(100);
    }

    public void deleteGame(final BadmintonGame badmintonGame)
    {
        final ArrayList<String> affectedPlayerKeys = new ArrayList<>();
        final ArrayList<BadmintonGame> affectedGames = new ArrayList<>();
        final ArrayList<String> changedPlayers = new ArrayList<>();

        affectedPlayerKeys.add(badmintonGame.playerOneKey);
        affectedPlayerKeys.add(badmintonGame.playerTwoKey);

        queryGamesAfter(badmintonGame.gameKey).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    BadmintonGame game = snapshot.getValue(BadmintonGame.class);

                    if (game != null && !game.gameKey.equals(badmintonGame.gameKey))
                    {
                        if (affectedPlayerKeys.contains(game.playerOneKey) && affectedPlayerKeys.contains(game.playerTwoKey))
                        {
                            affectedGames.add(game);
                        } else if (affectedPlayerKeys.contains(game.playerOneKey))
                        {
                            affectedPlayerKeys.add(game.playerTwoKey);
                            affectedGames.add(game);
                        } else if (affectedPlayerKeys.contains(game.playerTwoKey))
                        {
                            affectedPlayerKeys.add(game.playerOneKey);
                            affectedGames.add(game);
                        }
                    }
                }

                playerReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        ArrayList<BadmintonPlayer> players = new ArrayList<>();

                        for (String key : affectedPlayerKeys)
                        {
                            players.add(dataSnapshot.child(key).getValue(BadmintonPlayer.class));
                        }

                        for (int i = 0; i < 2; i++)
                        {
                            players.get(i).setPlayerRankingPoints(BadmintonGame.getStartingRank(badmintonGame, players.get(i)));
                            players.get(i).deleteGame(badmintonGame);
                            changedPlayers.add(players.get(i).playerKey);
                        }
                        gameReference.child(badmintonGame.gameKey).removeValue();

                        for (BadmintonGame laterGame : affectedGames)
                        {
                            BadmintonPlayer laterPlayerOne = null;
                            BadmintonPlayer laterPlayerTwo = null;

                            for (BadmintonPlayer player : players)
                            {
                                if (player.playerKey.equals(laterGame.playerOneKey))
                                {
                                    laterPlayerOne = player;
                                }

                                if (player.playerKey.equals(laterGame.playerTwoKey))
                                {
                                    laterPlayerTwo = player;
                                }

                                if (laterPlayerOne != null && laterPlayerTwo != null)
                                    break;
                            }

                            if (laterGame != null && laterPlayerTwo != null)
                            {
                                if (changedPlayers.contains(laterGame.playerOneKey) && changedPlayers.contains(laterGame.playerTwoKey))
                                {
                                    laterPlayerOne.deleteGame(laterGame);
                                    laterPlayerTwo.deleteGame(laterGame);
                                    RankCalculator.rankGame(laterGame, laterPlayerOne.playerRankingPoints, laterPlayerTwo.playerRankingPoints);
                                    laterPlayerOne.addGame(laterGame);
                                    laterPlayerTwo.addGame(laterGame);
                                } else if (changedPlayers.contains(laterGame.playerOneKey))
                                {
                                    laterPlayerOne.deleteGame(laterGame);
                                    laterPlayerTwo.deleteGame(laterGame);
                                    laterPlayerTwo.setPlayerRankingPoints(BadmintonGame.getStartingRank(laterGame, laterPlayerTwo));
                                    changedPlayers.add(laterPlayerTwo.playerKey);
                                    RankCalculator.rankGame(laterGame, laterPlayerOne.playerRankingPoints, laterPlayerTwo.playerRankingPoints);
                                    laterPlayerOne.addGame(laterGame);
                                    laterPlayerTwo.addGame(laterGame);
                                } else if (changedPlayers.contains(laterGame.playerTwoKey))
                                {
                                    laterPlayerOne.deleteGame(laterGame);
                                    laterPlayerTwo.deleteGame(laterGame);
                                    laterPlayerOne.setPlayerRankingPoints(BadmintonGame.getStartingRank(laterGame, laterPlayerOne));
                                    changedPlayers.add(laterPlayerOne.playerKey);
                                    RankCalculator.rankGame(laterGame, laterPlayerOne.playerRankingPoints, laterPlayerTwo.playerRankingPoints);
                                    laterPlayerOne.addGame(laterGame);
                                    laterPlayerTwo.addGame(laterGame);
                                }
                            }
                            gameReference.child(laterGame.gameKey).setValue(laterGame);
                        }

                        for (BadmintonPlayer player : players)
                        {
                            if (player.statistics == null || player.statistics.get(BadmintonPlayer.TOTAL_GAMES) == 0)
                            {
                                playerReference.child(player.playerKey).removeValue();
                                playerNameReference.child(player.playerKey).removeValue();
                            } else playerReference.child(player.playerKey).setValue(player);
                        }

                        addPointsChangeListener();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
