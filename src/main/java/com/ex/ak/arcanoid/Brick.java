package com.ex.ak.arcanoid;

import android.graphics.RectF;

/**
 * Created by Alex on 17.06.2016.
 */
public class Brick
{
    private RectF rectBrick;

    /**
     * Dimentions of brick
     */
    private double brickWidth;
    private double brickHeight;
    private double padding;
    private double brickX;
    private double brickY;

    //существует блок или он разбит
    private boolean Visible = true;

    public Brick(int row, int column , double brickWidth, double brickHeight, double brickPadding)                                // ! ! ! ! !
    {
        this.rectBrick  = new RectF((float)(brickWidth + brickPadding )    * column ,
                (float)(brickHeight + brickPadding )   * row,
                (float)((brickWidth + brickPadding )   * column    + brickWidth),
                (float)((brickHeight + brickPadding )  * row       + brickHeight));
        this.brickX         = (float)(brickWidth + brickPadding )    * column;
        this.brickY         = (float)(brickHeight + brickPadding )   * row;
        this.brickWidth     = brickWidth;
        this.brickHeight    = brickHeight;
        this.padding        = brickPadding;
    }

    public double getBrickHeight() {
        return brickHeight;
    }
    public double getBrickY() {
        return brickY;
    }
    public double getBrickX() {
        return brickX;
    }
    public double getBrickWidth() {
        return brickWidth;
    }
    public void setVisible(boolean visible) {
        this.Visible = visible;
    }
    public boolean Visible() {
        return this.Visible;
    }
    public RectF getRectBrick() {
        return rectBrick;
    }
}

