/*
*   Authors: James Zygmont & Eli Roman
*   Company Name: Digital Mantis Games
*   Project: Mafia Party Assistant
*   Date: 12/18/2014
*/

package com.digitalmantis.mafiaassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener {

    private Button btnStart;
    private EditText txtPlayers;
    private TextView lblInstructions, lblCredit;
    private int phase = 0, playerCount, curPlayerIndex = 0;
    private String[] roles, players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inflate the UI
        btnStart = (Button) findViewById(R.id.btnStart);
        txtPlayers = (EditText) findViewById(R.id.txtPlayers);
        lblCredit = (TextView) findViewById(R.id.lblCredit);
        lblInstructions = (TextView) findViewById(R.id.lblInstructions);
        btnStart.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View arg0) {
        // Handle the player count and the names
        if (phase == 0) {

            lblInstructions.setTextColor(Color.BLACK);
            lblInstructions.setText("");
            String input = txtPlayers.getText().toString();

            // Get the number of players and instantiate the array with that many spots
            if (input.length() != 0 && Integer.parseInt(input) >= 6) {
                playerCount = Integer.parseInt(txtPlayers.getText().toString());
                players = new String[playerCount];
                assignRoles();

                // Tell the players to enter their names
                lblCredit.setText("");
                btnStart.setText("Next");
                // Allow text for name input
                txtPlayers.setInputType(1);
                phase = 1;
                txtPlayers.setText("");
                lblInstructions.setText("Please enter player 1's name.");
                txtPlayers.setHint("Player name");
            } else {
                lblInstructions.setTextColor(Color.RED);
                lblInstructions.setText("You must have at least 6 players!");
            }

        } else if (phase == 1) {
            // Assign a name and role to each player
            if (curPlayerIndex < playerCount) {
                lblInstructions.setText("Please enter player " + (curPlayerIndex + 2) + "'s name");
                String playerName;

                if (!txtPlayers.getText().toString().equals("")) {
                    playerName = txtPlayers.getText().toString();
                } else {
                    playerName = "Player " + (curPlayerIndex + 1);
                }

                players[curPlayerIndex] = roles[curPlayerIndex].toString() + "##" + playerName;


                // Display the current player's role
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                alertDialog.setTitle("Player Created");
                alertDialog.setMessage("Congratulations, " + playerName + "!\nYou are " + roles[curPlayerIndex] + ".");
                alertDialog.show();

                // Clear the text box and move on to the next player
                txtPlayers.setText("");
                curPlayerIndex++;

                // Finish and move on
                if (curPlayerIndex == playerCount) {
                    phase = 2;
                    lblInstructions.setText("Press Next to Begin");
                    txtPlayers.setVisibility(View.INVISIBLE);
                }
            }
        } else if (phase == 2) {
            // Create a new intent focused on the game activity and pass in the player count
            Intent gameActivity = new Intent();
            gameActivity.setClassName("com.digitalmantis.mafiaassistant", "com.digitalmantis.mafiaassistant.VoteScreen");

            gameActivity.putExtra("players.array.key", players);

            startActivity(gameActivity);
        }

    }

    String[] assignRoles() {

        // Fill an array with the appropriate roles in a standard fashion
        roles = new String[playerCount];

        int numMafia = playerCount / 3;

        for (int i = 0; i <= numMafia; i++) {
            roles[i] = "Mafia";
        }

        for (int i = numMafia; i < playerCount; i++) {
            roles[i] = "Innocent";
        }

        roles[playerCount - 2] = "Detective";
        roles[playerCount - 1] = "Doctor";

        // Shuffle the roles and return the array
        return shuffle(roles);
    }

    // Shuffle an array randomly
    String[] shuffle(String[] array) {
        int currentIndex = array.length, randomIndex;
        String temporaryValue;

        while (0 != currentIndex) {
            randomIndex = (int) Math.floor(Math.random() * currentIndex);
            currentIndex -= 1;
            temporaryValue = array[currentIndex];
            array[currentIndex] = array[randomIndex];
            array[randomIndex] = temporaryValue;
        }

        return array;
    }
}
