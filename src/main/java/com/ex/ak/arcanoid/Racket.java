package com.ex.ak.arcanoid;

import android.graphics.RectF;

/**
 * Created by Alex on 13.06.2016.
 */
public class Racket
{
    private RectF rectRacket;

    /**
     *  Racket dimentions
     */
    private int racketLength = 150;
    private int racketHeight = 40;

    /**
     *  coordinates of top left corner of Racket
     */
    private double racketX;
    private double racketY;

    /**
     *  Racket Speed
     */
    private double racketSpeed;

    /**
     *  direction of Racket move
     */
    private int racketMoving;
    public final static int RACKET_STOP = 0;
    public final static int RACKET_LEFT = 1;
    public final static int RACKET_RIGHT = 2;

    /**
     * @param screenWidth   - Width     of game field
     * @param screenHeight  - Height    of game field
     * @param speed         - Speed     of Racket move
     */
    public Racket(int screenWidth, int screenHeight, double speed)
    {
        this.racketX = (screenWidth / 2) - (this.racketLength / 2);
        this.racketY = screenHeight - this.racketHeight;
        this.rectRacket = new RectF((float)this.racketX,                        // X of left top
                                    (float)this.racketY,                        // Y of left top
                                    (float)this.racketX + this.racketLength,    // X of right bottom
                                    (float)this.racketY + this.racketHeight);   // Y of right bottom
        this.racketSpeed = speed * 15;
    }

    public RectF getRectRacket() {
        return rectRacket;
    }

    public void setRacketDirection(int movingDir) {
        this.racketMoving = movingDir;
    }

    public void updateRacket(int screenX , double updateSpeed)
    {
        switch (this.racketMoving)
        {
            case RACKET_LEFT:
                this.racketX -= this.racketSpeed / updateSpeed;
                if (this.racketX <= 1)
                {
                    this.racketX = 1;
                }
                break;

            case RACKET_RIGHT:
                this.racketX += this.racketSpeed / updateSpeed;
                if (this.racketX >= (screenX - this.racketLength))
                {
                    this.racketX = screenX - this.racketLength;
                }
                break;

            case RACKET_STOP:
            default:
                break;
        }
        rectRacket.left     = (float) racketX;
        rectRacket.right    = (float)(racketX + racketLength);
    }

    public int getRacketLength() {
        return racketLength;
    }

    public int getRacketHeight() {
        return racketHeight;
    }

    public double getRacketX() {
        return racketX;
    }

    public double getRacketY() {
        return racketY;
    }
}
