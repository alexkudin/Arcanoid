package com.ex.ak.arcanoid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{
    public ArcView arcanView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =========================================================
        this.arcanView  = (ArcView) this.findViewById(R.id.arcView);
        this.arcanView.activity = this;
        // =========================================================
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        ArcView.timeStart = Calendar.getInstance().getTimeInMillis();
        this.arcanView.paused = false;
        ArcView.delta = ArcView.timeOfGame;
        // Tell the gameView resume method to execute
        //arcanView.resume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        ArcView.setNewGameRun(false);

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Destroing all stats && creating new Game
        ArcView.setNewGameRun(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.newGame)
        {
            this.arcanView.MapCreate();
            this.arcanView.lives = 3;
            this.arcanView.racket    = new Racket(this.arcanView.screenX, this.arcanView.screenY, this.arcanView.speed);
            this.arcanView.ball.defaultBall(     this.arcanView.racket.getRacketX(),
                    this.arcanView.racket.getRacketY(),
                    this.arcanView.racket.getRacketLength());
            this.arcanView.MSVM.isRun = true;
            this.arcanView.NewGameRun = true;
            this.arcanView.delta = 0;
            this.arcanView.surfaceCreated(this.arcanView.getHolder());
            return true;
        }
        if(id == R.id.pause)
        {
            this.arcanView.paused = true;
            //this.arcanView.MSVM.stopRun();
            return true;
        }
        if(id == R.id.resume)
        {
            //this.arcanView  = (ArcView) this.findViewById(R.id.arcView);
            //this.arcanView.activity = this;
            this.arcanView.NewGameRun = false;
            this.arcanView.paused = false;
            this.arcanView.MSVM.isRun = true;
            ArcView.delta = ArcView.timeOfGame;

            this.arcanView.surfaceCreated(this.arcanView.getHolder());

            return true;
        }
        if(id == R.id.quit)
        {
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


