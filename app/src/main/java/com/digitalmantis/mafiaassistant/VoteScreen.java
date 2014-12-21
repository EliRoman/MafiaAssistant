/*
*   Authors: Eli Roman & James Zygmont
*   Company Name: Digital Mantis Games
*   Project: Mafia Party Assistant
*   Date: 12/18/2014
*/

package com.digitalmantis.mafiaassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class VoteScreen extends Activity implements OnClickListener {


    private Player[] players;
    private TextView lblCurDay, lblResults;
    private Button btnNextPhase;
    private boolean gameOver = false, innocentVictory = false, docIsDead = false, detectiveIsDead = false;
    private String choiceID;
    private int curDay = 0, phaseIndex = 0, choiceIndex, innocentCount = 0, mafiaCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_screen);
        lblCurDay = (TextView) findViewById(R.id.lblCurDay);
        lblResults = (TextView) findViewById(R.id.lblResults);
        btnNextPhase = (Button) findViewById(R.id.btnNextPhase);
        btnNextPhase.setOnClickListener(this);

        // Get the string array and instantiate the players array
        String[] temp = getIntent().getStringArrayExtra("players.array.key");
        players = new Player[temp.length];

        // Take the array of pseudo-players and turn them into real players
        for (int i = 0; i < temp.length; i++) {
            String[] curPlayer = temp[i].split("\\#\\#");
            players[i] = new Player(curPlayer[0], curPlayer[1]);
            if (players[i].getRole().equals("Mafia")) {
                mafiaCount++;
            } else {
                innocentCount++;
            }
        }

        lblCurDay.setText("Night: " + (curDay + 1));

    }

    public void doVote(final String voter) {
        ArrayList<String> dialogItems = new ArrayList<String>();

        // Build the menu appropriately
        for (int i = 0; i < players.length; i++) {

            // If the current player is not dead, then add them to the voting list
            if (!players[i].getHealthStatus().equals("Dead")) {
                if (!players[i].getRole().equals(voter) || players[i].getRole().equals("Doctor") || players[i].getRole().equals("Innocent")) {
                    dialogItems.add(players[i].getName());
                }
            } else {
                continue;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (voter.equals("Detective")) {
            builder.setTitle("Who to investigate?");
        } else if (voter.equals("Mafia")) {
            builder.setTitle("Who to Kill");
        } else if (voter.equals("Doctor")) {
            builder.setTitle("Who to Save");
        } else if (voter.equals("Innocent")) {
            builder.setTitle("Who to Exile");
        }

        builder.setItems(dialogItems.toArray(new String[dialogItems.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                ArrayList<String> dialogIDs = new ArrayList<String>();

                // Build the menu appropriately
                for (int i = 0; i < players.length; i++) {

                    // If the current player is not dead, then add them to the voting list
                    if (!players[i].getHealthStatus().equals("Dead")) {
                        if (!players[i].getRole().equals(voter) || players[i].getRole().equals("Doctor") || players[i].getRole().equals("Innocent")) {
                            dialogIDs.add(players[i].getPlayerID());
                        }
                    } else {
                        continue;
                    }

                }

                choiceIndex = selection;
                choiceID = dialogIDs.get(choiceIndex);

                for (int i = 0; i < players.length; i++) {
                    if (players[i].getPlayerID().equals(choiceID)) {
                        calculateVote(voter, players[i]);
                        break;
                    }
                }
            }
        });
        builder.show();

    }

    // Dialogs and basic actions to perform when voting
    public void calculateVote(String voter, Player player) {

        // TODO: Find alternative to setButton()
        if (voter.equals("Detective")) {
            // Display the investigated player's role
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alertDialog.setTitle("Player Investigated");
            alertDialog.setMessage(player.getName() + " is " + player.getRole() + "!");
            alertDialog.show();
        } else if (voter.equals("Mafia")) {
            player.setHealthStatus(player.getHealthTypes()[2]);
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alertDialog.setTitle("Player Hit");
            alertDialog.setMessage("You put a hit on " + player.getName() + "!");
            alertDialog.show();
        } else if (voter.equals("Doctor")) {
            // Save the player
            player.setHealthStatus(player.getHealthTypes()[0]);
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alertDialog.setTitle("Player Saved");
            alertDialog.setMessage(player.getName() + " is safe from the Mafia!");
            alertDialog.show();
        } else if (voter.equals("Innocent")) {
            // Exile the player
            player.setHealthStatus(player.getHealthTypes()[1]);
            if (player.getRole().equals("Mafia")) {
                mafiaCount--;
            } else {
                innocentCount--;
            }

            //detect if the exiled player has a special role so that later we wont call them
            if (player.getRole().equals("Detective")) {
                detectiveIsDead = true;
            }
            if (player.getRole().equals("Doctor")) {
                docIsDead = true;
            }

            checkVictory();
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alertDialog.setTitle("Player Exiled");
            alertDialog.setMessage(player.getName() + " was " + player.getRole() + "!");
            alertDialog.show();
            phaseIndex = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vote_screen, menu);
        return true;
    }

    @Override
    public void onClick(View arg0) {
        if (!gameOver) {
            if (phaseIndex == 0) {
                if (!detectiveIsDead) {//if detective is not dead display detective screen
                    doVote("Detective");
                }
                phaseIndex++;
            } else if (phaseIndex == 1) {
                doVote("Mafia");
                phaseIndex++;
            } else if (phaseIndex == 2) {

                if (!docIsDead) {//if doctor is not dead display doctor screen
                    doVote("Doctor");
                }
                phaseIndex++;
            } else if (phaseIndex == 3) {
                showResults();
                curDay++;
                lblCurDay.setText("Day: " + (curDay + 1));
                phaseIndex++;
            } else if (phaseIndex == 4) {
                doVote("Innocent");

                phaseIndex = 0;
            }
        } else {
            if (innocentVictory) {
                lblResults.setText("Daily Happenings:\n\nThe mafia have been driven out of Ponty Pandy and the innocents have won!");
            } else {
                lblResults.setText("Daily Happenings:\n\nThe innocents have been driven out of Ponty Pandy and the mafia have won!");
            }
        }
    }

    public void showResults() {
        for (int i = 0; i < players.length; i++) {
            if (players[i].getHealthStatus().equals(players[i].getHealthTypes()[2])) {
                players[i].setHealthStatus(players[i].getHealthTypes()[1]);

                innocentCount--;

                //detect if the killed player has a special role so that later we wont call them
                if (players[i].getRole().equals("Detective")) {
                    detectiveIsDead = true;
                }
                if (players[i].getRole().equals("Doctor")) {
                    docIsDead = true;
                }

                // TODO: Add more flavor text for a more engaging experience
                lblResults.setText("Daily Happenings:\n\n" + players[i].getName() + " has died in a horrific accident!\n" + players[i].getName() + " was " + players[i].getRole() + "!");
                break;
            }

            lblResults.setText("Daily Happenings:\n\nIt was a quiet night in our fine town.");

        }
    }

    // If the mafia outnumber the innocent, then they win.
    // If there are no more mafia, then the innocents win
    public void checkVictory() {
        if (mafiaCount > innocentCount) {
            gameOver = true;
            innocentVictory = false;
        } else if (mafiaCount == 0) {
            gameOver = true;
            innocentVictory = true;
        }
    }
}