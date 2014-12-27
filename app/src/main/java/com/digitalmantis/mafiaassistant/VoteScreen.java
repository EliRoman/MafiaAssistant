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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class VoteScreen extends Activity implements OnClickListener {

    private Player[] players;
    private TextView lblCurDay, lblResults;
    private Button btnNextPhase;
    private boolean gameOver = false, innocentVictory = false, docIsDead = false, detectiveIsDead = false;
    private String choiceID;
    private String[] deathFlavors, calmFlavors, victoryFlavors;
    private int curDay = 0, phaseIndex = 0, innocentCount = 0, mafiaCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inflate the UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_screen);
        lblCurDay = (TextView) findViewById(R.id.lblCurDay);
        lblResults = (TextView) findViewById(R.id.lblResults);
        btnNextPhase = (Button) findViewById(R.id.btnNextPhase);
        btnNextPhase.setOnClickListener(this);

        loadTextResources();

        // Get the string array and instantiate the players array
        String[] temp = getIntent().getStringArrayExtra("players.array.key");
        players = new Player[temp.length];

        // Take the array of pseudo-players and turn them into real players, as well as incremement the appropriate counters
        for (int i = 0; i < temp.length; i++) {
            String[] curPlayer = temp[i].split("\\#\\#");
            players[i] = new Player(curPlayer[0], curPlayer[1]);
            if (players[i].getRole().equals("Mafia")) {
                mafiaCount++;
            } else {
                innocentCount++;
            }
        }

        // The current night is the current day plus 1 because we started one the first night
        lblCurDay.setText("Night: " + (curDay + 1));

    }

    public void doVote(final String voter) {

        // Create a new array list because we don't know how many people are going to be included in the voting popup
        ArrayList<String> dialogItems = new ArrayList<String>();

        // Build the menu appropriately
        for (Player player : players) {

            // If the current player is not dead, then add them to the voting list
            if (!player.getHealthStatus().equals("Dead")) {
                if (!player.getRole().equals(voter) || player.getRole().equals("Doctor") || player.getRole().equals("Innocent")) {
                    dialogItems.add(player.getName());
                }
            }
        }

        // Create the dialog for the popup
        AlertDialog.Builder votingPopup = new AlertDialog.Builder(this);

        if (voter.equals("Detective")) {
            votingPopup.setTitle("Who to investigate?");
        } else if (voter.equals("Mafia")) {
            votingPopup.setTitle("Who to Kill");
        } else if (voter.equals("Doctor")) {
            votingPopup.setTitle("Who to Save");
        } else if (voter.equals("Innocent")) {
            votingPopup.setTitle("Who to Exile");
        }

        // Fill the popup with the appropriate names
        votingPopup.setItems(dialogItems.toArray(new String[dialogItems.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                ArrayList<String> dialogIDs = new ArrayList<String>();

                for (Player player : players) {

                    // If the current player is not dead, then add them to the voting list
                    if (!player.getHealthStatus().equals("Dead")) {
                        if (!player.getRole().equals(voter) || player.getRole().equals("Doctor") || player.getRole().equals("Innocent")) {
                            dialogIDs.add(player.getPlayerID());
                        }
                    }

                }

                // Take the selection of the player and find their unique ID
                choiceID = dialogIDs.get(selection);

                for (Player player : players) {
                    if (player.getPlayerID().equals(choiceID)) {
                        handleVote(voter, player);
                        break;
                    }
                }
            }
        });
        votingPopup.show();

    }

    // TODO: Implement music to indicate whose turn it is to vote
    // Dialogs and basic actions to perform when voting
    public void handleVote(String voter, Player player) {

        // Create the votingResultPopup object
        AlertDialog votingResultPopup = new AlertDialog.Builder(this).create();
        votingResultPopup.setButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // TODO: Find alternative to setButton() if needed, maybe find a way to clean this up more?
        if (voter.equals("Detective")) {
            // Display the investigated player's role
            votingResultPopup.setTitle("Player Investigated");
            votingResultPopup.setMessage(player.getName() + " is " + player.getRole() + "!");
        } else if (voter.equals("Mafia")) {
            // Inform the mafia of their choice again
            player.setHealthStatus(player.getHealthTypes()[2]);
            votingResultPopup.setTitle("Player Hit");
            votingResultPopup.setMessage("You put a hit on " + player.getName() + "!");
        } else if (voter.equals("Doctor")) {
            // Indicate which player was saved
            player.setHealthStatus(player.getHealthTypes()[0]);
            votingResultPopup.setTitle("Player Saved");
            votingResultPopup.setMessage(player.getName() + " is safe from the Mafia!");
        } else if (voter.equals("Innocent")) {
            // Exile the player
            player.setHealthStatus(player.getHealthTypes()[1]);

            // If somebody was exiled, decrement
            if (player.getRole().equals("Mafia")) {
                mafiaCount--;
            } else {
                innocentCount--;
            }

            //detect if the exiled player has a special role so that later we wont call them
            if (player.getRole().equals("Detective")) {
                detectiveIsDead = true;
            } else if (player.getRole().equals("Doctor")) {
                docIsDead = true;
            }

            // Check to see if the conditions for a victory have been met, otherwise display the role of the exiled player
            checkVictory();
            votingResultPopup.setTitle("Player Exiled");
            votingResultPopup.setMessage(player.getName() + " was " + player.getRole() + "!");
            phaseIndex = 0;
        }

        // Show the built dialog
        votingResultPopup.show();
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

            switch (phaseIndex) {
                case 0:
                    lblCurDay.setText("Night: " + curDay);
                    // If the detective isn't dead, let him vote
                    if (!detectiveIsDead) {
                        doVote("Detective");
                    }
                    break;
                case 1:
                    // Let the mafia vote
                    doVote("Mafia");
                    break;
                case 2:
                    // If the doctor isn't dead, let him vote
                    if (!docIsDead) {
                        doVote("Doctor");
                    }
                    break;
                case 3:
                    // Show the results and continue to the night
                    showResults();
                    curDay++;
                    lblCurDay.setText("Day: " + (curDay + 1));
                    break;
                case 4:
                    doVote("Innocent");
                    phaseIndex = -1;
                    break;
            }

            // Increment to the next part of the night/day
            phaseIndex++;
        } else {
            // TODO: Add unique victory text
            if (innocentVictory) {
                lblResults.setText("Daily Happenings:\n\nThe mafia have been driven out of Ponty Pandy and the innocents have won!");
            } else {
                lblResults.setText("Daily Happenings:\n\nThe innocents have been driven out of Ponty Pandy and the mafia have won!");
            }
        }
    }

    public void showResults() {
        for (Player player : players) {
            if (player.getHealthStatus().equals(player.getHealthTypes()[2])) {
                player.setHealthStatus(player.getHealthTypes()[1]);

                // If somebody died, decrement the innocent counter
                innocentCount--;

                // Detect if the killed player has a special role so that later we wont call them
                if (player.getRole().equals("Detective")) {
                    detectiveIsDead = true;
                }
                if (player.getRole().equals("Doctor")) {
                    docIsDead = true;
                }

                // TODO: Add more flavor text for a more engaging experience
                lblResults.setText("Daily Happenings:\n\n" + player.getName() + " has died in a horrific accident!\n" + player.getName() + " was " + player.getRole() + "!");
                checkVictory();

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

    public String generateDeathFlavor() {

        return "";
    }

    // Load all text resources
    // TODO: Merge all text files into a single resource
    public void loadTextResources() {

//        InputStream calmRes = getResources().openRawResource(R.raw.death_flavor);
//        InputStream victoryRes = getResources().openRawResource(R.raw.death_flavor);


        // TODO: Change absolute path into relative path
        String line;
        ArrayList<String> rawInput = new ArrayList<String>();
//        try {
//            // Read the text from resources into deathFlavors
//            InputStream deathRes = getResources().getAssets().open("D:/AndroidStudioProjects/MafiaAssistant/app/src/main/res/raw/deathflavor.txt");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(deathRes));
//            line = reader.readLine();
//            while (line != null) {
//                rawInput.add(line);
//            }
//
//            deathFlavors = rawInput.toArray(new String[rawInput.size()]);
//
//            for (String flavor : deathFlavors) {
//                System.out.println(flavor);
//            }
//        } catch (IOException e) {
//            // TODO: Add some way to handle file loading exception or ignore them completely
//        }
    }
}