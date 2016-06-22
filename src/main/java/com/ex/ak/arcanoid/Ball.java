package com.ex.ak.arcanoid;

import android.graphics.RectF;
import android.util.Log;

/**
 * Created by Alex on 13.06.2016.
 */
public class Ball
{
    private RectF rectBall;

    /**
     *  Ball dimensions
     */
    private int ballWidth;
    private int ballHeight;

    /**
     *  coordinates of top left corner of Ball
     */
    private double ballX;
    private double ballY;


    /**
     *  Ball Speed
     */
    private double ballXspeed;
    private double ballYspeed;

    /**
     *  direction of Ball move
     */
    private boolean Up   ;
    private boolean Right;

    public Ball(double racketX, double racketY, int racketLength, int scale , double Xspeed , double Yspeed )
    {
        this.Up    = false;
        this.Right = true;
        this.ballXspeed = Xspeed;
        this.ballYspeed = Yspeed;
        this.ballWidth = this.ballHeight = scale / 2;
        this.ballX = (racketX + racketLength / 2) - (this.ballWidth);           // X top left corner of Ball
        this.ballY =  racketY - this.ballHeight * 1.5 ;                         // Y top left corner of Ball
        //this.ballX = (screenX / 2) - (this.ballWidth / 2);         // X top left corner of Ball
        //this.ballY =  screenY - 65;                                // Y top left corner of Ball
        this.rectBall = new RectF(  (float)this.ballX,
                                    (float)this.ballY,
                                    (float)this.ballX  + this.ballWidth,
                                    (float)this.ballY  + this.ballHeight);
        Log.d("Width_Height_Ball", this.ballWidth + "----" + this.ballHeight);
        Log.d("Ball_Speed", this.ballXspeed + "<<>>" + this.ballYspeed);
        Log.d("Ball_X_Y", this.ballX + "<<--->>" + this.ballY);

    }


    /**
     * Method for calculating coordinates of corners
     *  for drawing Ball
     * @param updateSpeed
     */
    public void updateBall(double updateSpeed)
    {
        rectBall.left   = (float)(this.ballX + this.ballXspeed / updateSpeed);  // X of left top
        rectBall.top    = (float)(this.ballY + this.ballYspeed / updateSpeed);  // Y of left top
        rectBall.right  = (float)(rectBall.left + this.ballWidth);              // X of right bottom
        rectBall.bottom = (float)(rectBall.top  + this.ballHeight);             // Y of right bottom

        this.ballX = rectBall.left;
        this.ballY = rectBall.top;

        //Log.d("Ball_Update", this.ballX + "--->>" + this.ballY);
    }


    /**
     *  Method for setting ball in center of Racket
     *  in beginning of Game
     * @param racketX   - Racket coordinate X
     * @param racketY   - Racket coordinate Y
     * @param racketLength  - length of Racket
     */
    public void defaultBall(double racketX, double racketY, int racketLength)
    {
        this.ballX = (racketX + racketLength / 2) - (this.ballWidth);           // X top left corner of Ball
        this.ballY =  racketY - this.ballHeight * 1.5;                          // Y top left corner of Ball
        this.ballXspeed = 200;
        this.ballYspeed = 200;
        this.Up    = true;


        Log.d("Ball_Default", this.ballX + "--->>" + this.ballY + "speed : " + this.ballXspeed);

    }

    public void changeBallXdirection(){
        this.Right = !this.Right;
    }

    public void changeBallYdirection(){
        this.Up   = !this.Up;
    }

    public void changeBallXspeed(){
        this.ballXspeed = this.ballXspeed * -1;
    }

    public void changeBallYspeed(){
        this.ballYspeed = this.ballYspeed * -1;
    }

    public boolean getUpDirection() {
        return this.Up;
    }
    public boolean getRightDirection() {
        return this.Right;
    }
    public double getBallY() {
        return ballY;
    }
    public double getBallX() {
        return ballX;
    }
    public int getBallWidth() {
        return ballWidth;
    }
    public int getBallHeight() {
        return ballHeight;
    }
    public RectF getRectBall() {
        return rectBall;
    }
    public void setBallYspeed(double ballYspeed) {
        this.ballYspeed = ballYspeed;
    }
    public void setBallXspeed(double ballXspeed) {
        this.ballXspeed = ballXspeed;
    }
}


