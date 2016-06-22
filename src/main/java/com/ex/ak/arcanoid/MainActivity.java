package com.ex.ak.arcanoid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    public ArcView arcanView;
    public TextView livesTV;
    public TextView timeTV;
    public AlertDialog dialog2;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.livesTV = (TextView)this.findViewById(R.id.tvLives);
        this.timeTV  = (TextView)this.findViewById(R.id.tvTime);

        /**
         *  диалог не создается  (((
         */
        /*
        AlertDialog.Builder biulder = new AlertDialog.Builder(this);
        biulder.setTitle("Game Over");
        biulder.setMessage("You loose !");
        biulder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        biulder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog2 = biulder.create();
        */

        //this.arcanView = (ArcView)this.findViewById(R.id.arcView);
        //this.livesTV.setText(String.valueOf(arcanView.lives));
    }

    // This method executes when the player starts the game
    @Override
    protected void onResume()
    {
        super.onResume();

        // Tell the gameView resume method to execute
        //arcanView.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause()
    {
        super.onPause();

        // Tell the gameView pause method to execute
        //arcanView.pause();
    }
}

