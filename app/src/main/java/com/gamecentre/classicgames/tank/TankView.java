package com.gamecentre.classicgames.tank;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gamecentre.classicgames.model.Game;
import com.gamecentre.classicgames.model.TankGameModel;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;
import com.gamecentre.classicgames.utils.ButtonListener;
import com.gamecentre.classicgames.pingpong.InputHandler;
import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.pingpong.Pong;
import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.utils.RemoteMessageListener;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


public class TankView extends View implements View.OnTouchListener, View.OnClickListener, RemoteMessageListener, ButtonListener {

    /** Debug tag */
    @SuppressWarnings("unused")
    private static final String TAG = "PongView";
    protected static final int FPS = 20;
    public static final float TO_SEC = 1000f/FPS;
    public static final int
            STARTING_LIVES = 1,
            TANK_SPEED = 20;

    public static int STARTING_BULLETS = 3;

    /**
     * This is mostly deprecated but kept around if the need
     * to add more game states comes around.
     */
    private TankView.State mCurrentState = TankView.State.Running;
    private TankView.State mLastState = TankView.State.Stopped;
    private boolean started = false;
    public Context context;

    public static int WIDTH;
    public static int HEIGHT;
    public static float SCALE;
    public static float RESIZE = 1;
    public static Bitmap graphics;
    private final int NUM_LEVELS = 150;
    private final float LPROB = 0.6f/NUM_LEVELS;
    private int level = 0;
    private boolean enemyBoat = false;

//    private int sW, sH;

    public static ArrayList<ArrayList<ImageView>> enemyCount;




    public static enum State { Running, Stopped}

    /** Flag that marks this view as initialized */
    private boolean mInitialized = false;

    /** Preferences loaded at startup */
    private int mTankSpeedModifier;

    /** Lives modifier */
    private int mLivesModifier;

    /** Starts a new round when set to true */
    private boolean mNewRound = true;

    /** Keeps the game thread alive */
    private boolean mContinue = true;

    /** Mutes sounds when true */
    private boolean mMuted = false;

    private Player P2, P1;
    private Eagle eagle;
    public static ArrayList<ArrayList<Bitmap>> tankBitmap;
    public static Sprite tankSprite;
    public Drawable eCountImg;
    public Bitmap eCountBm;
    private ArrayList<Enemy> Enemies = new ArrayList<>();
    private ArrayList<ArrayList<GameObjects>> levelObjects;
    private ArrayList<Bush> levelBushes;
    public static Bonus bonus;
    private int new_enemy_time = 0;
    private final int genEnemyTime = (int)TO_SEC;
    private final int MAX_ENEMIES = 4;
    private final int[][] eaglePos = {{11,25},{11,24},{11,23},{12,23},{13,23},{14,23},{14,24},{14,25}};

    public static ArrayList<ArrayList<Bitmap>> curtain;

    public static ArrayList<Game> gameModel;


    public static boolean freeze = false;
    public static int freezeTmr = 0;
    public static int FreezeTime = (int)(8*TankView.TO_SEC);

    public static boolean protectEagle = false;
    public static int protectEagleTmr = 0;
    public static int ProtectEagleTime = (int)(20*TankView.TO_SEC);


    private boolean drawStarted = false;
    private boolean startSound = false;

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

    /** Flags indicating who is a player */
    private boolean twoPlayers = false;

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

    /**
     * Creates a new PongView within some context
     * @param context
     * @param attrs
     */
    public TankView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TankView.this.context = context;
        constructView();
    }

    public TankView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        constructView();
    }

    /**
     * Set the paddles to their initial states and as well the ball.
     */
    private void constructView() {

        setFocusable(true);
        MessageRegister.getInstance().setMsgListener(this);
        MessageRegister.getInstance().setButtonListener(this);

        Context ctx = this.getContext();
        SharedPreferences settings = ctx.getSharedPreferences("GameSettings", 0);
//        loadPreferences( PreferenceManager.getDefaultSharedPreferences(ctx) );
        loadPreferences(settings);
        loadSFX();
//        loadGameObjects();

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
        SpriteObjects.getInstance().insert(ObjectType.ST_DESTROY_TANK, 1040, 0, 64, 64, 7, 2, false);//70
        SpriteObjects.getInstance().insert(ObjectType.ST_DESTROY_BULLET, 1108, 0, 32, 32, 5, 1, false); //40
        SpriteObjects.getInstance().insert(ObjectType.ST_BOAT_P1, 944, 96, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_BOAT_P2, 976, 96, 32, 32, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_EAGLE, 944, 0, 32, 32, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_DESTROY_EAGLE, 1040, 0, 64, 64, 7, 2, false); //100
        SpriteObjects.getInstance().insert(ObjectType.ST_FLAG, 944, 64, 16, 16, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_BULLET, 944, 128, 8, 8, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_LEFT_ENEMY, 944, 144, 16, 16, 1, 200, false);
        SpriteObjects.getInstance().insert(ObjectType.ST_STAGE_STATUS, 976, 64, 32, 32, 1, 200, false);

        SpriteObjects.getInstance().insert(ObjectType.ST_TANKS_LOGO, 0, 260, 406, 72, 1, 200, false);



    }

    protected void loadSFX() {
        Context ctx = getContext();
        mWinSFX = mPool.load(ctx, R.raw.win1, 1);
        mMissSFX = mPool.load(ctx, R.raw.miss, 1);
        mPaddleSFX = mPool.load(ctx, R.raw.movecard, 1);
        mWallSFX = mPool.load(ctx, R.raw.wrongmove, 1);
        mShootSFX = mPool.load(ctx, R.raw.shoot, 1);
        mHitSFX = mPool.load(ctx, R.raw.hit, 1);
//        mBrickSFX = mPool.load(ctx, R.raw.brick, 1);


    }

    protected void loadPreferences(SharedPreferences prefs) {
        Context ctx = getContext();
        Resources r = ctx.getResources();

        mTankSpeedModifier = Math.max(0, prefs.getInt(Pong.PREF_BALL_SPEED, 0));
        mMuted = prefs.getBoolean(Pong.PREF_MUTED, mMuted);
        mLivesModifier = Math.max(0, prefs.getInt(Pong.PREF_LIVES, 2));

    }

    private void loadLevel(int level) {
        levelObjects = new ArrayList<>();
        levelBushes = new ArrayList<>();
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
        int dim = (int)(TankView.this.getHeight()*0.98);
        Log.d("LAYOOUT", String.valueOf(dim));
        dim = (int)(dim/13)*13;
        params.width = dim;//(int)(32*13*SCALE);
        params.height = dim;//(int)(32*13*SCALE);
//        TankView.this.setLayoutParams(params);
        TankView.this.layout(0,0,dim,dim);



        WIDTH = TankView.this.getWidth();//*RESIZE;
        HEIGHT = TankView.this.getHeight();//*RESIZE;
        Log.d("VIEW DIM", WIDTH + " " + HEIGHT);
        Log.d("VIEW SCALE", String.valueOf(SCALE));

        graphics = BitmapFactory.decodeResource(context.getResources(), R.drawable.tanktexture);
        Bitmap test = Bitmap.createBitmap(graphics,0,0,32,32);
        int h = test.getHeight();
        RESIZE  = dim/(h*13f);

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
        getEnemyCountView();

        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_FLAG);
        bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y, sprite.w, sprite.h);
        Drawable d = new BitmapDrawable(context.getResources(), bm);
        TankActivity.P1StatusImg.setBackground(d);

        sprite = SpriteObjects.getInstance().getData(ObjectType.ST_STAGE_STATUS);
        bm = Bitmap.createBitmap(TankView.graphics, sprite.x, sprite.y, sprite.w, sprite.h);
        d = new BitmapDrawable(context.getResources(), bm);
        TankActivity.StageFlag.setBackground(d);

//        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
//        curtain = new ArrayList<>();
//        for(int row = 0; row < 26; row++) {
//            ArrayList <Bitmap> colBm = new ArrayList<>();
//            for(int col = 0; col < 26; col++) {
//                bm = Bitmap.createBitmap(new int[]{Color.BLACK},16, 16, conf);
//                colBm.add(bm);
//            }
//            curtain.add(colBm);
//        }

    }

    private void getEnemyCountView() {
        enemyCount = new ArrayList<>();
        int Rows = TankActivity.enemyCount.getChildCount();
        for(int row = 0; row < Rows; row++) {
            LinearLayout current_row =  (LinearLayout)TankActivity.enemyCount.getChildAt(row);
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



    /**
     * Reset the paddles/touchboxes/framespersecond/ballcounter for the next round.
     */
    private void nextRound() {
        drawStarted = false;
        startSound = false;
        if(twoPlayers) {
            gameModel = new ArrayList<>();
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
            Log.d("PLAYERS", "ONE PLAYER");
        }
        bonus = new Bonus();
        level = 17;
        clearLevel();
        loadLevel(level);
        TankActivity.StageTxt.setText(String.valueOf(level));
    }

    private void clearLevel() {
        levelObjects = null;
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

    public boolean isStarted(){
        return started;
    }



    private void checkCollisionTwoTanks(Player t1, Player t2) {
        t1.collidesWithObject(t2);
    }

    private void checkCollisionPlayer(Player p, ArrayList<Enemy> enemies) {
        for(Tank e:enemies){
            if(!e.isDestroyed() && p.collidesWithObject(e)) {
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

    private void checkCollisionEnemy(Player P1, ArrayList<Enemy> enemies) {
        for(int e1 = 0; e1 < enemies.size(); e1++) {
            if(enemies.get(e1).collidesWithObject(P1)) {
                continue;
            }
            else if(enemies.get(e1).collidesWithObject(eagle)) {
                continue;
            }
            else {
                for (int e2 = 0; e2 < enemies.size(); e2++) {
//                    if (e1 == e2) {
//                        continue;
//                    }
                    if (e1!=e2 && enemies.get(e1).collidesWithObject(enemies.get(e2))) {
                        break;
                    }
                    else {
                        boolean stop = false;
                        for(int i = 0; i < levelObjects.size(); i++){
//                        for(ArrayList<GameObjects> rowObjs:levelObjects) {
                            for(int j = 0; j < levelObjects.get(i).size(); j++){
//                            for(GameObjects obj:rowObjs) {
                                if(levelObjects.get(i).get(j) != null && (levelObjects.get(i).get(j) instanceof Brick || levelObjects.get(i).get(j) instanceof StoneWall || (levelObjects.get(i).get(j) instanceof Water && !enemyBoat))) {
                                    if(enemies.get(e1).collidesWithObject(levelObjects.get(i).get(j))) {
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

    private void checkCollisionPlayerBullet(Player p, ArrayList<Enemy> enemies) {
        for(Bullet pbullet:p.getBullets()){
            if(!pbullet.isDestroyed()) {
                for (Enemy e : enemies) {
                    e.collidsWithBullet(pbullet);
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
                                if(p.canBreakWall() || !((Brick) levelObjects.get(i).get(j)).collidsWithBullet(pbullet.getDirection())) {
                                    levelObjects.get(i).set(j, null);
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

    private void checkCollisionEnemyBullet(Tank p, ArrayList<Enemy> enemies) {
        for(Enemy e:enemies) {
            for(Bullet bullet: e.getBullets()) {
                if(!bullet.isDestroyed()) {
                    p.collidsWithBullet(bullet);


                    boolean stop = false;
                    int count = 0;
                    for(int i = 0; i < levelObjects.size(); i++){
                        for(int j = 0; j < levelObjects.get(i).size(); j++){
                            if(levelObjects.get(i).get(j) != null && !levelObjects.get(i).get(j).isDestroyed() && bullet.collides_with(levelObjects.get(i).get(j))) {
                                if(levelObjects.get(i).get(j) instanceof Brick) {
                                    if(e.canBreakWall() || !((Brick) levelObjects.get(i).get(j)).collidsWithBullet(bullet.getDirection())) {
                                        levelObjects.get(i).set(j, null);
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
                for(Tank e:Enemies) {
                    e.setDestroyed();
                }
                break;
            case 2:
                p.setFreeze();
                break;
            case 3:
                protectEagle = true;
                protectEagleTmr = ProtectEagleTime;
                for (int[] eaglePo : eaglePos) {
                    levelObjects.get(eaglePo[1]).set(eaglePo[0], new StoneWall(eaglePo[0], eaglePo[1]));
                }
                break;
        }
    }

    private void checkCollisionEnemyWithBonus(ArrayList<Enemy> enemies, Bonus b) {

    }

    private void doGameLogic(){
//        checkCollisionTwoTanks(P1,P2);
        if(drawStarted && !startSound) {
            SoundManager.playSound(Sounds.TANK.GAMESTART,1,3);
            Log.d("SOUND", "Played start sound");
            startSound = true;
        }

        sendToWifi();

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
        }

        for(int i = 0; i < Enemies.size(); i++) {
            if(Enemies.get(i).recycle) {
                Enemies.set(i,null);
            }
        }

        // Remove all recycled enemy
        while(Enemies.remove(null));

        generateEnemy();

        bonus.setBonus((int)(Math.random()*8));

        checkCollisionPlayer(P1,Enemies);
        checkCollisionPlayerBullet(P1, Enemies);
        checkCollisionPlayerWithBonus(P1,bonus);
        checkCollisionEnemyBullet(P1, Enemies);

        P1.update();

        // Get target
        int min_dist = TankView.HEIGHT + TankView.WIDTH;
        Rect pRect = P1.getRect();
        Point targ = new Point();
        for(Enemy e: Enemies) {

            if(e.type == ObjectType.ST_TANK_A || e.type == ObjectType.ST_TANK_D) {
                int dx = (int)(e.x - P1.x);
                int dy = (int)(e.y - P1.y);
                int dist = dx + dy;
                if(dist < min_dist) {
                    min_dist = dist;
                    targ.x = (int)(P1.x + P1.w/2);
                    targ.y = (int)(P1.y + P1.h/2);
                }
            }
            int dx = e.getRect().left - eagle.getRect().left;
            int dy = e.getRect().top - eagle.getRect().top;
            int dist = dx + dy;
            if(dist < min_dist) {
                min_dist = dist;
                targ.x = (int)(eagle.x + eagle.w/2);
                targ.y = (int)(eagle.y + eagle.h/2);
            }
            e.setTarget(targ);
        }

        checkCollisionEnemy(P1,Enemies);

        for(Enemy e: Enemies) {
            e.update();
        }

//        if(twoPlayers && gameModel.size() > 0) {
//            for(int i = 0; !gameModel.isEmpty(); i++) {
//                 if(gameModel.get(i) != null){
//                    TankGameModel model = (TankGameModel) gameModel.get(i);
//                    Log.d("P2 ==> ", model.mPlayer.x + " " + model.mPlayer.y + " " + model.mPlayer.dirction);
//                    float scale = (float) TankView.this.getHeight() / model.height;
//                    P2.x = (int) (model.mPlayer.x * scale);
//                    P2.y = (int) (model.mPlayer.y * scale);
//                    P2.direction = model.mPlayer.dirction;
//                    gameModel.set(i,null);
//                    break;
//                }
//            }
//            while(gameModel.remove(null));
//
//        }


    }

    public void sendToWifi() {
//        if(WifiDirectManager.getInstance().isServer()) {
        Gson gson = new Gson();
            TankGameModel model = new TankGameModel();
            model.loadEnemies(Enemies);
            model.loadPlayer(P1);
            model.loadLevelObjects(levelObjects);
//        WifiDirectManager.getInstance().sendMessage(model);

            WifiDirectManager.getInstance().sendMessage(gson.fromJson(gson.toJson(model), TankGameModel.class));
//        }
    }

    public void generateEnemy() {
//        level = 0;
        ++new_enemy_time;
        if(new_enemy_time > genEnemyTime) {
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
                Enemy enemy = new Enemy(tank_type, 0, 0);
                int p = (int) (Math.random() * 3) % 3;
//                int px = p * 6*enemy.w;
//                px = (px > TankView.WIDTH - enemy.w) ? (int) (TankView.WIDTH - enemy.w) : px;
                enemy.x = p * 6*enemy.w;
                enemy.y = 0;
                Enemies.add(enemy);
                --Enemy.lives;
                int row = (int)(Math.ceil((Enemy.lives+1.0)/2));
                int col = (Enemy.lives+1)%2 == 0 ? 1:0;
                TankView.enemyCount.get(row-1).get(col).setBackground(null);
            }
            new_enemy_time = 0;
        }
    }



    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(!mInitialized) {
            return;
        }

        for(ArrayList<GameObjects>rowsObjs:levelObjects) {
            for(GameObjects obj:rowsObjs) {
                if(obj != null) {
                    obj.draw(canvas);
                }
            }
        }

        if(P1 != null) {
            P1.draw(canvas);
        }
        if(P2 != null) {
            P2.draw(canvas);
        }

        eagle.draw(canvas);

        for(Tank e:Enemies) {
            e.draw(canvas);
        }

        for(Bush bush:levelBushes) {
            if(bush != null) {
                bush.draw(canvas);
            }
        }
        bonus.draw(canvas);

        drawStarted = true;

    }

    public boolean onTouch(View v, MotionEvent mo) {
//        if(v != this || !gameRunning()) return false;
        Log.d("Touch TAG", v.getTag().toString());
//        Toast.makeText(this, " ", Toast.LENGTH_SHORT).show();

//        if(v != this) return false;

        // We want to support multiple touch and single touch
        InputHandler handle = InputHandler.getInstance();

        // Loop through all the pointers that we detected and
        // process them as normal touch events.
        for(int i = 0; i < handle.getTouchCount(mo); i++) {
            int tx = (int) handle.getX(mo, i);
            int ty = (int) handle.getY(mo, i);

            if(v.getTag() == "shoot") {
                Log.d("Button", "SHOOT");
            }
        }

        Button b = findViewById(R.id.shootBtn);

        return true;
    }

    @Override
    public void onClick(View v) {
        Log.d("Click TAG", v.getTag().toString());
    }

    @Override
    public void onMessageReceived(Game message) {
        if(!mInitialized || P2 == null) {
            return;
        }
//        gameModel.add(message);

        TankGameModel model = (TankGameModel) message;
        Log.d("P2 ==> ", model.mPlayer.x + " " + model.mPlayer.y + " " + model.mPlayer.dirction);
        float scale = (float) TankView.this.getHeight() / model.height;
//        P2.x = (int) (model.mPlayer.x * scale);
//        P2.y = (int) (model.mPlayer.y * scale);
//        P2.direction = model.mPlayer.dirction;
//        P2.setShield(model.mPlayer.shield);
//        P2.setBoat(model.mPlayer.boat);
//        P2.armour = model.mPlayer.armour;
        P2.setModel(model.mPlayer,scale);
    }

    @Override
    public void onButtonPressed(View v, MotionEvent m) {
//        if(m.getAction() == MotionEvent.ACTION_DOWN) {
//        Log.d("Button", String.valueOf(TankActivity.upRect.left) + " " + TankActivity.upRect.top);
//        Log.d("View", String.valueOf(TankActivity.navRect.left) + " " + TankActivity.navRect.top);
//        Log.d("Touch", String.valueOf((int)m.getX() + TankActivity.navRect.left) + " " + (int)m.getY() + TankActivity.navRect.top);
//        if(TankActivity.upRect.contains((int)m.getX(),(int)m.getY())) {
//            P1.move(CONST.Direction.UP);
//
//        }
            switch (v.getId()) {
                case R.id.upBtn:
                    if(m.getX() < 0) {
                        P1.move(CONST.Direction.LEFT);
                    }
                    else if(m.getX() > TankActivity.upBtn.getWidth()) {
                        P1.move(CONST.Direction.RIGHT);
                    }
                    else if(m.getY() > TankActivity.upBtn.getHeight()) {
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
                    else if(m.getX() > TankActivity.dwnBtn.getWidth()) {
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
                    else if(m.getY() > TankActivity.lftBtn.getHeight()) {
                        P1.move(CONST.Direction.DOWN);
                    }
                    else if(m.getX() > TankActivity.lftBtn.getWidth()) {
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
                    else if(m.getY() > TankActivity.rtBtn.getHeight()) {
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
                    P1.starShooting();;
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
        if(v.getId() == R.id.shootBtn) {
            P1.fire();
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

    public void pause() {
        mLastState = mCurrentState;
        mCurrentState = TankView.State.Stopped;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void setPlayerControl(boolean twoPlayers) {
        this.twoPlayers = twoPlayers;
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
    }

    public void toggleMuted() {
        this.setMuted(!mMuted);
    }

    public void setMuted(boolean b) {
        // Set the in-memory flag
        mMuted = b;

        // Grab a preference editor
        Context ctx = this.getContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();

        // Save the value
        editor.putBoolean(Pong.PREF_MUTED, b);
        editor.commit();

        // TODO
        // Output a toast to the user
//        int rid = (mMuted) ? R.string.sound_disabled : R.string.sound_enabled;
//        Toast.makeText(ctx, rid, Toast.LENGTH_SHORT).show();
    }

    private void playSound(int rid) {
        if(mMuted == true) return;
        mPool.play(rid, 0.2f, 0.2f, 1, 0, 1.0f);
    }
}
