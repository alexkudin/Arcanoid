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
import java.io.Serializable;
import java.util.Calendar;

/**
 * класс отвечающий
 * за прорисовку Арканоида
 */
public class ArcView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener, Serializable
{
    /**
     *  Класс-Поток выполняющий Отрисовку
     *      игрового поля ArcView
     */
    class MySurfaceViewMover extends Thread
    {
        public boolean isRun   = true;
        SurfaceHolder   holder  = ArcView.this.getHolder();
        Paint           P       = new Paint();
        Canvas          canvas;

        @Override
        public synchronized void run()
        {
            P.setAntiAlias(true);

            while (this.isRun && !paused)
            {
                //_____a
                //получаем canvas
                canvas = holder.lockCanvas(null); // или holder.lockCanvas();
                if (canvas == null) {     continue;      }

                //______б
                // ---------- отрисовка игрового поля -----------
                P.setColor(Color.DKGRAY);
                canvas.drawRect(0, 0, screenX, screenY, P);   // цвет фона виджета

                // --------- обновление биты и шарика ------------
                actionBall();


                // --------- set lives to Lives TextView ---------
                if(lives < 4)
                {
                    ArcView.this.activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public synchronized void run()
                        {
                            ArcView.this.livesTV.setText(String.valueOf(ArcView.this.lives));
                        }
                    });
                }

                // --------- set time to Time TextView -----------
                ArcView.this.activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public synchronized void run()
                    {
                        getCurrentTime();
                        timeOfGame = (timeCurrent - timeStart) + delta;
                        ArcView.this.timeTV.setText(String.valueOf(timeOfGame/1000));
                    }
                });

                // ------------- Dialog GameOver ----------------
                if(lives < 0)
                {
                    this.stopRun();

                    ArcView.this.activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            AlertDialog.Builder biulder = new AlertDialog.Builder(context);
                            biulder.setTitle("Game Over");
                            biulder.setMessage("If you want start new game Press OK, Press Cancel if you want Exit");
                            biulder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    MapCreate();
                                    lives = 3;
                                    racket    = new Racket(screenX, screenY, speed);
                                    ball.defaultBall(   racket.getRacketX(),
                                                        racket.getRacketY(),
                                                        racket.getRacketLength());
                                    MSVM.isRun = true;
                                    NewGameRun = true;
                                    delta = 0;
                                    surfaceCreated(ArcView.this.getHolder());
                                    //Log.d("click ok - dialog", "<><><><><>");
                                }
                            });
                            biulder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    System.exit(0);
                                }
                            });
                            dialogGameOver = biulder.create();
                            dialogGameOver.show();

                            try
                            {
                                ArcView.this.MSVM.isRun = false;
                                ArcView.this.MSVM.join();   // обязательно дождаться завершения работы потока, выполняющего отрисовку
                                //Log.d("!!!!! stop Thread","!!!!!!!");
                            }
                            catch (InterruptedException ie){ ie.getMessage();}
                        }
                    });
                }

                // ------------- Dialog isWin ----------------
                chkWin();

                if(isWin)
                {
                    this.stopRun();

                    ArcView.this.activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            AlertDialog.Builder biulder = new AlertDialog.Builder(context);
                            biulder.setTitle("You Win");
                            biulder.setMessage("If you want start new game Press OK, Press Cancel if you want Exit");
                            biulder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    bricksCnt = 0;
                                    delta = 0;
                                    lives = 3;
                                    isWin = false;
                                    MapCreate();
                                    racket    = new Racket(screenX, screenY, speed);
                                    ball.defaultBall(   racket.getRacketX(),
                                                        racket.getRacketY(),
                                                        racket.getRacketLength());
                                    MSVM.isRun = true;
                                    MSVM.goRun();
                                    NewGameRun = true;

                                    surfaceCreated(ArcView.this.getHolder());
                                }
                            });
                            biulder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    System.exit(0);
                                }
                            });
                            dialogGameOver = biulder.create();
                            dialogGameOver.show();

                            try
                            {
                                ArcView.this.MSVM.isRun = false;
                                ArcView.this.MSVM.join();   // обязательно дождаться завершения работы потока, выполняющего отрисовку
                            }
                            catch (InterruptedException ie){ ie.getMessage();}
                        }
                    });
                }

                // ----------- отрисовка Кирпичей ---------------
                for (int i = 0; i < bricks.length; i++)
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

                // ----------- отрисовка Мяча ---------------
                P.setColor(Color.GREEN);
                canvas.drawOval(ball.getRectBall(), P);

                // ----------- отрисовка Ракетки ---------------
                P.setColor(Color.rgb( 0,255,255));
                canvas.drawRect(racket.getRectRacket(), P);

                racket.updateRacket(screenX ,speed);
                ball.updateBall(speed);
                //isWin(bricks , bricksCnt);  // check for win
                //Log.d(">>>>>>>", " Counter : "   + bricksCnt);

                holder.unlockCanvasAndPost(canvas);

                try
                {
                    sleep(30);
                }
                catch (InterruptedException ie){ ie.getMessage();}
            }
        }

        public void stopRun()
        {
            this.isRun = false;
        }
        public void goRun()
        {
            this.isRun = true;
        }
    }

    public MySurfaceViewMover MSVM;                        // экземпляр класса-ПотокаОтрисовки
    boolean isWin = false;
    public static boolean NewGameRun = true;                // статус игры : Новая - true или Продолжается - false
    public boolean paused = false;                          // if game is on pause == true
    public static int screenX;                             // ширина игрового поля
    public static int screenY;                             // высота игрового поля
    public static int SCALE = 30;                           // масштаб после пересчета
    public static int DEFAULT_SCALE = 30;                   // масштаб изначально
    public static final float MODE_LDPI = 0.75f;
    public static final float MODE_MDPI = 1.0f;
    public static final float MODE_HDPI = 1.5f;
    public static final float MODE_XHDPI = 2.0f;
    public static final float MODE_XHDPI_3 = 3.0f;
    public static final float MODE_DEFAULT = 1.0f;
    public static double     brickWidth;                   // высота Кирпича
    public static double     brickHeight;                  // ширина Кирпича
    public static double     brickPadding = 4.0;           // padding для Кирпича
    private Bitmap     brick = BitmapFactory.decodeResource(this.getResources(), R.drawable.brick);
    public static Brick [][] bricks  = new Brick [4][10];  // массив Кипичей
    public static Racket     racket;                       // Ракетка
    public static Ball       ball;                         // Мяч
    public static double     speed   = 15;                 // Условная скорость(от нее пересчитываются все остальные скорости)
    public int               lives   = 3;                  // Количество жизней изначально
    //public String       myTime = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().get(Calendar.SECOND));
    public static long        timeStart;
    public static long        timeCurrent;
    public static long        timeOfGame;
    public static long        delta = 0;
    public int                bricksCnt;
    public TextView livesTV;
    public TextView timeTV;
    public Context context;
    Activity activity;                                      // ссылка на МэйнАктивити
    public AlertDialog dialogGameOver;                      // диалог конца игры

    public void getCurrentTime()
    {
        Calendar c = Calendar.getInstance();
        timeCurrent = c.getTimeInMillis();
    }

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
        screenX = this.getWidth();
        screenY = this.getHeight();

        //Log.d("<<<<<<<", " xPixels: "  + screenX);
        //Log.d(">>>>>>>", " yPixels: "   + screenY);

        //Получаю характеристики экрана (узнаю какой режим включен)
        Display display             = this.getDisplay();
        DisplayMetrics metrics      = new DisplayMetrics();
        display.getMetrics(metrics);

        //Log.d("<<<<<<<", " heightPixels: "  + metrics.heightPixels);
        //Log.d(">>>>>>>", " widthPixels: "   + metrics.widthPixels);
        //Log.d("------", " DPI: " + metrics.densityDpi);
        //Log.d("------", " density: " + metrics.density);

        //проверяю если режим экрана больше дефолтного (1.0f)
        //корректирую размер ячейки в зависимости от текущего режима
        //увеличиваю размер ячейки (количество пикселей)
        if(metrics.density > MODE_DEFAULT)
        {
            //0.75
            if(metrics.density == MODE_LDPI)
            {
                SCALE = (int) (DEFAULT_SCALE * MODE_LDPI);
                Log.d("//////", "SCALE = " + SCALE);
            }
            //1.5
            else if(metrics.density == MODE_HDPI)
            {
                SCALE = (int) (DEFAULT_SCALE * MODE_HDPI);
                Log.d("//////", "SCALE = " + SCALE);
            }
            //2.0
            else if(metrics.density == MODE_XHDPI)
            {
                SCALE = (int) (DEFAULT_SCALE * MODE_XHDPI);
                Log.d("//////", "SCALE = " + SCALE);
            }
            //3.0
            else if(metrics.density == MODE_XHDPI_3)
            {
                SCALE = (int) (DEFAULT_SCALE * MODE_XHDPI_3);
                Log.d("//////", "SCALE = " + SCALE);
            }
        }
        //Log.d("Current Scale", "SCALE (AFTER) = " + SCALE);

        brickWidth     = (SCALE + (brickPadding * 2));
        brickHeight    = brickWidth / 2;
        this.brick     = Bitmap.createScaledBitmap(this.brick,(int)brickWidth,(int)brickHeight,true);

        //Log.d("BrickWidth",  "brWidth = " + brickWidth);
        //Log.d("BrickHeight", "brHeight = " + brickHeight);

        if(NewGameRun)
        {
            racket    = new Racket(screenX, screenY, speed);
            ball      = new Ball(   racket.getRacketX(),
                                    racket.getRacketY(),
                                    racket.getRacketLength() , SCALE , 200 , 200);
            timeStart = Calendar.getInstance().getTimeInMillis();
            MapCreate();
        }

        this.MSVM.start();

        timeStart = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.d("-----", "width = " + width + " height = " + height);

        //Делаю оптимальный расчет ширины и высоты SurfaceView, подогнаный под размер SCALE
        screenX = this.getWidth();
        screenY = this.getHeight();

        Log.d("Surf_Changed>>>>>", " xPixels: "  + screenX);
        Log.d("Surf_Changed>>>>>", " yPixels: "  + screenY);

        Log.d("Surf_Changed , Height = ", String.valueOf(height / SCALE));
        Log.d("Surf_Changed , Width  = ", String.valueOf(width / SCALE));

        if(this.livesTV == null)
        {
            this.livesTV = (TextView)this.activity.findViewById(R.id.tvLives);
        }
        this.livesTV.setText(String.valueOf(this.lives));

        if(this.timeTV == null)
        {
            this.timeTV = (TextView)this.activity.findViewById(R.id.tvTime);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)    {
        try
        {
            this.MSVM.stopRun();
            this.MSVM.join();   // обязательно дождаться завершения работы потока, выполняющего отрисовку
        }
        catch (InterruptedException ie)  {  Log.d("Exception : ",ie.getMessage()); }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)    {
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

    public static void setNewGameRun(boolean NewGame) {
        NewGameRun = NewGame;
    }

    public void MapCreate()    {
        this.bricksCnt = 0;
        for (int i = 0; i < bricks.length ; i++)
        {
            for (int j = 0; j < bricks[i].length; j++)
            {
                bricks[i][j] = new Brick(i, j, brickWidth, brickHeight, brickPadding);
                bricks[i][j].setVisible(true);
                this.bricksCnt++;
            }
        }
        //Log.d("<<<>>>>", " bricksCnt: "  + bricksCnt);
    }

    public void actionBall()    {
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
            if ((ball.getRectBall().bottom) >= screenY)
            {
                this.lives --;

                ball.defaultBall(   racket.getRacketX(),
                        racket.getRacketY(),
                        racket.getRacketLength());

            }

            //Colliding with Brick`s
            {
                for (int i = 0; i < bricks.length; i++)
                {
                    for (int j = 0;  j < bricks[i].length; j++)
                    {
                        if( bricks[i][j].Visible())
                        {
                            if (RectF.intersects( bricks[i][j].getRectBrick(), ball.getRectBall() ))
                            {
                                //bricksCnt--;
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

    public void chkWin()
    {
        int bricksCounter = this.bricksCnt;

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
        this.isWin = bricksCounter < 1;
        //Log.d("chkWin","brickCnt = " + bricksCounter);
    }
}
