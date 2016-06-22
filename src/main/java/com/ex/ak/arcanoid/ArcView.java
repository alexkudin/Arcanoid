package com.ex.ak.arcanoid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;


/**
 * класс отвечающий
 * за прорисовку Арканоида
 */
public class ArcView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener
{

    class MySurfaceViewMover extends Thread
    {
        private boolean isRun   = true;
        SurfaceHolder   holder  = ArcView.this.getHolder();
        Paint           P       = new Paint();
        Canvas          canvas;

        @Override
        public void run()
        {
            P.setAntiAlias(true);

            while (this.isRun)
            {
                //_____a
                //получаем canvas
                canvas = holder.lockCanvas(null); // или holder.lockCanvas();
                if (canvas == null) {     continue;      }

                //______б
                //отрисовка игрового поля
                P.setColor(Color.DKGRAY);
                canvas.drawRect(0, 0, screenX, screenY, P);   // цвет фона виджета

                //обновление биты и шарика

                actionBall();

                if(ArcView.this.lives < 3)
                {
                    //Уменьшаешь количество жизней


                    ArcView.this.activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ArcView.this.livesTV.setText("Lives: " + ArcView.this.lives);
                        }
                    });
                }

                for (int i = 3; i < bricks.length; i++)
                {
                    for (int j = 0;  j < bricks[i].length; j++)
                    {
                        if(bricks[i][j].Visible())
                        {
                            //canvas.drawRect(bricks[i][j].getRectBrick(), P);
                            canvas.drawBitmap(ArcView.this.brick, bricks[i][j].getRectBrick().left,bricks[i][j].getRectBrick().top, P);
                        }
                    }
                }


                P.setColor(Color.GREEN);
                canvas.drawOval(ball.getRectBall(), P);

                P.setColor(Color.rgb( 0,255,255));
                canvas.drawRect(racket.getRectRacket(), P);

                racket.updateRacket(screenX ,speed);
                ball.updateBall(speed);
                //isWin(bricks , bricksCnt);  // check for win
                //Log.d(">>>>>>>", " Counter : "   + bricksCnt);

                holder.unlockCanvasAndPost(canvas);

                try
                {
                    this.sleep(30);
                }
                catch (InterruptedException ie){}
            }
        }

        public void stopRun()
        {
            this.isRun = false;
        }
    }

    private MySurfaceViewMover MSVM;

    boolean isWin = false;
    boolean gameRun;            // status of game : Run or Not
    boolean paused = true;      // if game is paused == true
    private int screenX;        // ширина игрового поля
    private int screenY;        // высота игрового поля
    public int SCALE = 30;      // Размер ячейки изначально
    public int DEFAULT_SCALE = 30;
    public static final float MODE_LDPI = 0.75f;
    public static final float MODE_MDPI = 1.0f;
    public static final float MODE_HDPI = 1.5f;
    public static final float MODE_XHDPI = 2.0f;
    public static final float MODE_XHDPI_3 = 3.0f;
    public static final float MODE_DEFAULT = 1.0f;
    private double     brickWidth;                // высота Кирпича
    private double     brickHeight;               // ширина Кирпича
    private double     brickPadding = 4.0;        // padding для Кирпича
    private Bitmap     brick = BitmapFactory.decodeResource(this.getResources(), R.drawable.brick);
    private Brick [][] bricks  = new Brick [8][10];
    private Racket     racket;
    private Ball       ball;
    private double     speed   = 15;
    public int         lives   = 3;
    public int         bricksCnt;
    public TextView livesTV;
    public Context context;
    Activity activity;
    //public Activity activity;


    /**
     * Регистрируюсь у SurfaceHolder-а на получение событий
     */
    {
        this.getHolder().addCallback(this);
        setOnTouchListener(this);
    }
    public ArcView(Context context)    {
        super(context);
        this.context = context;
    }
    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }
    public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        this.MSVM = new MySurfaceViewMover();

        //вычисление размеров игрового поля и размеров игрового блока в зависимости от размеров экрана
        this.screenX = this.getWidth();
        this.screenY = this.getHeight();
        Log.d("<<<<<<<", " xPixels: "  + this.screenX);
        Log.d(">>>>>>>", " yPixels: "   + this.screenY);

        //----------------------------------------------------------------------------------
        //Получаю характеристики экрана (узнаю какой режим включен)
        Display display = this.getDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Log.d("<<<<<<<", " heightPixels: "  + metrics.heightPixels);
        Log.d(">>>>>>>", " widthPixels: "   + metrics.widthPixels);

        Log.d("------", " DPI: " + metrics.densityDpi);
        Log.d("------", " density: " + metrics.density);

        //проверяю если режим экрана больше дефолтного (1.0f)
        //корректирую размер ячейки в зависимости от текущего режима
        //увеличиваю размер ячейки (количество пикселей)
        if(metrics.density > MODE_DEFAULT)
        {
            //0.75
            if(metrics.density == MODE_LDPI)
            {
                this.SCALE = (int) (this.DEFAULT_SCALE * MODE_LDPI);
                Log.d("//////", "SCALE = " + this.SCALE);
            }
            //1.5
            else if(metrics.density == MODE_HDPI)
            {
                this.SCALE = (int) (this.DEFAULT_SCALE * MODE_HDPI);
                Log.d("//////", "SCALE = " + this.SCALE);
            }
            //2.0
            else if(metrics.density == MODE_XHDPI)
            {
                this.SCALE = (int) (this.DEFAULT_SCALE * MODE_XHDPI);
                Log.d("//////", "SCALE = " + this.SCALE);
            }
            //3.0
            else if(metrics.density == MODE_XHDPI_3)
            {
                this.SCALE = (int) (this.DEFAULT_SCALE * MODE_XHDPI_3);
                Log.d("//////", "SCALE = " + this.SCALE);
            }
        }
        Log.d("Current Scale", "SCALE (AFTER) = " + this.SCALE);

        this.brickWidth     = (this.SCALE + (this.brickPadding * 2));
        this.brickHeight    = this.brickWidth / 2;
        this.brick          = Bitmap.createScaledBitmap(this.brick,(int)this.brickWidth,(int)this.brickHeight,true);

        Log.d("BrickWidth",  "brWidth = " + this.brickWidth);
        Log.d("BrickHeight", "brHeight = " + this.brickHeight);

        this.racket    = new Racket(this.screenX, this.screenY, this.speed);
        this.ball      = new Ball(  this.racket.getRacketX(), this.racket.getRacketY(),
                                    this.racket.getRacketLength() , this.SCALE , 200 , 200);
        /*this.ball.defaultBall(  this.racket.getRacketX(),
                                this.racket.getRacketY(),
                                this.racket.getRacketLength());*/

        MapCreate();
        this.MSVM.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.d("-----", "width = " + width + " height = " + height);

        //Делаю оптимальный расчет ширины и высоты SurfaceView, подогнаный под размер SCALE
        this.screenX = this.getWidth();
        this.screenY = this.getHeight();
        Log.d("Surf_Changed>>>>>", " xPixels: "  + this.screenX);
        Log.d("Surf_Changed>>>>>", " yPixels: "   + this.screenY);

        Log.d("Surf_Changed , Height = ",String.valueOf(height / this.SCALE));
        Log.d("Surf_Changed , Width  = ",String.valueOf(width / this.SCALE));


        if(this.livesTV == null)
        {
            this.livesTV = (TextView)this.activity.findViewById(R.id.tvLives);
        }
        //Записываю текущее значение счетчика (по умолчанию = 3) при первом создании SurfaceView
        this.livesTV.setText("Lives: " + this.lives);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        try
        {
            this.MSVM.stopRun();
            this.MSVM.join();   // обязательно дождаться завершения работы потока, выполняющего отрисовку
        }
        catch (InterruptedException ie)  {  Log.d("Exception : ",ie.getMessage()); }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() > racket.getRectRacket().centerX())
                {
                    racket.setRacketDirection(Racket.RACKET_RIGHT);
                }
                else
                {
                    racket.setRacketDirection(Racket.RACKET_LEFT);
                }
                break;

            case MotionEvent.ACTION_UP:
                racket.setRacketDirection(Racket.RACKET_STOP);
                break;
        }
        return true;
    }

    public void MapCreate()
    {
        for (int i = 0; i < this.bricks.length ; i++)
        {
            for (int j = 0; j < this.bricks[i].length; j++)
            {
                bricks[i][j] = new Brick(i, j, this.brickWidth, this.brickHeight, this.brickPadding);
                bricks[i][j].setVisible(true);
                //bricksCnt++;
            }
        }
    }


    public void actionBall()			// checking ball position && collisions with bricks,walls and bottom
    {
        if (!this.isWin && this.lives > -1)
        {
            //left Wall
            if (ball.getRectBall().left <= 0)
            {
                ball.changeBallXdirection();
                ball.changeBallXspeed();
            }
            // right wall
            if((ball.getBallX() + ball.getBallWidth()) >=  screenX)
            {
                ball.changeBallXdirection();
                ball.changeBallXspeed();
            }
            //Ball collide with Racket
            if (RectF.intersects(ball.getRectBall(), racket.getRectRacket()))
            {
                ball.changeBallYdirection();
                ball.changeBallYspeed();
            }
            //Ball collide with Roof
            if (ball.getBallY() <= 0)
            {
                ball.changeBallYdirection();
                ball.changeBallYspeed();
            }

            //Ball goes to Bottom
            if ((ball.getRectBall().bottom) >= this.screenY)
            {
                this.lives --;


                ball.defaultBall(this.racket.getRacketX(),
                        this.racket.getRacketY(),
                        this.racket.getRacketLength());

            }

            //Colliding with Brick`s
            {
                for (int i = 0; i < bricks.length; i++)
                {
                    for (int j = 0;  j< bricks[i].length; j++)
                    {
                        if( bricks[i][j].Visible())
                        {
                            if (RectF.intersects(bricks[i][j].getRectBrick(), ball.getRectBall()))
                            {
                                if(ball.getBallY() <= (bricks[i][j].getBrickY() + bricks[i][j].getBrickHeight()))
                                {
                                    bricks[i][j].setVisible(false);
                                    ball.changeBallYdirection();
                                    ball.changeBallYspeed();
                                }
                                else
                                if (ball.getBallX() <= (bricks[i][j].getBrickX() + bricks[i][j].getBrickWidth()))
                                {
                                    bricks[i][j].setVisible(false);
                                    ball.changeBallXdirection();
                                    ball.changeBallXspeed();
                                }
                                else
                                if ((ball.getBallX() + ball.getBallWidth()) >= bricks[i][j].getBrickX() )
                                {
                                    bricks[i][j].setVisible(false);
                                    ball.changeBallXdirection();
                                    ball.changeBallXspeed();
                                }
                                else
                                if((ball.getBallY() + ball.getBallHeight()) >= bricks[i][j].getBrickY() )
                                {
                                    bricks[i][j].setVisible(false);
                                    ball.changeBallYdirection();
                                    ball.changeBallYspeed();
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            ball.setBallXspeed(0);
            ball.setBallYspeed(0);
            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if(this.isWin)
            {
                Log.d("You Win! Congrats!!", " G A M E  O V E R ");
            }
            else
            {
                Log.d("GAME OVER", " G A M E  O V E R ");
            }

        }
    }

    /*
    public void isWin(Brick [][] bricks , int bricksCounter)
    {
        for (int i = 0; i < this.bricks.length ; i++)
        {
            for (int j = 0; j < this.bricks[i].length; j++)
            {
                if (!bricks[i][j].Visible())
                {
                    bricksCounter--;
                }
            }
        }
        isWin = bricksCounter <= 0;
    }
    */

    public int getLives() {
        return lives;
    }
}
// This is the end of our ArcView  class
// -----------------------------------------------------------