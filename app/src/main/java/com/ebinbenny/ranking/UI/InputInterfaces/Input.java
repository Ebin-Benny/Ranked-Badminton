package com.ebinbenny.ranking.UI.InputInterfaces;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import com.ebinbenny.ranking.Breakdown.BadmintonGame;
import com.ebinbenny.ranking.Breakdown.Game;
import com.ebinbenny.ranking.Manager.DBConnection;
import com.ebinbenny.ranking.Other.EmptyTokenizer;
import com.ebinbenny.ranking.R;

import java.util.ArrayList;

public class Input
{

    private final ArrayList<EditText> playerOneScoreEditTexts = new ArrayList<>();
    private final ArrayList<EditText> playerTwoScoreEditTexts = new ArrayList<>();
    private final ArrayList<TextInputLayout> playerOneTextLayouts = new ArrayList<>();
    private final ArrayList<TextInputLayout> playerTwoTextLayouts = new ArrayList<>();
    private boolean editMode;
    private DBConnection dbConnection;
    private Context context;
    private FloatingActionButton inputButton;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView title;
    private MultiAutoCompleteTextView playerOneNameInput, playerTwoNameInput;
    private BadmintonGame gameToEdit;
    private Drawable addDrawable, tickDrawable, editDrawable;
    private Drawable[] layers;
    private int fabColors[];
    private View view;
    private Activity activity;
    private int fabRed, fabGreen;

    public Input(int gameType, DBConnection dbConnection, Context context, View view, Activity activity)
    {
        this.dbConnection = dbConnection;
        this.context = context;
        this.view = view;
        this.activity = activity;

        addDrawable = ContextCompat.getDrawable(context, R.drawable.ic_add_white_64dp);
        tickDrawable = ContextCompat.getDrawable(context, R.drawable.ic_check_white_64dp);
        editDrawable = ContextCompat.getDrawable(context, R.drawable.ic_edit_white_64dp);

        layers = new Drawable[]{addDrawable, tickDrawable};

        editMode = false;

        fabGreen = ContextCompat.getColor(context, R.color.fabFirstPosition);
        fabRed = ContextCompat.getColor(context, R.color.fabSecondPosition);
        fabColors = new int[]{fabGreen, fabRed};

        switch (gameType)
        {
            case Game.BADMINTON:
                initialiseBadmintonLayout();
            default:
                break;
        }
    }

    public void setPlayerTokens(String[] names)
    {
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, names);
        playerOneNameInput.setAdapter(arrayAdapter);
        playerTwoNameInput.setAdapter(arrayAdapter);
    }

    public void initialiseBadmintonLayout()
    {
        inputButton = (FloatingActionButton) view.findViewById(R.id.input_button);
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setPeekHeight(0);

        View inputLayout = view.findViewById(R.id.input_layout);
        title = (TextView) inputLayout.findViewById(R.id.title);
        title.setText(R.string.add_game);

        playerOneNameInput = (MultiAutoCompleteTextView) inputLayout.findViewById(R.id.name_input_1);
        playerTwoNameInput = (MultiAutoCompleteTextView) inputLayout.findViewById(R.id.name_input_2);

        final TextInputLayout playerOneNameInputLayout = (TextInputLayout) inputLayout.findViewById(R.id.name_input_layout_1);
        TextInputLayout playerTwoNameInputLayout = (TextInputLayout) inputLayout.findViewById(R.id.name_input_layout_2);


        playerOneNameInputLayout.setHint("Player 1");
        playerTwoNameInputLayout.setHint("Player 2");

        playerOneNameInput.setTokenizer(new EmptyTokenizer());
        playerTwoNameInput.setTokenizer(new EmptyTokenizer());

        playerOneScoreEditTexts.add((EditText) inputLayout.findViewById(R.id.score_input_1_1));
        playerOneScoreEditTexts.add((EditText) inputLayout.findViewById(R.id.score_input_2_1));
        playerOneScoreEditTexts.add((EditText) inputLayout.findViewById(R.id.score_input_3_1));
        playerOneTextLayouts.add((TextInputLayout) inputLayout.findViewById(R.id.text_input_layout_1_1));
        playerOneTextLayouts.add((TextInputLayout) inputLayout.findViewById(R.id.text_input_layout_2_1));
        playerOneTextLayouts.add((TextInputLayout) inputLayout.findViewById(R.id.text_input_layout_3_1));


        playerTwoScoreEditTexts.add((EditText) inputLayout.findViewById(R.id.score_input_1_2));
        playerTwoScoreEditTexts.add((EditText) inputLayout.findViewById(R.id.score_input_2_2));
        playerTwoScoreEditTexts.add((EditText) inputLayout.findViewById(R.id.score_input_3_2));
        playerTwoTextLayouts.add((TextInputLayout) inputLayout.findViewById(R.id.text_input_layout_1_2));
        playerTwoTextLayouts.add((TextInputLayout) inputLayout.findViewById(R.id.text_input_layout_2_2));
        playerTwoTextLayouts.add((TextInputLayout) inputLayout.findViewById(R.id.text_input_layout_3_2));

        playerTwoNameInput.setNextFocusForwardId(playerOneScoreEditTexts.get(0).getId());

        for (int i = 1; i < 3; i++)
        {
            playerOneTextLayouts.get(i).setVisibility(View.GONE);
            playerTwoTextLayouts.get(i).setVisibility(View.GONE);

            final int finalI = i;

            TextWatcher textWatcher = new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {

                }

                @Override
                public void afterTextChanged(Editable editable)
                {
                    if (!TextUtils.isEmpty(playerOneScoreEditTexts.get(finalI - 1).getText()) && !TextUtils.isEmpty(playerTwoScoreEditTexts.get(finalI - 1).getText()))
                    {

                        int playerOneSetOneScore = Integer.parseInt(playerOneScoreEditTexts.get(0).getText().toString());
                        int playerTwoSetOneScore = Integer.parseInt(playerTwoScoreEditTexts.get(0).getText().toString());

                        int playerOneSetTwoScore = 0, playerTwoSetTwoScore = 0;

                        if (!TextUtils.isEmpty(playerOneScoreEditTexts.get(1).getText()) && !TextUtils.isEmpty(playerTwoScoreEditTexts.get(1).getText()))
                        {
                            playerOneSetTwoScore = Integer.parseInt(playerOneScoreEditTexts.get(1).getText().toString());
                            playerTwoSetTwoScore = Integer.parseInt(playerTwoScoreEditTexts.get(1).getText().toString());
                        }

                        boolean hasWinner = (playerOneSetOneScore > playerTwoSetOneScore && playerOneSetTwoScore > playerTwoSetTwoScore)
                                || (playerTwoSetOneScore > playerOneSetOneScore && playerTwoSetTwoScore > playerOneSetTwoScore);

                        if (!hasWinner)
                        {
                            playerOneTextLayouts.get(finalI).setVisibility(View.VISIBLE);
                            playerTwoTextLayouts.get(finalI).setVisibility(View.VISIBLE);
                        }
                    } else if (TextUtils.isEmpty(playerOneScoreEditTexts.get(finalI).getText()) && TextUtils.isEmpty(playerTwoScoreEditTexts.get(finalI).getText()))
                    {
                        playerOneTextLayouts.get(finalI).setVisibility(View.GONE);
                        playerTwoTextLayouts.get(finalI).setVisibility(View.GONE);
                    }
                }
            };

            playerOneScoreEditTexts.get(i - 1).addTextChangedListener(textWatcher);
            playerTwoScoreEditTexts.get(i - 1).addTextChangedListener(textWatcher);

        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
        {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                {
                    resetInputs();
                    editMode = false;
                    title.setText(R.string.add_game);
                    fabColors[1] = fabRed;
                    layers[1] = tickDrawable;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {

                if (slideOffset > 1) slideOffset = 1;
                else if (slideOffset < 0) slideOffset = 0;
                inputButton.setBackgroundTintList(ColorStateList.valueOf((Integer) new ArgbEvaluator().evaluate(slideOffset, fabColors[0], fabColors[1])));
                int alpha1 = (int) ((1 - (2 * slideOffset)) * 255);
                if (alpha1 > 255) alpha1 = 255;
                else if (alpha1 < 0) alpha1 = 0;
                layers[0].setAlpha(alpha1);
                int alpha2 = (int) ((1 - (2 * (1 - slideOffset))) * 255);
                if (alpha2 > 255) alpha2 = 255;
                else if (alpha2 < 0) alpha2 = 0;
                layers[1].setAlpha((alpha2));
                LayerDrawable layerDrawable = new LayerDrawable(layers);
                inputButton.setImageDrawable(layerDrawable);
            }
        });

        inputButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                {
                    boolean error = false;

                    if (TextUtils.isEmpty(playerOneNameInput.getText()))
                    {
                        playerOneNameInput.setError("Enter Name");
                        error = true;
                    }

                    if (TextUtils.isEmpty(playerTwoNameInput.getText()))
                    {
                        playerTwoNameInput.setError("Enter Name");
                        error = true;
                    }

                    for (int i = 0; i < playerOneScoreEditTexts.size(); i++)
                    {
                        if (!TextUtils.isEmpty(playerOneScoreEditTexts.get(i).getText()) && TextUtils.isEmpty(playerTwoScoreEditTexts.get(i).getText()))
                        {
                            error = true;
                            playerTwoScoreEditTexts.get(i).setError("Enter points");
                        } else if (!TextUtils.isEmpty(playerOneScoreEditTexts.get(i).getText()) && TextUtils.isEmpty(playerTwoScoreEditTexts.get(i).getText()))
                        {
                            error = true;
                            playerOneScoreEditTexts.get(i).setError("Enter points");
                        }
                    }

                    if (TextUtils.isEmpty(playerOneScoreEditTexts.get(0).getText()) && TextUtils.isEmpty(playerTwoScoreEditTexts.get(0).getText()))
                    {
                        error = true;
                        playerOneScoreEditTexts.get(0).setError("Enter points");
                        playerTwoScoreEditTexts.get(0).setError("Enter points");
                    }

                    if (!error)
                    {
                        for (int i = 0; i < playerOneScoreEditTexts.size(); i++)
                        {
                            if (!TextUtils.isEmpty(playerOneScoreEditTexts.get(i).getText()) && !TextUtils.isEmpty(playerTwoScoreEditTexts.get(i).getText()))
                            {
                                int playerOneScore = Integer.parseInt(playerOneScoreEditTexts.get(i).getText().toString());
                                int playerTwoScore = Integer.parseInt(playerTwoScoreEditTexts.get(i).getText().toString());
                                int difference = Math.abs(playerOneScore - playerTwoScore);

                                if ((playerOneScore == playerTwoScore) || (playerOneScore > 21 && difference != 2) || (playerTwoScore > 21 && difference != 2) || (playerTwoScore < 21 && playerOneScore < 21))
                                {
                                    playerOneScoreEditTexts.get(i).setError("Enter valid score");
                                    playerTwoScoreEditTexts.get(i).setError("Enter valid score");
                                    error = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!error)
                    {

                        String playerOneName = playerOneNameInput.getText().toString();
                        String playerTwoName = playerTwoNameInput.getText().toString();

                        ArrayList<Integer> playerOneScores = new ArrayList<>();
                        ArrayList<Integer> playerTwoScores = new ArrayList<>();

                        for (int i = 0; i < playerOneScoreEditTexts.size(); i++)
                        {
                            if (!TextUtils.isEmpty(playerOneScoreEditTexts.get(i).getText()) && !TextUtils.isEmpty(playerTwoScoreEditTexts.get(i).getText()))
                            {
                                playerOneScores.add(Integer.parseInt(playerOneScoreEditTexts.get(i).getText().toString()));
                                playerTwoScores.add(Integer.parseInt(playerTwoScoreEditTexts.get(i).getText().toString()));
                            }
                        }

                        if (editMode)
                            dbConnection.editGame(gameToEdit, new BadmintonGame(playerOneName, playerTwoName, playerOneScores, playerTwoScores, gameToEdit.time));
                        else
                            dbConnection.addGame(new BadmintonGame(playerOneName, playerTwoName, playerOneScores, playerTwoScores));

                        if (activity.getCurrentFocus() != null)
                        {
                            InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                        }
                        view.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            }
                        }, 100);
                    }

                } else
                {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
    }

    public void editGame(BadmintonGame game)
    {
        playerOneNameInput.setText(game.playerOneName);
        playerTwoNameInput.setText(game.playerTwoName);
        title.setText(R.string.edit_game);

        for (int i = 0; i < game.playerTwoScores.size(); i++)
        {
            playerOneScoreEditTexts.get(i).setText(String.valueOf(game.playerOneScores.get(i)));
            playerOneScoreEditTexts.get(i).setVisibility(View.VISIBLE);
            playerTwoScoreEditTexts.get(i).setText(String.valueOf(game.playerTwoScores.get(i)));
            playerTwoScoreEditTexts.get(i).setVisibility(View.VISIBLE);
        }

        this.gameToEdit = game;
        editMode = true;
        fabColors[1] = fabGreen;
        layers[1] = editDrawable;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }


    public void resetInputs()
    {
        playerOneNameInput.setText("");
        playerOneNameInput.setError(null);
        playerTwoNameInput.setText("");
        playerTwoNameInput.setText(null);

        for (int i = 0; i < playerOneScoreEditTexts.size(); i++)
        {
            playerOneScoreEditTexts.get(i).setText("");
            playerOneScoreEditTexts.get(i).setError(null);
            playerTwoScoreEditTexts.get(i).setText("");
            playerTwoScoreEditTexts.get(i).setError(null);
        }

        activity.getCurrentFocus().clearFocus();

    }

}
