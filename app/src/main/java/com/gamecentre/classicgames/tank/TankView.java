package com.gamecentre.classicgames.tank;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gamecentre.classicgames.model.Game;
import com.gamecentre.classicgames.model.TankGameModel;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;
import com.gamecentre.classicgames.utils.ButtonListener;
import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.utils.RemoteMessageListener;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


public class TankView extends View implements RemoteMessageListener, ButtonListener {

    /** Debug tag */
    @SuppressWarnings("unused")
    private static final String TAG = "TankView";
    protected static final int FPS = 20;
    public static final float TO_SEC = 1000f/FPS;
    public static final int
            STAGE_COMPLETE = 1,
            GAME_OVER = 2,
            PAUSE = 3,
            END_GAME = 4;

    public static int GOLD_LEVEL = 0;

    public static int EVENT = 0;

    public static int CHECKING_RETRY = 0;

    public static int STARTING_BULLETS = 3;

    /**
     * This is mostly deprecated but kept around if the need
     * to add more game states comes around.
     */
    private TankView.State mCurrentState = TankView.State.Running;
    private TankView.State mLastState = TankView.State.Stopped;
    private boolean started = false;
    public static Context context;

    public static TankView instance;

    public static int WIDTH;
    public static int HEIGHT;
    public static float SCALE;
    public static float RESIZE = 1;
    public static Bitmap graphics;
    private final int NUM_LEVELS = 150;
    private final float LPROB = 0.6f/NUM_LEVELS;
    public static int level = 0;
    private boolean enemyBoat = false;

//    private int sW, sH;

    public static ArrayList<ArrayList<ImageView>> enemyCount;




    public static enum State { Running, Stopped}

    /** Flag that marks this view as initialized */
    private boolean mInitialized = false;
    private boolean round_started = false;

    protected boolean updatingRemote = false;
    protected boolean drawing = false;
    protected boolean notifyStageComplete = false;
    protected boolean notifyGameOver = false;
    protected boolean notifyPause = false;
    protected boolean notifyEndGame = false;

    /** Preferences loaded at startup */
    private int mTankSpeedModifier;

    /** Lives modifier */
    private int mLivesModifier;

    /** Starts a new round when set to true */
    public static boolean mNewRound = true;

    /** Keeps the game thread alive */
    private boolean mContinue = true;

    /** Mutes sounds when true */
    private boolean mSound = false;
    private static boolean mVibrate = false;
    private static Vibrator mVibrator;

    private Player P2, P1;
    private Eagle eagle;
    public static ArrayList<ArrayList<Bitmap>> tankBitmap;
    public static Sprite tankSprite;
    public Drawable eCountImg;
    public Bitmap eCountBm;
    private ArrayList<Enemy> Enemies;
    private ArrayList<ArrayList<GameObjects>> levelObjects;
    ArrayList<int[]> levelObjectsUpdate;
    private ArrayList<Bush> levelBushes;
    private ArrayList<Integer> levelBushesUpdate;
    public static Bonus bonus;
    public static Gold gold;
    private int new_enemy_time = 0;
    private final int genEnemyTime = (int)TO_SEC;
    private final int MAX_ENEMIES = 4;
    private final int[][] eaglePos = {{11,25},{11,24},{11,23},{12,23},{13,23},{14,23},{14,24},{14,25}};

    private  Bitmap curtain;
    public static int tile_dim;
    private int curtainFrame = 0;
    private int curtainFrameTmr = 0;
    private int curtainFrameTime = (int)(0.01*TO_SEC);
    private boolean closingCurtain = false;
    private boolean openingCurtain = false;
    private boolean movingCurting = true;
    private boolean curtainPause = false;
    private int curtainPauseTmr;
    private int curtainPauseTime = (int)(0.4*TO_SEC);
    Paint curtainPaint = new Paint();
    Rect curtainTRect;
    Rect curtainBRect;

    public static boolean gameover = false;
    public static boolean stageComplete = false;

    private int scoreFrame;
    private int enemyFrame;
    private int scoreFrameTmr;
    private int scoreFrameTime = (int)(0.05*TO_SEC);
    private boolean showingScore = false;

    private int showScoreDelay = (int)(2*TO_SEC);
    private int showScoreTmr = 0;

    private int newStageTmr = (int)(2*TO_SEC);


    public static ConcurrentLinkedQueue<Game> gameModel;


    public static boolean freeze = false;
    public static int freezeTmr = 0;
    public static int FreezeTime = (int)(8*TankView.TO_SEC);

    public static boolean protectEagle = false;
    public static int protectEagleTmr = 0;
    public static int ProtectEagleTime = (int)(20*TankView.TO_SEC);

    public static boolean ENEMY_BOOST = true;


    private boolean drawStarted = false;
    private boolean startSound = false;
    private boolean playerReady = false;

    public static Typeface typeface;
    public static Paint txtPaint;

    /** Touch boxes for various functions. These are assigned in initialize() */
    private Rect mPauseTouchBox;

    /** Timestamp of the last frame created */
    private long mLastFrame = 0;

//    protected ArrayList<TankView.Bullet> bullets = new ArrayList<>();

    /** Random number generator */
    private static final Random RNG = new Random();

    /** Pool for our sound effects */
    protected SoundPool mPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

    protected int mWinSFX, mMissSFX, mPaddleSFX, mWallSFX, mShootSFX, mHitSFX, mBrickSFX;

    /** Paint object */
    private final Paint mPaint = new Paint();

    /** Padding for touch zones and paddles */
    private static final int PADDING = 3;

    /** Redraws the screen according to FPS */
    private TankView.RefreshHandler mRedrawHandler = new TankView.RefreshHandler();

//    private TankView.RemoteUpdateHandler mRemoteUpdateHandler = new TankView.RemoteUpdateHandler();

    /** Flags indicating who is a player */
    public static boolean twoPlayers = false;

    /**
     * An overloaded class that repaints this view in a separate thread.
     * Calling PongView.update() should initiate the thread.
     * @author OEP
     *
     */
    class RefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            TankView.this.update();
            TankView.this.invalidate(); // Mark the view as 'dirty'
        }

        public void sleep(long delay) {
            this.removeMessages(0);
            this.sendMessageDelayed(obtainMessage(0), delay);
        }
    }


    class RemoteUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            while(true) {
                if (gameModel != null && !gameModel.isEmpty()) {
                    updatingRemote = true;
                    TankView.this.getRemoteUpdate();
                    updatingRemote = false;
                    TankView.this.invalidate();
                }
            }
        }
    }

    Thread tRemoteHandler = new Thread() {
        @Override
        public void run(){
            while (true){
                //we can't update the UI from here so we'll signal our handler and it will do it for us.
//                mRemoteUpdateHandler.sendMessage(null);
                if (gameModel != null && !gameModel.isEmpty() && !drawing) {
                    updatingRemote = true;
                    TankView.this.getRemoteUpdate();
                    updatingRemote = false;
                }
            }
        }
    };

    /**
     * Creates a new PongView within some context
     * @param context
     * @param attrs
     */
    public TankView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TankView.this.context = context;
        instance = this;
        constructView();
    }

    public TankView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        instance = this;
        constructView();
    }

    public static TankView getInstance() {
        return instance;
    }

    public Context getTankViewContext() {
        return context;
    }

    /**
     * Set the paddles to their initial states and as well the ball.
     */
    private void constructView() {

        setFocusable(true);
        MessageRegister.getInstance().setMsgListener(this);
        MessageRegister.getInstance().setButtonListener(this);

        Context ctx = this.getContext();
        SharedPreferences settings = ctx.getSharedPreferences("TankSettings", 0);
        loadPreferences(settings);

    }

    private  void loadGameObjects() {
        SpriteObjects.getInstance().insert(ObjectType.ST_TANK, 0, 0, 32, 32, 2, 10, true);

        SpriteObjects.getInstance().insert(ObjectType.ST_TANK_A, 128, 0, 32, 32, 2, 10, true);
        SpriteObjects.getInstance().insert(ObjectType.ST_TANK_B, 128, 64, 32, 32, 2, 10, true);
        SpriteObjects.getInstance().insert(ObjectType.ST_TANK_C, 128, 128, 32, 32, 2, 10, true);
        SpriteObjects.getInstance().insert(ObjectType.ST_TANK_D, 128, 192, 32, 32, 2, 10, true);

        SpriteObjects.getInstance().insert(ObjectType.ST_PLAYER_1, 640, 0, 32, 32, 2, 10, true); //50
        SpriteObjects.getInstance().insert(ObjectType.ST_PLAYER_2, 768, 0, 32, 32, 2, 10, true);

        SpriteObjects.getInstance().insert(ObjectType.ST_BRICK_WALL, 928, 0, 16, 16, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_STONE_WALL, 928, 144, 16, 16, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_WATER, 928, 160, 16, 16, 2, 10, true);
        SpriteObjects.getInstance().insert(ObjectType.ST_BUSH, 928, 192, 16, 16, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_ICE, 928, 208, 16, 16, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_BONUS_GRENADE, 896, 0, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BONUS_HELMET, 896, 32, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BONUS_CLOCK, 896, 64, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BONUS_SHOVEL, 896, 96, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BONUS_TANK, 896, 128, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BONUS_STAR, 896, 160, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BONUS_GUN, 896, 192, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BONUS_BOAT, 896, 224, 32, 32, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_SHIELD, 976, 0, 32, 32, 2, 2, true);
        SpriteObjects.getInstance().insert(ObjectType.ST_CREATE, 1008, 0, 32, 32, 10, 1, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_DESTROY_TANK, 1040, 0, 64, 64, 7, 1, false);//70
        SpriteObjects.getInstance().insert(ObjectType.ST_DESTROY_BULLET, 1108, 0, 32, 32, 5, 1, false); //40
        SpriteObjects.getInstance().insert(ObjectType.ST_BOAT_P1, 944, 96, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BOAT_P2, 976, 96, 32, 32, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_EAGLE, 944, 0, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_DESTROY_EAGLE, 1040, 0, 64, 64, 7, 1, false); //100
        SpriteObjects.getInstance().insert(ObjectType.ST_FLAG, 944, 64, 16, 16, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_BULLET, 944, 128, 8, 8, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_LEFT_ENEMY, 944, 144, 16, 16, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_STAGE_STATUS, 976, 64, 32, 32, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_TANKS_LOGO, 0, 260, 406, 72, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_CURTAIN, 928, 224, 16, 16, 1, 200, false);



    }

    protected void loadPreferences(SharedPreferences prefs) {

        mSound = prefs.getBoolean(TankMenuActivity.PREF_MUTED, mSound);
        SoundManager.enableSound(mSound);

        mVibrate = prefs.getBoolean(TankMenuActivity.PREF_VIBRATE, mVibrate);
        mVibrator = (Vibrator) (context.getSystemService(Context.VIBRATOR_SERVICE));

    }

    private void loadLevel(int level) {
        levelObjects = new ArrayList<>();
        levelBushes = new ArrayList<>();
        gold = new Gold();

        if(twoPlayers) {
            levelBushesUpdate = new ArrayList<>();
        }
        if(twoPlayers) {
            levelObjectsUpdate = new ArrayList<>();
        }
        BufferedReader reader;
        int row_count = 0;
        try {
            InputStream inputStream = context.getAssets().open(String.valueOf(level));
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();

            while(line != null){
                ArrayList<GameObjects> row = new ArrayList<>();
                Log.d("LEVEL", line);
                for (int i = 0; i < line.length(); i++){
                    GameObjects obj;
                    char c = line.charAt(i);
                    switch (c){
                        case '#' :
                            obj = new Brick(i,row_count);
                            break;
                        case '@' :
                            obj = new StoneWall(i,row_count);
                            break;
                        case '%' :
                            levelBushes.add(new Bush(i,row_count));
                            obj =  null;
                            break;
                        case '~' :
                            obj = new Water(i,row_count);
                            break;
                        case '-' :
                            obj = new Ice(i,row_count);
                            break;
                        default: obj = null;
                    }
                    row.add(obj);
                }
                levelObjects.add(row);
                ++row_count;
                line = reader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("LOADING LEVEL", String.valueOf(levelObjects.size()) + " " + levelObjects.get(0).size());

//        for(ArrayList<GameObjects> obj: levelObjects) {
//            while(obj.remove(null));
//        }

        eagle = new Eagle();

    }

    private void initializeGame() {
        SCALE = getResources().getDisplayMetrics().density;

        ViewGroup.LayoutParams params = TankView.this.getLayoutParams();
        int dim = (int)(TankView.this.getHeight()*0.95);
        Log.d("LAYOOUT", String.valueOf(dim));
        dim = (int)(dim/52f)*52;
        params.width = dim;//(int)(32*13*SCALE);
        params.height = dim;//(int)(32*13*SCALE);
//        TankView.this.setLayoutParams(params);
        TankView.this.layout(0,0,dim,dim);

//        ((TankActivity)context).boarder.layout(0,0,(int)(dim*0.8),(int)(dim*0.8));

        WIDTH = TankView.this.getWidth();
        HEIGHT = TankView.this.getHeight();
        tile_dim = (int)(HEIGHT/26);
        Log.d("VIEW DIM", WIDTH + " " + HEIGHT);
        Log.d("VIEW SCALE", String.valueOf(SCALE));

        graphics = BitmapFactory.decodeResource(context.getResources(), R.drawable.tanktexture);
        Bitmap test = Bitmap.createBitmap(graphics,0,0,32,32);
        int h = test.getHeight();
        RESIZE  = dim/(h*13f);

//        TankView.typeface = Typeface.createFromAsset(TankView.context.getAssets(),"prstartk.ttf");
        TankView.typeface = Typeface.createFromAsset(TankView.context.getAssets(),"arialbd.ttf");
        TankView.txtPaint = new Paint();
        txtPaint.setTypeface(TankView.typeface);
        txtPaint.setColor(Color.WHITE);
//        txtPaint.setStyle(Paint.Style.STROKE);
        txtPaint.setTextSize(10*SCALE);


        loadGameObjects();

        graphics = Bitmap.createScaledBitmap(graphics,(int)(RESIZE*graphics.getWidth()/SCALE),(int)(RESIZE*graphics.getHeight()/SCALE),false);

        tankSprite = SpriteObjects.getInstance().getData(ObjectType.ST_TANK);
        Bitmap bm = Bitmap.createBitmap(TankView.graphics, 0, 0 , tankSprite.w*28, tankSprite.h* tankSprite.frame_count*4);
        tankBitmap = new ArrayList<>();
        for(int i = 0; i < 8; i++){
            ArrayList<Bitmap> b = new ArrayList<>();
            for(int j = 0; j < 28; j++) {
                b.add(Bitmap.createBitmap(bm, j*tankSprite.w, i*tankSprite.h, tankSprite.w, tankSprite.h));
            }
            tankBitmap.add(b);
        }

        Sprite sprite = SpriteObjects.getInstance().getData(ObjectType.ST_LEFT_ENEMY);
        eCountBm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y, sprite.w, sprite.h);
        eCountImg = new BitmapDrawable(context.getResources(), eCountBm);
//        getEnemyCountView();

        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_FLAG);
        bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y, sprite.w, sprite.h);
        Drawable d = new BitmapDrawable(context.getResources(), bm);
        ((TankActivity)context).P1StatusImg.setBackground(d);

        if(twoPlayers) {
            bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y + sprite.h, sprite.w, sprite.h);
            d = new BitmapDrawable(context.getResources(), bm);
            ((TankActivity)context).P2StatusImg.setBackground(d);
        }

        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_STAGE_STATUS);
        bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y, sprite.w, sprite.h);
        d = new BitmapDrawable(context.getResources(), bm);
        ((TankActivity)context).StageFlag.setBackground(d);

        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_CURTAIN);
        bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y, sprite.w, sprite.h);
        curtain = Bitmap.createScaledBitmap(bm,WIDTH,bm.getHeight(),false);
        curtainPaint.setColor(Color.GRAY);
        curtainPaint.setStyle(Paint.Style.FILL);
        curtainTRect = new Rect();
        curtainBRect = new Rect();

        ((TankActivity)context).scoreView.setVisibility(View.INVISIBLE);

        resetScoreView();

        if(twoPlayers) {
            gameModel = new ConcurrentLinkedQueue<>();
            if(WifiDirectManager.getInstance().isServer()) {
                P1 = new Player(ObjectType.ST_PLAYER_1, getWidth() / 2 , getHeight() / 2 , 1);
                P2 = new Player(ObjectType.ST_PLAYER_2, getWidth() / 2, getHeight() / 2, 2);
            }
            else {
                P2 = new Player(ObjectType.ST_PLAYER_1, getWidth() / 2 , getHeight() / 2 , 2);
                P1 = new Player(ObjectType.ST_PLAYER_2, getWidth() / 2, getHeight() / 2, 1);
            }

            tRemoteHandler.start();
        }
        else {
            P1 = new Player(ObjectType.ST_PLAYER_1, getWidth() / 2 - 100, getHeight() / 2 - 100, 1);
            Log.d("PLAYERS", "ONE PLAYER");
        }
        Enemies = new ArrayList<>();
        bonus = new Bonus();

        TankView.GOLD_LEVEL = ((TankActivity)context).settings.getInt(TankActivity.GOLD_LEVEL,0);
    }

    public void retryStage() {
        if(twoPlayers) {
            gameModel = new ConcurrentLinkedQueue<>();
            if(WifiDirectManager.getInstance().isServer()) {
                P1 = new Player(ObjectType.ST_PLAYER_1, getWidth() / 2 , getHeight() / 2 , 1);
                P2 = new Player(ObjectType.ST_PLAYER_2, getWidth() / 2, getHeight() / 2, 2);
            }
            else {
                P2 = new Player(ObjectType.ST_PLAYER_1, getWidth() / 2 , getHeight() / 2 , 2);
                P1 = new Player(ObjectType.ST_PLAYER_2, getWidth() / 2, getHeight() / 2, 1);
            }
        }
        else {
            P1 = new Player(ObjectType.ST_PLAYER_1, getWidth() / 2 - 100, getHeight() / 2 - 100, 1);
        }

        bonus = new Bonus();
        mNewRound = true;
        if(((TankActivity)context).mInterstitialAd == null) {
            ((TankActivity) context).loadInterstitialAd();
        }
//        level;
    }

    /**
     * Reset the paddles/touchboxes/framespersecond/ballcounter for the next round.
     */
    private void nextRound() {
        drawStarted = false;
        startSound = false;
        notifyStageComplete = false;
        notifyGameOver = false;
        notifyPause = false;
        notifyEndGame = false;
        bonus.reset();

        ((TankActivity)context).scoreView.setVisibility(View.INVISIBLE);

        Enemy.lives = 20;
        getEnemyCountView();
        Enemies.clear();


//        level;
        clearLevel();
        loadLevel(level);
        ((TankActivity)context).StageTxt.setText(String.valueOf(level));

        closingCurtain = true;
        openingCurtain = false;
        curtainFrame = 0;
        curtainFrameTmr = curtainFrameTime;

        ((TankActivity)context).gameOverTxt.setVisibility(View.INVISIBLE);
        ((TankActivity)context).gameOverTxt.setY(HEIGHT);
        gameover = false;
        stageComplete = false;
        ((TankActivity)context).enableControls();

        resetScoreView();

        if(P1.lives > 0) {
            P1.respawn();
            P1.resetKills();
            P1.clearBullets();
            P1.bId = 0;
            P1.stageScore = 0;
        }
        showingScore = false;
        round_started = true;
        if(((TankActivity)context).mInterstitialAd == null) {
            ((TankActivity) context).loadInterstitialAd();
        }
        Log.d("NXT ROUND GOLD LEVEL", String.valueOf(TankView.GOLD_LEVEL) + level);
        if(TankView.GOLD_LEVEL < level) {
            Log.d("NXT ROUND", "New gold available");
            gold.setPosition(TankView.WIDTH / 2, TankView.HEIGHT / 2);
            gold.setAvailable(true);
        }
        else {
            gold.setAvailable(false);
            Log.d("NXT ROUND", "New gold not available");
        }
    }

    /**
     * The main loop. Call this to update the game state.
     */
    public void update() {
        if(getHeight() == 0 || getWidth() == 0) {
            mRedrawHandler.sleep(1000 / FPS);
            return;
        }

        if(!mInitialized) {
            initializeGame();
            mInitialized = true;
        }

        long now = System.currentTimeMillis();
        if(gameRunning() && mCurrentState != TankView.State.Stopped) {
            if(now - mLastFrame >= 1000 / FPS) {
                if(mNewRound) {
                    nextRound();
                    mNewRound = false;
                }
                doGameLogic();
            }
        }

        // We will take this much time off of the next update() call to normalize for
        // CPU time used updating the game state.

        if(mContinue) {
            long diff = System.currentTimeMillis() - now;
            mRedrawHandler.sleep(Math.max(0, (1000 / FPS) - diff) );
        }
    }

    public void update(boolean start) {
        this.started = start;
        mContinue = true;
        update();
    }

    private void displayGameOver() {
        ((TankActivity)context).gameOverTxt.setVisibility(View.VISIBLE);
        ((TankActivity)context).gameOverTxt.animate().setDuration(2000);
        ((TankActivity)context).gameOverTxt.animate().y(HEIGHT/2);
    }

    private void resetScoreView() {
        ((TankActivity)context).p1Score.setText(String.valueOf(0));

        ((TankActivity)context).p1AScore.setText(String.valueOf(0));
        ((TankActivity)context).p1BScore.setText(String.valueOf(0));
        ((TankActivity)context).p1CScore.setText(String.valueOf(0));
        ((TankActivity)context).p1DScore.setText(String.valueOf(0));

        ((TankActivity)context).p1ACount.setText(String.valueOf(0));
        ((TankActivity)context).p1BCount.setText(String.valueOf(0));
        ((TankActivity)context).p1CCount.setText(String.valueOf(0));
        ((TankActivity)context).p1DCount.setText(String.valueOf(0));
        ((TankActivity)context).p1Count.setText(String.valueOf(0));

        ((TankActivity)context).p2Score.setText(String.valueOf(0));

        ((TankActivity)context).p2AScore.setText(String.valueOf(0));
        ((TankActivity)context).p2BScore.setText(String.valueOf(0));
        ((TankActivity)context).p2CScore.setText(String.valueOf(0));
        ((TankActivity)context).p2DScore.setText(String.valueOf(0));

        ((TankActivity)context).p2ACount.setText(String.valueOf(0));
        ((TankActivity)context).p2BCount.setText(String.valueOf(0));
        ((TankActivity)context).p2CCount.setText(String.valueOf(0));
        ((TankActivity)context).p2DCount.setText(String.valueOf(0));
        ((TankActivity)context).p2Count.setText(String.valueOf(0));

        scoreFrame = 0;


    }

    private void showScores() {
        if(enemyFrame >= 5 && stageComplete) {
//            if(--newStageTmr <= 0){
//                mNewRound = true;
//                newStageTmr = (int) (2 * TO_SEC);
//            }
            return;
        }
        if(!showingScore) {
            int retries = ((TankActivity)context).settings.getInt(TankActivity.RETRY_COUNT,3);
//            if(retries <= 0 && gameover) {
//                ((TankActivity)context).nxtBtn.setEnabled(false);
//                ((TankActivity)context).nxtBtn.setVisibility(View.INVISIBLE);
//            }
//            else {
//                ((TankActivity)context).nxtBtn.setEnabled(true);
//                ((TankActivity)context).nxtBtn.setVisibility(View.VISIBLE);
//            }
            ((TankActivity) context).retryCount.setText(String.valueOf(retries));
            ((TankActivity) context).p1Score.setText(String.valueOf(P1.totalScore += P1.stageScore));
            ((TankActivity) context).stageScore.setText(String.valueOf(level));
            ((TankActivity) context).scoreView.setVisibility(View.VISIBLE);
            showingScore = true;
            scoreFrameTmr = scoreFrameTime;
            enemyFrame = 0;
            Log.d("SCORES", "Showing scores");

        }

        if(scoreFrameTmr <= 0) {
            int p1killCount, p2killCount;

            switch (enemyFrame) {
                case 0:
                    p1killCount = Integer.parseInt((String) ((TankActivity) context).p1ACount.getText());

//                    if(twoPlayers) {
//                        p2killCount = Integer.parseInt((String) ((TankActivity) context).p1ACount.getText());
//                    }
                    if (p1killCount < P1.kills[enemyFrame]) {
                        p1killCount++;
                        ((TankActivity) context).p1AScore.setText(String.valueOf(p1killCount*100));
                        ((TankActivity) context).p1ACount.setText(String.valueOf(p1killCount));
                        SoundManager.playSound(Sounds.TANK.SCORE);
                    }
//                    else if (twoPlayers && p1killCount < P1.kills[enemyFrame]) {
//                        p1killCount++;
//                        ((TankActivity) context).p1AScore.setText(String.valueOf(p1killCount*100));
//                        ((TankActivity) context).p1ACount.setText(String.valueOf(p1killCount));
//                        SoundManager.playSound(Sounds.TANK.SCORE);
//                    }
                    else {
                        enemyFrame++;
                    }
                    break;
                case 1:
                    p1killCount = Integer.parseInt((String) ((TankActivity) context).p1BCount.getText());
                    if (p1killCount < P1.kills[enemyFrame]) {
                        p1killCount++;
                        ((TankActivity) context).p1BScore.setText(String.valueOf(p1killCount*200));
                        ((TankActivity) context).p1BCount.setText(String.valueOf(p1killCount));
                        SoundManager.playSound(Sounds.TANK.SCORE);
                    } else {
                        enemyFrame++;
                    }
                    break;
                case 2:
                    p1killCount = Integer.parseInt((String) ((TankActivity) context).p1CCount.getText());
                    if (p1killCount < P1.kills[enemyFrame]) {
                        p1killCount++;
                        ((TankActivity) context).p1CScore.setText(String.valueOf(p1killCount*300));
                        ((TankActivity) context).p1CCount.setText(String.valueOf(p1killCount));
                        SoundManager.playSound(Sounds.TANK.SCORE);
                    } else {
                        enemyFrame++;
                    }
                    break;
                case 3:
                    p1killCount = Integer.parseInt((String) ((TankActivity) context).p1DCount.getText());
                    if (p1killCount < P1.kills[enemyFrame]) {
                        p1killCount++;
                        ((TankActivity) context).p1DScore.setText(String.valueOf(p1killCount*400));
                        ((TankActivity) context).p1DCount.setText(String.valueOf(p1killCount));
                        SoundManager.playSound(Sounds.TANK.SCORE);
                    } else {
                        enemyFrame++;
                    }
                    break;

                case 4:

                    ((TankActivity) context).p1Count.setText(String.valueOf(P1.totalKills));
                    SoundManager.playSound(Sounds.TANK.PAUSE);
                    enemyFrame++;
                    break;
            }
            scoreFrameTmr = scoreFrameTime;
        }
        else {
            --scoreFrameTmr;
        }
    }

    private void saveNewStage(int level){
        SharedPreferences settings = context.getSharedPreferences("TankSettings", 0);
        int oldLevel = settings.getInt(TankMenuActivity.PREF_LEVEL,1);
        if(oldLevel < level) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(TankMenuActivity.PREF_LEVEL, level);
            editor.apply();
        }
    }

    private void clearLevel() {
        levelObjects = null;
    }


    private void moveCurtain() {
        if(curtainPause && --curtainPauseTmr > 0) {
            if(curtainPauseTmr == 1) {
                ((TankActivity)context).curtainTxt.setVisibility(View.INVISIBLE);
            }
            return;
        }
        if(movingCurting && (closingCurtain || openingCurtain)){
            if(curtainFrame < 13){
                if (curtainFrameTmr <= 0) {
                    curtainFrame++;
                    curtainFrameTmr = curtainFrameTime;
                }
                else {
                    curtainFrameTmr--;
                }
            }
        }
    }

    private void getEnemyCountView() {
        enemyCount = new ArrayList<>();
        int Rows = ((TankActivity)context).enemyCount.getChildCount();
        for(int row = 0; row < Rows; row++) {
            LinearLayout current_row =  (LinearLayout)((TankActivity)context).enemyCount.getChildAt(row);
            int Cols = current_row.getChildCount();
            ArrayList<ImageView> colCount = new ArrayList<>();
            for(int col = 0; col < Cols; col++) {
                ImageView card = (ImageView) current_row.getChildAt(col);
                card.setBackground(eCountImg);
                colCount.add(card);
            }
            enemyCount.add(colCount);
        }
    }


    public boolean isStarted(){
        return started;
    }



    private void checkCollisionTwoTanks(Player t1, Player t2) {
        t1.collidesWithObject(t2);
    }

    private void checkCollisionPlayer(Player p) {
        for(Enemy e:Enemies){
            if(!e.isDestroyed() && p.collidesWithObject(e)) {
                return;
            }
        }
        if(twoPlayers) {
            if(P1.collidesWithObject(P2)) {
                return;
            }
        }
        if(!p.collidesWithObject(eagle)) {
            boolean stop = false;
            for(int i = 0; i < levelObjects.size(); i++){
                for(int j = 0; j < levelObjects.get(i).size(); j++){
                    if(levelObjects.get(i).get(j) != null & (levelObjects.get(i).get(j) instanceof Brick || levelObjects.get(i).get(j) instanceof StoneWall || (levelObjects.get(i).get(j) instanceof Water && !p.hasBoat()))) {

                        if(p.collidesWithObject(levelObjects.get(i).get(j))) {
                            stop = true;
                            p.stopSlip();
                            break;
                        }
                    }
                    else if(levelObjects.get(i).get(j) instanceof Ice && p.collidesWithObject(levelObjects.get(i).get(j))) {
                        p.iceSlippage();
                    }
                }
                if(stop){
                    break;
                }
            }
        }
    }

    private void checkCollisionEnemyWithPlayer(Tank p, ArrayList<Enemy> enemies) {
        for(Tank e:enemies){
            if(e.collidesWithObject(p)) {
                continue;
            }
            else{
                e.collidesWithObject(eagle);
            }
        }

    }

    private void checkCollisionEnemy() {
        for(int e1 = 0; e1 < Enemies.size(); e1++) {
            if(Enemies.get(e1).collidesWithObject(P1)) {
                continue;
            }
            else if(twoPlayers && Enemies.get(e1).collidesWithObject(P2)) {
                continue;
            }
            else if(Enemies.get(e1).collidesWithObject(eagle)) {
                continue;
            }
            else {
                for (int e2 = 0; e2 < Enemies.size(); e2++) {
//                    if (e1 == e2) {
//                        continue;
//                    }
                    if (e1!=e2 && Enemies.get(e1).collidesWithObject(Enemies.get(e2))) {
                        break;
                    }
                    else {
                        boolean stop = false;
                        for(int i = 0; i < levelObjects.size(); i++){
                            for(int j = 0; j < levelObjects.get(i).size(); j++){
                                if(levelObjects.get(i).get(j) != null && (levelObjects.get(i).get(j) instanceof Brick || levelObjects.get(i).get(j) instanceof StoneWall || (levelObjects.get(i).get(j) instanceof Water && !enemyBoat))) {
                                    if(Enemies.get(e1).collidesWithObject(levelObjects.get(i).get(j))) {
                                        stop = true;
                                        break;
                                    }
                                }
                            }
                            if(stop){
                                break;
                            }
                        }
                    }

                }
            }
//
        }
    }

    private void checkCollisionPlayerBullet(Player p) {
        for(Bullet pbullet:p.getBullets()){
            if(!pbullet.isDestroyed()) {
                for (Enemy e : Enemies) {
                    if(e.collidsWithBullet(pbullet)) {
                        ++p.totalKills;
                        if(e.type == ObjectType.ST_TANK_A) {
                            ++p.kills[0];
                            p.stageScore += 100;
                        }
                        else if(e.type == ObjectType.ST_TANK_B) {
                            ++p.kills[1];
                            p.stageScore += 200;
                        }
                        else if(e.type == ObjectType.ST_TANK_C) {
                            ++p.kills[2];
                            p.stageScore += 300;
                        }
                        else if(e.type == ObjectType.ST_TANK_D) {
                            ++p.kills[3];
                            p.stageScore += 400;
                        }
                    }
                    for(Bullet ebullet:e.getBullets()) {
                        if(!pbullet.isDestroyed() && !ebullet.isDestroyed()) {
                            if (pbullet.collides_with(ebullet)) {
                                pbullet.setDestroyed(false);
                                ebullet.setDestroyed(false);
                            }
                        }
                    }
                }
                boolean stop = false;
                int count = 0;
                for(int i = 0; i < levelObjects.size(); i++){
                    for(int j = 0; j < levelObjects.get(i).size(); j++){
                        if(levelObjects.get(i).get(j) != null && !levelObjects.get(i).get(j).isDestroyed() && pbullet.collides_with(levelObjects.get(i).get(j))) {
                            if(levelObjects.get(i).get(j) instanceof Brick) {
                                boolean coll = ((Brick) levelObjects.get(i).get(j)).collidsWithBullet(pbullet.getDirection());
                                if(coll && twoPlayers) {
                                    switch (pbullet.getDirection()) {
                                        case CONST.Direction.UP:
                                            levelObjectsUpdate.add(new int[]{i,j,6});
                                            break;
                                        case CONST.Direction.DOWN:
                                            levelObjectsUpdate.add(new int[]{i,j,7});
                                            break;
                                        case CONST.Direction.LEFT:
                                            levelObjectsUpdate.add(new int[]{i,j,8});
                                            break;
                                        case CONST.Direction.RIGHT:
                                            levelObjectsUpdate.add(new int[]{i,j,9});
                                            break;
                                    }

                                }
                                if(p.canBreakWall() || !coll) {
                                    levelObjects.get(i).set(j, null);
                                    if(twoPlayers) {
                                        levelObjectsUpdate.add(new int[]{i, j, 0});
                                    }
                                }
                                pbullet.setDestroyed();
                                count++;
                                if(count >= 2){
                                    stop = true;
                                    break;
                                }
                                SoundManager.playSound(Sounds.TANK.BRICK,1,1);
                            }
                            else if(levelObjects.get(i).get(j) instanceof StoneWall) {
                                pbullet.setDestroyed();
                                if(p.canBreakWall()) {
                                    levelObjects.get(i).set(j,null);
                                    if(twoPlayers) {
                                        levelObjectsUpdate.add(new int[]{i, j, 0});
                                    }
                                }
                                count++;
                                if(count >= 2){
                                    stop = true;
                                    break;
                                }
                                if(p.canBreakWall()) {
                                    SoundManager.playSound(Sounds.TANK.BRICK, 1, 1);
                                }
                                else{
                                    SoundManager.playSound(Sounds.TANK.STEEL, 1, 1);
                                }
                            }
                        }
                    }
                    if(stop){
                        break;
                    }
                }
                count = 0;
                for(int i = 0; i < levelBushes.size(); i++) {
                    if(p.canClearBush() && levelBushes.get(i) != null && pbullet.collides_with(levelBushes.get(i))) {
                        levelBushes.set(i,null);
                        if(twoPlayers) {
                            levelBushesUpdate.add(i);
                        }
                        count++;
                        if(count >= 2){
                            break;
                        }
                        SoundManager.playSound(Sounds.TANK.BRICK,1,1);
                    }
                }
                if(!eagle.isDestroyed()) {
                    eagle.collidesWithBullet(pbullet);
                }
            }
        }
    }

    private void checkCollisionEnemyBulletWithPlayer(Player p) {
            for(Enemy e:Enemies) {
                for (Bullet bullet : e.getBullets()) {
                    if (bullet != null && !bullet.isDestroyed() && p.collidsWithBullet(bullet)) {
                        return;
                    }
                }
            }
    }

    private void checkCollisionEnemyBullet(Player p) {
        for(Enemy e:Enemies) {
            for(Bullet bullet: e.getBullets()) {
                if(!bullet.isDestroyed()) {
                    p.collidsWithBullet(bullet);


                    boolean stop = false;
                    int count = 0;
                    for(int i = 0; i < levelObjects.size(); i++){
                        for(int j = 0; j < levelObjects.get(i).size(); j++){
                            if(levelObjects.get(i).get(j) != null && !levelObjects.get(i).get(j).isDestroyed() && bullet.collides_with(levelObjects.get(i).get(j))) {
                                if(levelObjects.get(i).get(j) instanceof Brick) {
                                    boolean coll = ((Brick) levelObjects.get(i).get(j)).collidsWithBullet(bullet.getDirection());
                                    if(coll && twoPlayers) {
                                        switch (bullet.getDirection()) {
                                            case CONST.Direction.UP:
                                                levelObjectsUpdate.add(new int[]{i,j,6});
                                                break;
                                            case CONST.Direction.DOWN:
                                                levelObjectsUpdate.add(new int[]{i,j,7});
                                                break;
                                            case CONST.Direction.LEFT:
                                                levelObjectsUpdate.add(new int[]{i,j,8});
                                                break;
                                            case CONST.Direction.RIGHT:
                                                levelObjectsUpdate.add(new int[]{i,j,9});
                                                break;
                                        }

                                    }
                                    if(e.canBreakWall() || !coll) {
                                        levelObjects.get(i).set(j, null);
                                        if(twoPlayers) {
                                            levelObjectsUpdate.add(new int[]{i, j, 0});
                                        }
                                    }
                                    bullet.setDestroyed();
                                    count++;
                                    if(count >= 2){
                                        stop = true;
                                        break;
                                    }
//                                    SoundManager.playSound(Sounds.TANK.BRICK,1,1);
                                }
                                else if(levelObjects.get(i).get(j) instanceof StoneWall) {
                                    bullet.setDestroyed();
                                    if(e.canBreakWall()) {
                                        levelObjects.get(i).set(j,null);
                                        if(twoPlayers){
                                            levelObjectsUpdate.add(new int[]{i, j, 0});
                                        }
                                    }
                                    count++;
                                    if(count >= 2){
                                        stop = true;
                                        break;
                                    }
//                                    if(e.canBreakWall()) {
//                                        SoundManager.playSound(Sounds.TANK.BRICK, 1, 1);
//                                    }
//                                    else{
//                                        SoundManager.playSound(Sounds.TANK.STEEL, 1, 1);
//                                    }
                                }
                            }
                        }
                        if(stop){
                            break;
                        }
                    }
                    count = 0;
                    for(int i = 0; i < levelBushes.size(); i++) {
                        if(e.canClearBush() && levelBushes.get(i) != null && bullet.collides_with(levelBushes.get(i))) {
                            levelBushes.set(i,null);
                            count++;
                            if(count >= 2){
                                break;
                            }
//                            SoundManager.playSound(Sounds.TANK.BRICK,1,1);
                        }
                    }




                    if(!eagle.isDestroyed()) {
                        eagle.collidesWithBullet(bullet);
                    }



                }

            }
        }
    }

    private void checkCollisionPlayerWithBonus(Player p, Bonus b) {
        int resp = p.collidsWithBonus(b);
        switch(resp) {
            case 0:
                for(Enemy e:Enemies) {
                    e.setDestroyed();
                }
                break;
            case 2:
                p.setFreeze();
                break;
            case 3:
                protectEagle = true;
                eagle.protection = 1;
                protectEagleTmr = ProtectEagleTime;
                for (int[] eaglePo : eaglePos) {
                    levelObjects.get(eaglePo[1]).set(eaglePo[0], new StoneWall(eaglePo[0], eaglePo[1]));
                }
                break;
        }
    }


    private void checkCollisionEnemyWithBonus(Bonus b) {
        if(!TankView.ENEMY_BOOST) {
            return;
        }
        for(Enemy e: Enemies) {
            int resp = e.collidsWithBonus(b);
            switch(resp) {
                case 0:
                    P1.setDestroyed();
                    break;
                case 2:
                    P1.freeze();
                    break;
                case 3:
                    protectEagle = true;
                    eagle.protection = 1;
                    protectEagleTmr = ProtectEagleTime;
                    for (int[] eaglePo : eaglePos) {
                        levelObjects.get(eaglePo[1]).set(eaglePo[0], null);
                    }
                    break;
            }
        }

    }

    private void checkCollisionPlayerWithGold() {
        if(gold.isAvailable() && P1.collides_with(gold)) {
            P1.stageScore += 800;
            ((TankActivity)context).updateGold(1);
            gold.setTaken();
            TankView.GOLD_LEVEL = level;
            ((TankActivity)context).saveInt(TankActivity.GOLD_LEVEL,level);
        }
    }

    public void updateP1Lives(int life) {
        P1.lives += life;
    }

    private void doGameLogic(){
//        if(twoPlayers && !playerReady && !WifiDirectManager.getInstance().isServer()) {
//            waitPlayer();
//            return;
//        }
//        if(notifyPause) {
//            notifyPause = false;
//            pause();
//        }
        if(notifyEndGame) {
            notifyEndGame = false;
            pauseNoAds();
            TankView.EVENT = TankView.END_GAME;
            if(((TankActivity)context).mInterstitialAd == null) {
                ((TankActivity) context).loadInterstitialAd();
            }
            ((TankActivity) context).showInterstitialAd();
            ((TankActivity)context).endGame();
        }

        if(((Enemy.lives <= 0 && Enemies.size() <= 0) || notifyStageComplete) && !stageComplete) {
            pauseNoAds();
            TankView.EVENT = TankView.STAGE_COMPLETE;
            if(((TankActivity)context).mInterstitialAd == null) {
                ((TankActivity) context).loadInterstitialAd();
            }
            ((TankActivity) context).showInterstitialAd();
            doStageComplete();
        }
        else if((((!twoPlayers && P1.lives <= 0) || (twoPlayers && P1.lives <= 0 && P2.lives <= 0)) || (eagle != null && eagle.isDestroyed()) || notifyGameOver) && !gameover) {

            if(eagle != null && eagle.isDestroyed()) {
                pauseNoAds();
                TankView.EVENT = TankView.GAME_OVER;
                if (((TankActivity) context).mInterstitialAd == null) {
                    ((TankActivity) context).loadInterstitialAd();
                }
                ((TankActivity) context).showInterstitialAd();
                doGameOver();
            }

            else {
                if ( CHECKING_RETRY== 0) {
                    pauseNoAds();
                    doCheckRetry();
                } else if (CHECKING_RETRY == 2 || CHECKING_RETRY == 3) {

                    //TODO
                    // Increment life and respawn
//                    if(CHECKING_RETRY == 2) {
//                        P1.lives = 1;
//                    }
//                    else {
//                        P1.lives = 2;
//                    }
                    P1.respawn();
                    CHECKING_RETRY = 0;

                } else if (CHECKING_RETRY == 4) {
                    // Did not get new life
                    CHECKING_RETRY = 0;
                    pauseNoAds();
                    TankView.EVENT = TankView.GAME_OVER;
                    if (((TankActivity) context).mInterstitialAd == null) {
                        ((TankActivity) context).loadInterstitialAd();
                    }
                    ((TankActivity) context).showInterstitialAd();
                    doGameOver();
                }
            }

        }

        if((gameover || stageComplete) && showScoreTmr <= 0) {
            showScores();
        }
        else if ((gameover || stageComplete) && !showingScore) {
            --showScoreTmr;
        }
        if(showingScore) {
            return;
        }
//        checkCollisionTwoTanks(P1,P2);
        if(drawStarted && !startSound) {
            SoundManager.playSound(Sounds.TANK.GAMESTART,1,3);
            Log.d("SOUND", "Played start sound");
            startSound = true;
        }

        sendToWifi();
        moveCurtain();
        if(movingCurting && curtainFrame >= 13) {
            closingCurtain = false;
            openingCurtain = true;
            curtainFrame = 0;
            curtainPauseTmr = curtainPauseTime;
            curtainPause = true;
            ((TankActivity)context).curtainTxt.setText(new StringBuilder().append("STAGE ").append(level).toString());
            ((TankActivity)context).curtainTxt.setVisibility(View.VISIBLE);
        }
        if(movingCurting && curtainFrame >= 12 && openingCurtain) {
            movingCurting = false;
        }

        if(freeze && freezeTmr > 0) {
            --freezeTmr;
        }
        else {
            freeze = false;
        }


        if(protectEagle && protectEagleTmr > 0) {
            --protectEagleTmr;
        }
        else if(protectEagle){
            for (int[] eaglePo : eaglePos) {
                levelObjects.get(eaglePo[1]).set(eaglePo[0], new Brick(eaglePo[0], eaglePo[1]));
            }
            protectEagle = false;
            eagle.protection = 2;
        }

        if(twoPlayers) {
            levelObjectsUpdate.clear();
            levelBushesUpdate.clear();
        }

        if(!twoPlayers || WifiDirectManager.getInstance().isServer()) {
            for (int i = 0; i < Enemies.size(); i++) {
                if (Enemies.get(i).recycle) {
                    Enemies.set(i, null);
                }
            }
            // Remove all recycled enemy
            while (Enemies.remove(null)) ;

            generateEnemy();

//            checkCollisionPlayer(P1);
            checkCollisionPlayerBullet(P1);
            checkCollisionPlayerWithBonus(P1, bonus);
            checkCollisionEnemyWithBonus(bonus);
            checkCollisionEnemyBullet(P1);
            if(twoPlayers){
                checkCollisionEnemyBulletWithPlayer(P2);
            }

        }

        if(twoPlayers && !WifiDirectManager.getInstance().isServer()) {
            checkCollisionPlayerBullet(P1);
            checkCollisionPlayerWithBonus(P1, bonus);
            checkCollisionEnemyBulletWithPlayer(P1);
        }
            checkCollisionPlayer(P1);
            checkCollisionPlayerWithGold();
            P1.update();

        if(!twoPlayers || WifiDirectManager.getInstance().isServer()) {
            // Get target
            int min_dist = TankView.HEIGHT + TankView.WIDTH;
            Rect pRect = P1.getRect();
            Point targ = new Point();
            for (Enemy e : Enemies) {

                if (e.type == ObjectType.ST_TANK_A || e.type == ObjectType.ST_TANK_D) {
                    int dx = (int) (e.x - P1.x);
                    int dy = (int) (e.y - P1.y);
                    int dist = dx + dy;
                    if (dist < min_dist) {
                        min_dist = dist;
                        targ.x = (int) (P1.x + P1.w / 2);
                        targ.y = (int) (P1.y + P1.h / 2);
                    }
                }
                int dx = e.getRect().left - eagle.getRect().left;
                int dy = e.getRect().top - eagle.getRect().top;
                int dist = dx + dy;
                if (dist < min_dist) {
                    min_dist = dist;
                    targ.x = (int) (eagle.x + eagle.w / 2);
                    targ.y = (int) (eagle.y + eagle.h / 2);
                }
                e.setTarget(targ);
            }

            for (Enemy e : Enemies) {
                e.changeDirection();
            }

            checkCollisionEnemy();

            for (Enemy e : Enemies) {
                e.update();
            }
        }
    }

    public void doCheckRetry() {
        if(CHECKING_RETRY == 0) {
            CHECKING_RETRY = 1;
            TankEndGameDialog wd = new TankEndGameDialog((TankActivity) context, this);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(wd.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            wd.show();
            wd.getWindow().setAttributes(lp);
        }
    }

    public void doGameOver() {
        ((TankActivity) context).nxtBtn.setText(R.string.retryTxt);
        P1.stopShooting();
        P1.stopMoving();
        gameover = true;
        enemyFrame = 0;
        ((TankActivity)context).disableControls();
        SoundManager.playSound(Sounds.TANK.GAMEOVER);
        displayGameOver();
        showScoreTmr = showScoreDelay;
        sendPlayerInfo(GAME_OVER);
    }

    public void doStageComplete() {
        ((TankActivity) context).nxtBtn.setText(R.string.nextTxt);
        P1.stopShooting();
        P1.stopMoving();
        stageComplete = true;
        enemyFrame = 0;
        showScoreTmr = showScoreDelay;
        sendPlayerInfo(STAGE_COMPLETE);
        level++;
        saveNewStage(level);
    }

    private void getRemoteUpdate() {
        if(!round_started) {
            return;
        }
//        if(twoPlayers && !gameModel.isEmpty()){
//        if(gameModel.isEmpty()){
//            return;
//        }
        Game m = null;
        try {
            m = gameModel.poll();
        }
        catch (Exception e){
            Log.d("Model", "Model exception");
            return;
        }

        if(!(m instanceof  TankGameModel)) {
            return;
        }
        TankGameModel model = (TankGameModel)m;
        playerReady = true;

        if(!eagle.destroyed){
            eagle.destroyed = model.eagleDestroyed;
        }

        if(model.gameOver){
            notifyGameOver = true;
            return;
        }
        else if(model.stageComplete) {
            notifyStageComplete = true;
            return;
        }
        else if(model.pause) {
            pause();
            return;
        }

        else if(model.end_game) {
            notifyEndGame = true;
            return;
        }

        Log.d("P2 ==> ", model.mPlayer.x + " " + model.mPlayer.y + " " + model.mPlayer.dirction);
        float scale = (float) TankView.this.getHeight() / model.height;

        P2.setModel(model.mPlayer, scale);


        if (!WifiDirectManager.getInstance().isServer()) {
            for (int i = 0; i < Enemies.size(); i++) {
                if (Enemies.get(i).recycle) {
                    Enemies.set(i, null);
                }
            }
            while (Enemies.remove(null)) ;
            boolean found;
            for (int i = 0; i < model.mEnemies.size(); i++) {
                found = false;
                for (int j = 0; j < Enemies.size(); j++) {
                    if(model.mEnemies.get(i).id == Enemies.get(j).id){
                        if(!Enemies.get(j).isDestroyed()) {
                            Enemies.get(j).setModel(model.mEnemies.get(i), scale,false);
                        }
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    Enemy e = new Enemy(ObjectType.ST_TANK_A,model.mEnemies.get(i).group, 0, 0);
                    e.setModel(model.mEnemies.get(i), scale, false);
                    Enemies.add(e);
                }
            }


        }

        if (WifiDirectManager.getInstance().isServer()) {

            for (int i = 0; i < model.mEnemies.size(); i++) {
                for (int j = 0; j < Enemies.size(); j++) {
                    if(model.mEnemies.get(i).id == Enemies.get(j).id){
                        if(!Enemies.get(j).isDestroyed()) {
                            Enemies.get(j).setModel(model.mEnemies.get(i), scale, true);
                        }
                        break;
                    }
                }
            }

            if(model.mPlayer.gotBonus == Bonus.CLOCK) {
                TankView.freeze = true;
                TankView.freezeTmr = TankView.FreezeTime;
            }
//            else if(model.mPlayer.gotBonus == Bonus.GRENADE) {
//
//            }


        }

        for(int[] l:model.lObjects) {

            if(l[2] == 0) {
                levelObjects.get(l[0]).set(l[1],null);
            }
            else if(levelObjects.get(l[0]).get(l[1]) != null) {
                if (l[2] == 6) {
                    ((Brick) levelObjects.get(l[0]).get(l[1])).collidsWithBullet(CONST.Direction.UP);
                } else if (l[2] == 7) {
                    ((Brick) levelObjects.get(l[0]).get(l[1])).collidsWithBullet(CONST.Direction.DOWN);
                } else if (l[2] == 8) {
                    ((Brick) levelObjects.get(l[0]).get(l[1])).collidsWithBullet(CONST.Direction.LEFT);
                } else if (l[2] == 9) {
                    ((Brick) levelObjects.get(l[0]).get(l[1])).collidsWithBullet(CONST.Direction.RIGHT);
                }
            }
        }

        for(int i:model.lBushes) {
            levelBushes.set(i,null);
        }

        if(model.eagleProtection == 1) {
            for (int[] eaglePo : eaglePos) {
                levelObjects.get(eaglePo[1]).set(eaglePo[0], new StoneWall(eaglePo[0], eaglePo[1]));
            }
        }
        else if(model.eagleProtection == 2) {
            for (int[] eaglePo : eaglePos) {
                levelObjects.get(eaglePo[1]).set(eaglePo[0], new Brick(eaglePo[0], eaglePo[1]));
            }
        }

        bonus.setBonus((int)(model.bonus[0]*scale),(int)(model.bonus[1]*scale),model.bonus[2],model.bnsAv,model.bnsClr,model.bonus[3]);

    }

    public void sendToWifi() {
        if(twoPlayers) {
            TankGameModel model = new TankGameModel();
//            if(WifiDirectManager.getInstance().isServer()) {
                model.loadEnemies(Enemies);
                model.loadLevelObjects(levelObjectsUpdate);
            model.loadLevelBushes(levelBushesUpdate);
//            }
            model.loadPlayer(P1);
            model.eagleDestroyed = eagle.isDestroyed();
            model.eagleProtection = eagle.protection;
            model.loadBonus(bonus.x, bonus.y, bonus.getBonus(), Bonus.available, Bonus.cleared, Bonus.id);
            WifiDirectManager.getInstance().sendMessage(model);
//            Gson gson = new Gson();
//            WifiDirectManager.getInstance().sendMessage(gson.fromJson(gson.toJson(model), TankGameModel.class));
            eagle.protection = 0;
            Bonus.cleared = false;
            P1.gotBonus = 0;
        }
    }

    public void sendPlayerInfo(int info) {
        TankGameModel model = new TankGameModel();
        switch (info) {
            case STAGE_COMPLETE:
                model.stageComplete = true;
                break;
            case GAME_OVER:
                model.gameOver = true;
                break;
            case PAUSE:
                model.pause = true;
                break;
            case END_GAME:
                model.end_game = true;
                break;
        }
        WifiDirectManager.getInstance().sendMessage(model);
    }

    public void generateEnemy() {
        ++new_enemy_time;
        if(new_enemy_time > genEnemyTime) {
            int group = 1;
            float g1 = groupProb(0.6f,0.1f);
            float g2 = groupProb(0.2f,0.2f);
            float g3 = groupProb(0.15f,0.3f);
            float g4 = groupProb(0.05f,0.4f);

            float g = (float)(Math.random());
            if(g < g1) {
                group = 1;
            }
            else if(g < g1+g2) {
                group = 2;
            }
            else if(g < g1+g2+g3) {
                group = 3;
            }
            else if(g <= g1+g2+g3+g4) {
                group = 4;
            }


            float e = (float)(Math.random());
            int type = (e < 0.2 + LPROB*level)? 3 : (int)(Math.random()*3)%3;
            ObjectType tank_type = ObjectType.ST_TANK_A;
            switch (type) {
                case 0:
                    tank_type = ObjectType.ST_TANK_A;
                    break;
                case 1:
                    tank_type = ObjectType.ST_TANK_B;
                    break;
                case 2:
                    tank_type = ObjectType.ST_TANK_C;
                    break;
                case 3:
                    tank_type = ObjectType.ST_TANK_D;
                    break;
            }

            if(Enemies.size() < MAX_ENEMIES && Enemy.lives > 0) {
                Enemy enemy = new Enemy(tank_type,group, 0, 0);
                int p = (int) (Math.random() * 3) % 3;
//                int px = p * 6*enemy.w;
//                px = (px > TankView.WIDTH - enemy.w) ? (int) (TankView.WIDTH - enemy.w) : px;
                enemy.x = p * 6*enemy.w;
                enemy.y = 0;
                if(Math.random() < 0.2) {
                    enemy.hasBonus = true;
                }
                Enemies.add(enemy);
                --Enemy.lives;
                int row = (int)(Math.ceil((Enemy.lives+1.0)/2));
                int col = (Enemy.lives+1)%2 == 0 ? 1:0;
                TankView.enemyCount.get(row-1).get(col).setBackground(null);
            }
            new_enemy_time = 0;
        }
    }

    private float groupProb(float min, float max) {
        return (min - max)*level/NUM_LEVELS + min;
    }

    public void applyBonus(String tag) {

        if(tag.equals(TankActivity.GRENADE)) {
            for(Enemy e:Enemies) {
                e.setDestroyed();
            }
        }
        else if(tag.equals(TankActivity.CLOCK)) {
            TankView.freeze = true;
            TankView.freezeTmr = TankView.FreezeTime;
        }
        else if(tag.equals(TankActivity.SHOVEL)) {
            protectEagle = true;
            eagle.protection = 1;
            protectEagleTmr = ProtectEagleTime;
            for (int[] eaglePo : eaglePos) {
                levelObjects.get(eaglePo[1]).set(eaglePo[0], new StoneWall(eaglePo[0], eaglePo[1]));
            }
        }
        else if(tag.equals(TankActivity.TANK)) {
            P1.applyTank();
        }
        else if(tag.equals(TankActivity.GUN)) {
            P1.applyGun();
        }
        else if(tag.equals(TankActivity.BOAT)) {
            P1.applyBoat();
        }
        else if(tag.equals(TankActivity.STAR)) {
            P1.applyStar();
        }
        else if(tag.equals(TankActivity.SHIELD)) {
            P1.applyShield();
        }
    }



    @Override
    public void onDraw(Canvas canvas) {

        if(showingScore) {
            return;
        }
        super.onDraw(canvas);

        if(!mInitialized) {
            return;
        }

        drawing = true;
        gold.draw(canvas);
        if(!twoPlayers || (twoPlayers && !updatingRemote)) {
            for (ArrayList<GameObjects> rowsObjs : levelObjects) {
                for (GameObjects obj : rowsObjs) {
                    if (obj != null) {
                        obj.draw(canvas);
                    }
                }
            }
        }

        if(P1 != null) {
            P1.draw(canvas);
        }
        if(P2 != null && !updatingRemote) {
            P2.draw(canvas);
        }

        eagle.draw(canvas);

        if(!twoPlayers || (twoPlayers && !updatingRemote)){
            for (Tank e : Enemies) {
                e.draw(canvas);
            }
        }

        for(Bush bush:levelBushes) {
            if(bush != null) {
                bush.draw(canvas);
            }
        }
        bonus.draw(canvas);

        if(movingCurting) {
            if(openingCurtain) {
                curtainTRect.left = 0;
                curtainTRect.top = 0;
                curtainTRect.right = WIDTH;
                curtainTRect.bottom = tile_dim*(13-curtainFrame);

                curtainBRect.left = 0;
                curtainBRect.top = tile_dim*(12+curtainFrame);
                curtainBRect.right = WIDTH;
                curtainBRect.bottom = HEIGHT;
            }
            else if(closingCurtain) {
                curtainTRect.left = 0;
                curtainTRect.top = 0;
                curtainTRect.right = WIDTH;
                curtainTRect.bottom = tile_dim*(curtainFrame);

                curtainBRect.left = 0;
                curtainBRect.top = tile_dim*(26-curtainFrame);
                curtainBRect.right = WIDTH;
                curtainBRect.bottom = HEIGHT;
            }
            canvas.drawRect(curtainTRect,curtainPaint);
            canvas.drawRect(curtainBRect,curtainPaint);
        }
        drawStarted = true;
        drawing = false;

    }


    @Override
    public void onMessageReceived(Game message) {
        if(!mInitialized || P2 == null) {
            return;
        }
        gameModel.add(message);
    }

    @Override
    public void onButtonPressed(View v, MotionEvent m) {

            switch (v.getId()) {
                case R.id.upBtn:
                    if(m.getX() < 0) {
                        P1.move(CONST.Direction.LEFT);
                    }
                    else if(m.getX() > ((TankActivity)context).upBtn.getWidth()) {
                        P1.move(CONST.Direction.RIGHT);
                    }
                    else if(m.getY() > ((TankActivity)context).upBtn.getHeight()) {
                        P1.move(CONST.Direction.DOWN);
                    }
                    else {
                        P1.move(CONST.Direction.UP);
                    }
                    break;
                case R.id.downBtn:
                    if(m.getX() < 0) {
                        P1.move(CONST.Direction.LEFT);
                    }
                    else if(m.getX() > ((TankActivity)context).dwnBtn.getWidth()) {
                        P1.move(CONST.Direction.RIGHT);
                    }
                    else if(m.getY() < 0) {
                        P1.move(CONST.Direction.UP);
                    }
                    else {
                        P1.move(CONST.Direction.DOWN);
                    }
                    break;
                case R.id.leftBtn:
                    if(m.getY() < 0) {
                        P1.move(CONST.Direction.UP);
                    }
                    else if(m.getY() > ((TankActivity)context).lftBtn.getHeight()) {
                        P1.move(CONST.Direction.DOWN);
                    }
                    else if(m.getX() > ((TankActivity)context).lftBtn.getWidth()) {
                        P1.move(CONST.Direction.RIGHT);
                    }
                    else {
                        P1.move(CONST.Direction.LEFT);
                    }
                    break;
                case R.id.rightBtn:
                    if(m.getY() < 0) {
                        P1.move(CONST.Direction.UP);
                    }
                    else if(m.getY() > ((TankActivity)context).rtBtn.getHeight()) {
                        P1.move(CONST.Direction.DOWN);
                    }
                    else if(m.getX() < 0) {
                        P1.move(CONST.Direction.LEFT);
                    }
                    else {
                        P1.move(CONST.Direction.RIGHT);
                    }
                    break;
                case R.id.shootBtn:
                    P1.startShooting();
                    break;
            }
//        }
        if(m.getAction() == MotionEvent.ACTION_UP) {
            if(v.getId()== R.id.shootBtn) {
                P1.stopShooting();
            }
            else {
                P1.stopMoving();
            }
        }
        if(m.getAction() == MotionEvent.ACTION_DOWN && v.getId() == R.id.shootBtn) {
            TankView.vibrate();
//            P1.fire();
        }
    }



    /**
     * Reset the lives, paddles and the like for a new game.
     */
    public void newGame() {
    }

    /**
     * This is kind of useless as well.
     */
    private void resumeLastState() {
        if(mLastState == TankView.State.Stopped && mCurrentState == TankView.State.Stopped) {
            mCurrentState = TankView.State.Running;
        }
        else if(mCurrentState != TankView.State.Stopped) {
            // Do nothing
        }
        else if(mLastState != TankView.State.Stopped) {
            mCurrentState = mLastState;
            mLastState = TankView.State.Stopped;
        }
    }

    public boolean gameRunning() {
        // TODO
//        return mInitialized && P2 != null && P1 != null
//                && P2.isAlive() && P1.isAlive();
        return true;
    }

    public void pauseNoAds() {
        mCurrentState = TankView.State.Stopped;
        mLastState = State.Running;
//        SoundManager.pauseSounds();
    }

    public void interrupt() {
        mCurrentState = TankView.State.Stopped;
        mLastState = State.Running;
        ((TankActivity)context).pauseBtn.setText(R.string.continueTxt);
//        SoundManager.pauseSounds();
    }

    public void enablePause(boolean enable) {
        ((TankActivity)context).pauseBtn.setEnabled(enable);
    }

    public void resumeNoAds() {
        mLastState = TankView.State.Stopped;
        mCurrentState = State.Running;
//        SoundManager.resumeSounds();
    }

    public void pause() {

        SoundManager.playSound(Sounds.TANK.PAUSE);

        if (mCurrentState != TankView.State.Stopped) {
            mLastState = mCurrentState;
            mCurrentState = TankView.State.Stopped;
            ((TankActivity)context).disableControls();
            ((TankActivity)context).pauseBtn.setText(R.string.continueTxt);
//            SoundManager.pauseSounds();
        } else {
            mCurrentState = mLastState;
            mLastState = TankView.State.Stopped;
            ((TankActivity)context).enableControls();
            ((TankActivity)context).pauseBtn.setText(R.string.pauseTxt);
//            SoundManager.resumeSounds();
        }


        if(mCurrentState == TankView.State.Stopped) {
            TankView.EVENT = TankView.PAUSE;
            ((TankActivity) context).loadInterstitialAd();
            ((TankActivity) context).showInterstitialAd();
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void setPlayerControl(boolean twoPlayers) {
        this.twoPlayers = twoPlayers;
    }

    public static void vibrate() {
        if(!mVibrate) {
            return;
        }
        // Vibrate for 100 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            mVibrator.vibrate(100);
        }
    }

    public static void vibrate(int time) {
        if(!mVibrate) {
            return;
        }
        // Vibrate for time milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            mVibrator.vibrate(time);
        }
    }

    public void resume() {
        mContinue = true;
        update();
    }

    public void stop() {
        mContinue = false;
    }

    /**
     * Release all resource locks.
     */
    public void release() {
        SoundManager.cleanup();
        showingScore = false;
        mInitialized = false;

        P2 = null;
        P1 = null;
        eagle = null;
        tankBitmap = null;
        tankSprite = null;
        eCountImg = null;
        eCountBm = null;
        Enemies = null;
        levelObjects = null;
        levelBushes = null;
        bonus = null;
        gameModel = null;
        level = 0;

    }

//    public void toggleMuted() {
//        this.setMuted(!mSound);
//    }

//    public void setMuted(boolean b) {
//        // Set the in-memory flag
//        mSound = b;
//
//        // Grab a preference editor
//        Context ctx = this.getContext();
//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
//        SharedPreferences.Editor editor = settings.edit();
//
//        // Save the value
//        editor.putBoolean(Pong.PREF_MUTED, b);
//        editor.apply();
//
//        // TODO
//        // Output a toast to the user
////        int rid = (mMuted) ? R.string.sound_disabled : R.string.sound_enabled;
////        Toast.makeText(ctx, rid, Toast.LENGTH_SHORT).show();
//    }
}
