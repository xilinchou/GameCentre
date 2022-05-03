package com.gamecentre.classicgames.pingpong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
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
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;

import com.gamecentre.classicgames.model.Game;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.utils.RemoteMessageListener;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

/**
 * This class is the main viewing window for the Pong game. All the game's
 * logic takes place within this class as well.
 * @author OEP
 *
 */
public class PongView extends View implements OnTouchListener, OnKeyListener, RemoteMessageListener {
    /** Debug tag */
    @SuppressWarnings("unused")
    private static final String TAG = "PongView";
    protected static final int FPS = 60;
    public static final int
            STARTING_LIVES = 1,
            PLAYER_PADDLE_SPEED = 20;

    public static int STARTING_BULLETS = 3;

    /**
     * This is mostly deprecated but kept around if the need
     * to add more game states comes around.
     */
    private State mCurrentState = State.Running;
    private State mLastState = State.Stopped;
    private boolean started = false;
    public static enum State { Running, Stopped}

    /** Flag that marks this view as initialized */
    private boolean mInitialized = false;

    /** Preferences loaded at startup */
    private int mBallSpeedModifier;

    /** Lives modifier */
    private int mLivesModifier;

    /** AI Strategy */
    private int mAiStrategy;

    /** CPU handicap */
    private int mCpuHandicap;

    /** Starts a new round when set to true */
    private boolean mNewRound = true;

    /** Keeps the game thread alive */
    private boolean mContinue = true;

    /** Mutes sounds when true */
    private boolean mMuted = false;

    private Paddle P2, P1;

    private  BrickWall brickWall;

    private BlindWall blindWall;

    private Terminator terminator = null;

    protected ArrayList<BaseWall> baseWalls;

    private int init_p1Touch = -1;
    private int init_p2Touch = 0;

    private int init_dest1 = -1;
    private int init_dest2 = 0;

    private final int NUM_STAGES = 9;

    private ArrayList<Integer> stageNumbers;

    private int currentStage = 0;

    /** Touch boxes for various functions. These are assigned in initialize() */
    private Rect mPauseTouchBox;

    /** Timestamp of the last frame created */
    private long mLastFrame = 0;

    protected ArrayList<Ball> mBalls;

    protected ArrayList<Bullet> bullets = new ArrayList<>();

    private int NUM_BALLS = 4;

    /** Random number generator */
    private static final Random RNG = new Random();

    /** Pool for our sound effects */
    protected SoundPool mPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

    protected int mWinSFX, mMissSFX, mPaddleSFX, mWallSFX, mShootSFX, mHitSFX, mBrickSFX;

    /** Paint object */
    private final Paint mPaint = new Paint();

    /** Padding for touch zones and paddles */
    private static final int PADDING = 3;

    /** Scrollwheel sensitivity */
    private static final int SCROLL_SENSITIVITY = 80;

    /** Redraws the screen according to FPS */
    private RefreshHandler mRedrawHandler = new RefreshHandler();

    /** Flags indicating who is a player */
    private boolean mP2_Player = false, mP1_Player = false;

    /**
     * An overloaded class that repaints this view in a separate thread.
     * Calling PongView.update() should initiate the thread.
     * @author OEP
     *
     */
    class RefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            PongView.this.update();
            PongView.this.invalidate(); // Mark the view as 'dirty'
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
    public PongView(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructView();
    }

    public PongView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        constructView();
    }

    /**
     * Set the paddles to their initial states and as well the ball.
     */
    private void constructView() {
        setOnTouchListener(this);
        setOnKeyListener(this);
        setFocusable(true);
        MessageRegister.getInstance().setMsgListener(this);

        Context ctx = this.getContext();
        SharedPreferences settings = ctx.getSharedPreferences("GameSettings", 0);
//        loadPreferences( PreferenceManager.getDefaultSharedPreferences(ctx) );
        loadPreferences(settings);
        loadSFX();

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

    protected void shuffleStages() {
        stageNumbers = new ArrayList<>();
        ArrayList<Integer> stgNum = new ArrayList<>();
        int stages = NUM_STAGES;

        for(int i = 0; i < NUM_STAGES; i++) {
            stgNum.add(i);
        }

        for(int i = 0; i < stages; i++) {
            int stage = (int)(Math.random()*(stgNum.size()-1));
            stageNumbers.add(stgNum.get(stage));
            Log.d("Stages: ", String.valueOf(stageNumbers.get(i)));
            stgNum.remove(stage);
        }
    }

    protected void loadPreferences(SharedPreferences prefs) {
        Context ctx = getContext();
        Resources r = ctx.getResources();

        mBallSpeedModifier = Math.max(0, prefs.getInt(Pong.PREF_BALL_SPEED, 0));
        mMuted = prefs.getBoolean(Pong.PREF_MUTED, mMuted);
        mLivesModifier = Math.max(0, prefs.getInt(Pong.PREF_LIVES, 2));
        mCpuHandicap = Math.max(0, Math.min(PLAYER_PADDLE_SPEED-1, prefs.getInt(Pong.PREF_HANDICAP, 4)));

        String strategy = prefs.getString(Pong.PREF_STRATEGY, null);

        String strategies[] = r.getStringArray(R.array.values_ai_strategies);

        mAiStrategy = 1;
        // Linear-search the array for the appropriate strategy index =/
        for(int i = 0; strategy != null && strategy.length() > 0 && i < strategies.length; i++) {
            if(strategy.equals(strategies[i])) {
                mAiStrategy = i;
                break;
            }
        }

        shuffleStages();

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
            initializePongView();
            mInitialized = true;
        }

        long now = System.currentTimeMillis();
        if(gameRunning() && mCurrentState != State.Stopped) {
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

    public void getBalls(int num_balls) {
        mBalls = new ArrayList<>();
        for(int i = 0; i < num_balls; i++) {
            mBalls.add(new Ball());
        }
    }

    public boolean isStarted(){
        return started;
    }

    /**
     * All of the game's logic (per game iteration) is in this function.
     * Given some initial game state, it computes the next game state.
     */
    private void doGameLogic() {
        if(brickWall!= null && brickWall.DelayCollision > 0) {
            --brickWall.DelayCollision;
        }
        boolean isServing = false;

        for(int i = 0; i < mBalls.size(); i++) {
            if(mBalls.get(i) == null){
                continue;
            }
            mBalls.get(i).move();
            isServing = mBalls.get(i).serving();

            // Shake it up if it appears to not be moving vertically
            if(mBalls.get(i).py == mBalls.get(i).y && !mBalls.get(i).serving()) {
                mBalls.get(i).randomAngle(mBalls.get(i).goingUp());
            }

            // Check collision with brick wall
            if(brickWall != null) {
                brickWall.checkCollision(mBalls.get(i));
            }

            if(baseWalls != null) {
                mBalls.get(i).bounceBaseWall(baseWalls.get(0));
                mBalls.get(i).bounceBaseWall(baseWalls.get(1));
            }

            if(baseWalls  != null){
                if (mBalls.get(i).goingDown() && P1.collides(mBalls.get(i))) {
                    P1.loseLife();
                    mNewRound = true;
                    break;
                }

                if (mBalls.get(i).goingUp() && P2.collides(mBalls.get(i))) {
                    P2.loseLife();
                    mNewRound = true;
                    break;
                }
            }

            // Check ball bounce
            handleBounces(mBalls.get(i));
        }

        if(terminator != null) {
            terminator.move();
            terminator.shoot();
        }

        // Check bullet successful hit
        for(Bullet bullet: bullets){
            bullet.move();
            if(P1.collides(bullet)){
                P1.disableMove();
                playSound(mHitSFX);
            }
            if(P2.collides(bullet)){
                P2.disableMove();
                playSound(mHitSFX);
            }

        }

        if(P1.player) {
            P1.move();
        }else{
            doAI(P1, P2, mBalls);
        }

        if(P2.player) {
            P2.move();
        }else{
            doAI(P2, P1, mBalls);
        }

        // See if all is lost
        if(baseWalls  == null) {
            for (int i = 0; i < mBalls.size(); i++) {
                if (mBalls.get(i) == null) {
                    continue;
                }
                if (mBalls.get(i).y >= getHeight()) {
                    mBalls.set(i, null);
                    P1.loseLife();
                    mNewRound = true;
                    break;
                } else if (mBalls.get(i).y <= 0) {
                    mBalls.set(i, null);
                    P2.loseLife();
                    mNewRound = true;
                    break;
                }
            }
        }else {
//            for(Ball ball: mBalls) {
//                if(ball.goingDown() && P1.collides(ball)) {
//                    P1.loseLife();
//                    mNewRound = true;
//                    break;
//                }
//
//                if(ball.goingUp() && P2.collides(ball)) {
//                    P2.loseLife();
//                    mNewRound = true;
//                    break;
//                }
//            }
        }
//        mNewRound = true;
//        for(Ball mBall:mBalls){
//            if(mBall != null){
//                mNewRound = false;
//                break;
//            }
//        }
        if(!P1.living() || !P2.living()) {
            playSound(mWinSFX);
        }

        if(P1.player && P2.player) {
            PongGameModel pgm = new PongGameModel();
            int[] rmPaddle = new int[4];
            int[] rmBall = new int[3];
//            Rect paddle = MessageParser.getInstance().getPaddle();
//            float[] ball = MessageParser.getInstance().getBall();


            if(WifiDirectManager.getInstance().isServer()){
                rmPaddle = P1.getPaddle();
//                if(paddle != null) {
//                    P2.setPaddle(paddle);
//                }
                if(mBalls.size() > 0) {
                    rmBall = (mBalls.get(0).getBall());
                }
            }
            else{
                rmPaddle = P1.getPaddle();
//                if(paddle != null) {
//                    P2.setPaddle(paddle);
//                }
//                if(rmBall != null) {
//                    mBall.setBall(ball);
//                }
            }
//            String message = MessageParser.getInstance().getMessage(rmBall,rmPaddle);

            pgm.setBall(rmBall);
            pgm.setPaddle(rmPaddle);
            WifiDirectManager.getInstance().sendMessage(pgm);
        }
    }

    protected void handleBounces(Ball b) {
        handleTopFastBounce(P2, b);
        handleBottomFastBounce(P1, b);

        // Handle bouncing off of a wall
        if(b.x <= Ball.RADIUS || b.x >= getWidth() - Ball.RADIUS) {
            b.bounceWall();
            playSound(mWallSFX);
            if(b.x == Ball.RADIUS)
                b.x++;
            else
                b.x--;
        }

    }

    protected void handleTopFastBounce(Paddle tank, Ball b) {
        if(b.goingUp() == false) return;

        float tx = b.x;
        float ty = b.y - Ball.RADIUS;
        float ptx = b.px;
        float pty = b.py - Ball.RADIUS;
        float dyp = ty - tank.getBottom();
        float xc = tx + (tx - ptx) * dyp / (ty - pty);

        if(ty < tank.getBottom() && pty > tank.getBottom()
                && xc > tank.getLeft() && xc < tank.getRight()) {

            b.x = xc;
            b.y = tank.getBottom() + Ball.RADIUS;
            b.bouncePaddle(tank);
            playSound(mPaddleSFX);
            increaseDifficulty(b);
        }
    }

    protected void handleBottomFastBounce(Paddle tank, Ball b) {
        if(b.goingDown() == false) return;

        float bx = b.x;
        float by = b.y + Ball.RADIUS;
        float pbx = b.px;
        float pby = b.py + Ball.RADIUS;
        float dyp = by - tank.getTop();
        float xc = bx + (bx - pbx) * dyp / (pby - by);

        if(by > tank.getTop() && pby < tank.getTop()
                && xc > tank.getLeft() && xc < tank.getRight()) {

            b.x = xc;
            b.y = tank.getTop() - Ball.RADIUS;
            b.bouncePaddle(tank);
            playSound(mPaddleSFX);
            increaseDifficulty(b);
        }
    }

    private void doAI(Paddle cpu, Paddle opponent, ArrayList<Ball> balls) {
        if(balls.get(0).serving()) {
            cpu.destination = getWidth() / 2;
            cpu.move(true);
            return;
        }
        if(baseWalls != null) {
            aiDodge(cpu,balls);
            return;
        }
        int px = cpu.centerX();
        int py = cpu.centerY();

        Ball b = null;

        float minDist = 999999999;
        for(Ball ball: balls) {
            if(ball == null){
                continue;
            }
            float bx = ball.x;
            float by = ball.y;

            float dist  = (px-bx)*(px-bx) + (py-by)*(py-by);
            if(dist < minDist) {
                minDist = dist;
                b = new Ball(ball);
            }

        }

        doAI(cpu, opponent, b);
    }

    private int predBallX(Ball b, Paddle p) {
        boolean downwards = p.getBottom() > getHeight()/2;
        // Playable width of the stage
        float playWidth = getWidth() - 2 * Ball.RADIUS;

        // Distance of ball to wall
        float wallDist = (b.goingLeft()) ? b.x - Ball.RADIUS : playWidth - b.x + Ball.RADIUS;

        // Y distance to first point of bounce on wall
        float firstBounceY = wallDist * Math.abs(b.vy/b.vx);

        if((!downwards && firstBounceY > (b.y-p.getBottom()))) {
            return (int)(b.y-p.getBottom()*Math.abs(b.vx/b.vy));
        }
        else if(downwards && firstBounceY > (p.getTop() - b.y)){
            return (int)(p.getTop() - b.y*Math.abs(b.vx/b.vy));
        }

        float total = (downwards) ?  p.getTop() - firstBounceY: firstBounceY - p.getBottom();

        float bounceY = Math.abs(b.vy/b.vx)*playWidth;

        // Effective x-translation left over after first bounce
        float remainsY = total % bounceY;

        float xPred = remainsY*Math.abs(b.vx/b.vy);

        return (int)xPred;
    }

    private void aiDodge(Paddle cpu, ArrayList<Ball> balls) {
        ArrayList<Ball> bx = new ArrayList<>();
        ArrayList<Integer> bxy = new ArrayList<>();
        for(Ball ball: balls){
            if(cpu.centerY() < getHeight()/2) {
                if (ball.goingUp() && ball.y < (float)getHeight()/2) {
                    int x = predBallX(ball,cpu);
                    bxy.add(x);
                }
//                else if((ball.y - Ball.RADIUS) < (cpu.getBottom() + Paddle.PADDLE_THICKNESS)) {
//                    bxy.add((int)ball.x);
//                }

            }
            else{
                if (ball.goingDown() && ball.y > (float) getHeight()/2) {
                    int x = predBallX(ball,cpu);
                    bxy.add(x);
                }
//                else if((ball.y + Ball.RADIUS) > (cpu.getBottom() - Paddle.PADDLE_THICKNESS)) {
//                    bxy.add((int)ball.x);
//                }
            }
        }
//        Collections.sort(bx, (b1, b2) -> Integer.compare((int)b2.x,(int)b1.x));
        Collections.sort(bxy);

        float left = 0;
        float pos1 = 0;
        float dist = 0;

        for(int i = 0; i < bxy.size(); i++) {
            if(bxy.get(i) - left >= dist){
                dist = bxy.get(i) - left;
                pos1 = left + dist/2;

                left = bxy.get(i);
            }
        }
        if(bxy.size() > 0 && getWidth()-bxy.get(bxy.size()-1) > dist){
            dist = getWidth() - bxy.get(bxy.size()-1);
            pos1 = left + dist/2;
        }
        if(pos1 < (float) Paddle.PADDLE_WIDTH/2){
            pos1 = (float) Paddle.PADDLE_WIDTH/2;
        }

        if(pos1 > getWidth()-(float) Paddle.PADDLE_WIDTH/2){
            pos1 = getWidth()-(float) Paddle.PADDLE_WIDTH/2;
        }

        if((int)pos1 != 0) {
            cpu.destination = (int) pos1;
        }
        cpu.move(true);
    }

    private void doAI(Paddle cpu, Paddle opponent, Ball b) {

        switch(mAiStrategy) {
            case 2:	aiFollow(cpu, b); break;
            case 1:	aiExact(cpu, b); break;
            default: aiPrediction(cpu,opponent, b); break;
        }
    }

    /**
     * A generalized Pong AI player. Takes a Rect object and a Ball, computes where the ball will
     * be when ball.y == rect.y, and tries to move toward that x-coordinate. If the ball is moving
     * straight it will try to clip the ball with the edge of the paddle.
     * @param cpu
     */

    private void aiPrediction(Paddle cpu, Paddle opponent, Ball b) {
        Ball ball = new Ball(b);

        // Special case: move torward the center if the ball is blinking
//        if(b.serving()) {
//            cpu.destination = getWidth() / 2;
//            cpu.move(true);
//            return;
//        }

        boolean coming = (cpu.centerY() < ball.y && ball.vy < 0)
                || (cpu.centerY() > ball.y && ball.vy > 0);

        if(!coming) {
            cpu.destination = getWidth()/2;
            cpu.move(true);
            return;
        }

        // Something is wrong if vy = 0.. let's wait until things fix themselves
        if(ball.vy == 0) return;

        // Y-Distance from ball to Rect 'cpu'
        float cpuDist = Math.abs(ball.y - cpu.centerY());
        // Y-Distance to opponent.
        float oppDist = Math.abs( ball.y - opponent.centerY() );

        // Distance between two paddles.
        float paddleDistance = Math.abs(cpu.centerY() - opponent.centerY());

        // Is the ball coming at us?
//        boolean coming = (cpu.centerY() < ball.y && ball.vy < 0)
//                || (cpu.centerY() > ball.y && ball.vy > 0);

        // Total amount of x-distance the ball covers
        float total = ((((coming) ? cpuDist : oppDist + paddleDistance)) / Math.abs(ball.vy)) * Math.abs( ball.vx );

        // Playable width of the stage
        float playWidth = getWidth() - 2 * Ball.RADIUS;


        float wallDist = (ball.goingLeft()) ? ball.x - Ball.RADIUS : playWidth - ball.x + Ball.RADIUS;

        // Effective x-translation left over after first bounce
        float remains = (total - wallDist) % playWidth;

        // Bounces the ball will incur
        int bounces = (int) ((total) / playWidth);

        boolean left = (bounces % 2 == 0) ? !ball.goingLeft() : ball.goingLeft();

        cpu.destination = getWidth() / 2;

        // Now we need to compute the final x. That's all that matters.
        if(bounces == 0) {
            cpu.destination = (int) (ball.x + total * Math.signum(ball.vx));
        }
        else if(left) {
            cpu.destination = (int) (Ball.RADIUS + remains);
        }
        else { // The ball is going right...
            cpu.destination = (int) ((Ball.RADIUS + playWidth) - remains);
        }

        // Try to give it a little kick if vx = 0
        int salt = (int) (System.currentTimeMillis() / 10000);
        Random r = new Random((long) (cpu.centerY() + ball.vx + ball.vy + salt));
        int width = cpu.getWidth();
        cpu.destination = (int) bound(
                cpu.destination + r.nextInt(2 * width - (width / 5)) - width + (width / 10),
                0, getWidth()
        );
        cpu.move(true);
    }

    private void aiExact(Paddle cpu, Ball b) {
        Ball ball = new Ball(b);

        boolean coming = (cpu.centerY() < ball.y && ball.vy < 0)
                || (cpu.centerY() > ball.y && ball.vy > 0);

        if(!coming) {
            cpu.destination = getWidth()/2;
            cpu.move(true);
            return;
        }
        cpu.destination = (int) b.x;
        // Watch out, computer is cheating !!!
//        cpu.setPosition(cpu.destination);
        cpu.move(true);
    }

    private void aiFollow(Paddle cpu, Ball b) {
        Ball ball = new Ball(b);

        boolean coming = (cpu.centerY() < ball.y && ball.vy < 0)
                || (cpu.centerY() > ball.y && ball.vy > 0);

        if(!coming) {
            cpu.destination = getWidth()/2;
            cpu.move(true);
            return;
        }

        cpu.destination = (int) b.x;
        cpu.move(true);
    }

    /**
     * Knocks up the framerate a bit to keep it difficult.
     */
    private void increaseDifficulty(Ball b) {
        b.speed++;
    }

    /**
     * Set the state, start a new round, start the loop if needed.
     * @param next, the next state
     */
    public void setMode(State next) {
        mCurrentState = next;
        nextRound();
        update();
    }

    /**
     * Reset the paddles/touchboxes/framespersecond/ballcounter for the next round.
     */
    private void nextRound() {
        getBalls(NUM_BALLS);
        serveBall(mBalls);
    }

    /**
     * Initializes objects needed to carry out the game.
     * This should be called once as soon as the View has reached
     * its inflated size.
     */
    private void initializePongView() {
        initializePause();
        initializePaddles();
    }

    private void initializePause() {
        int min = Math.min(getWidth() / 4, getHeight() / 4);
        int xmid = getWidth() / 2;
        int ymid = getHeight() / 2;
        mPauseTouchBox = new Rect(xmid - min, ymid - min, xmid + min, ymid + min);
    }

    private void initializePaddles() {
        Rect redTouch = new Rect(0,0,getWidth(),getHeight() / 8);
        Rect blueTouch = new Rect(0, 7 * getHeight() / 8, getWidth(), getHeight());

        Rect blueBTouch = new Rect(0, 6 * getHeight() / 8, getWidth(), 7 * getHeight() / 8);


        int p1Color,p2Color;

        if(mP2_Player && !WifiDirectManager.getInstance().isServer()){
            p1Color = Color.RED;
            p2Color = Color.BLUE;
        }else{
            p1Color = Color.BLUE;
            p2Color = Color.RED;
        }


        P2 = new Paddle(p2Color, redTouch.bottom + PADDING);
        P1 = new Paddle(p1Color, blueTouch.top - PADDING - Paddle.PADDLE_THICKNESS);


        loadStage();

        P2.player = mP2_Player;
        P1.player = mP1_Player;

        P2.setTouchbox( redTouch );
        P1.setTouchbox( blueTouch );

        P1.setBTouchbox(blueBTouch);



        P2.setHandicap(mCpuHandicap);
//        mBlue.setHandicap(20);
        P1.setSpeed(PLAYER_PADDLE_SPEED);



        P2.setLives(STARTING_LIVES + mLivesModifier);
        P1.setLives(STARTING_LIVES + mLivesModifier);
    }

    void resetStage() {
        brickWall = null;
        blindWall = null;
        terminator = null;
        baseWalls = null;
    }

    public void loadStage() {
        int stage = stageNumbers.get(currentStage);
        resetStage();
//        int stage = 3;
        switch (stage) {
            case 0:
                brickWall = new BrickWall();
                NUM_BALLS = 2;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;
            case 1:
                blindWall = new BlindWall();
                NUM_BALLS = 1;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;
            case 2:
                terminator = new Terminator(Color.CYAN,getHeight()/2 - Paddle.PADDLE_THICKNESS/2);
                NUM_BALLS = 1;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;
            case 3:
                baseWalls = new ArrayList<>();
                baseWalls.add(new BaseWall(Color.WHITE,P2.getTop() - Paddle.PADDLE_THICKNESS - PADDING));
                baseWalls.add(new BaseWall(Color.WHITE,P1.getBottom() + PADDING));
                NUM_BALLS = 4;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;

            case 4:
                terminator = new Terminator(Color.CYAN,getHeight()/2 - Paddle.PADDLE_THICKNESS/2);
                baseWalls = new ArrayList<>();
                baseWalls.add(new BaseWall(Color.WHITE,P2.getTop() - Paddle.PADDLE_THICKNESS - PADDING));
                baseWalls.add(new BaseWall(Color.WHITE,P1.getBottom() + PADDING));
                NUM_BALLS = 4;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;

            case 5:
                terminator = new Terminator(Color.CYAN,getHeight()/2 - Paddle.PADDLE_THICKNESS/2);
                blindWall = new BlindWall();
                NUM_BALLS = 1;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;

            case 6:
                blindWall = new BlindWall();
                brickWall = new BrickWall();
                NUM_BALLS = 2;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;

            case 7:
                blindWall = new BlindWall();
                baseWalls = new ArrayList<>();
                baseWalls.add(new BaseWall(Color.WHITE,P2.getTop() - Paddle.PADDLE_THICKNESS - PADDING));
                baseWalls.add(new BaseWall(Color.WHITE,P1.getBottom() + PADDING));
                NUM_BALLS = 4;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;

            case 8:
                brickWall = new BrickWall();
                baseWalls = new ArrayList<>();
                baseWalls.add(new BaseWall(Color.WHITE,P2.getTop() - Paddle.PADDLE_THICKNESS - PADDING));
                baseWalls.add(new BaseWall(Color.WHITE,P1.getBottom() + PADDING));
                NUM_BALLS = 4;
                Log.d("Stage Select: ", String.valueOf(stage));
                break;
        }
        ++currentStage;
        if(currentStage >= NUM_STAGES){
            shuffleStages();
            currentStage = 0;
        }
    }

    /**
     * Reset ball to an initial state
     */
    private void serveBall(ArrayList<Ball> balls) {
        boolean up = true;
        for(int i = 0; i < balls.size(); i++) {
            balls.get(i).x = getWidth() / 2;
            balls.get(i).y = getHeight() / 2;
            balls.get(i).speed = Ball.SPEED + mBallSpeedModifier;
            up = !up;
            balls.get(i).randomAngle(up);
            balls.get(i).pause();
        }
        if(brickWall != null) {
            brickWall.delayCollision();
        }
    }

    protected float bound(float x, float low, float hi) {
        return Math.max(low, Math.min(x, hi));
    }

    /**
     * Use for keeping track of a position.
     * @author pkilgo
     *
     */
    class Point {
        private int x, y;
        Point() {
            x = 0; y = 0;
        }

        Point(int x, int y) {
            this.x = x; this.y = y;
        }

        public int getX() { return x; }
        public int getY() { return y ; }
        public void set(double d, double e) { this.x = (int) d; this.y = (int) e; }

        public void translate(int i, int j) { this.x += i; this.y += j; }

        @Override
        public String toString() {
            return "Point: (" + x + ", " + y + ")";
        }
    }

    public void onSizeChanged(int w, int h, int ow, int oh) {
    }

    /**
     * Paints the game!
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mInitialized == false) {
            return;
        }

        Context context = getContext();

        // Draw the paddles / touch boundaries
        P1.draw(canvas);
        P2.draw(canvas);

        // Draw touchboxes if needed
        if(gameRunning() && P2.player && mCurrentState == State.Running)
            P2.drawTouchbox(canvas);

        if(gameRunning() && P1.player && mCurrentState == State.Running)
            P1.drawTouchbox(canvas);

        // Draw ball stuff
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(Color.WHITE);

        for(Ball mBall : mBalls) {
            if(mBall == null){
                continue;
            }
            mBall.draw(canvas);
        }

        mPaint.setStyle(Style.FILL);
        mPaint.setColor(Color.GREEN);


        for(Bullet bullet:bullets){
            bullet.draw(canvas);
        }

        if(brickWall != null) {
            brickWall.draw(canvas);
        }

        if(blindWall != null) {
            blindWall.draw(canvas);
        }

        if(baseWalls != null) {
            for(BaseWall bw:baseWalls) {
                bw.draw(canvas);
            }
        }

        if(terminator != null) {
            terminator.draw(canvas);
        }


        // If either is a not a player, blink and let them know they can join in!
        // This blinks with the ball.
        for(Ball mBall: mBalls) {
            if (mBall!= null && mBall.serving()) {
                String join = context.getString(R.string.join_in);
                int joinw = (int) mPaint.measureText(join);

                if (!P2.player) {
                    mPaint.setColor(Color.RED);
                    canvas.drawText(join, getWidth() / 2 - joinw / 2, P2.touchCenterY(), mPaint);
                }

                if (!P1.player) {
                    mPaint.setColor(Color.BLUE);
                    canvas.drawText(join, getWidth() / 2 - joinw / 2, P1.touchCenterY(), mPaint);
                }
                break;
            }
        }

        // Show where the player can touch to pause the game
        for(Ball mBall: mBalls) {
            if (mBall != null && mBall.serving()) {
                String pause = context.getString(R.string.pause);
                int pausew = (int) mPaint.measureText(pause);

                mPaint.setColor(Color.GREEN);
                mPaint.setStyle(Style.STROKE);
                canvas.drawRect(mPauseTouchBox, mPaint);
                canvas.drawText(pause, getWidth() / 2 - pausew / 2, getHeight() / 2, mPaint);
                break;
            }
        }

        // Paint a PAUSED message
        if(gameRunning() && mCurrentState == State.Stopped) {
            String s = context.getString(R.string.paused);
            int width = (int) mPaint.measureText(s);
            int height = (int) (mPaint.ascent() + mPaint.descent());
            mPaint.setColor(Color.WHITE);
            canvas.drawText(s, getWidth() / 2 - width / 2, getHeight() / 2 - height / 2, mPaint);
        }

        // Draw a 'lives' counter
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Style.FILL_AND_STROKE);
        for(int i = 0; i < P2.getLives(); i++) {
            canvas.drawCircle(Ball.RADIUS + PADDING + i * (2 * Ball.RADIUS + PADDING),
                    PADDING + Ball.RADIUS,
                    Ball.RADIUS,
                    mPaint);
        }

        for(int i = 0; i < P1.getLives(); i++) {
            canvas.drawCircle(Ball.RADIUS + PADDING + i * (2 * Ball.RADIUS + PADDING),
                    getHeight() - PADDING - Ball.RADIUS,
                    Ball.RADIUS,
                    mPaint);
        }


        // Draw a 'bullet' counter
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Style.FILL_AND_STROKE);
        int xpos = this.getWidth() - (P2.getBullets() + 1)*(2 * Bullet.RADIUS + PADDING);
        for(int i = 0; i < P2.getBullets(); i++) {
            canvas.drawCircle(xpos + Bullet.RADIUS + PADDING + i * (2 * Bullet.RADIUS + PADDING),
                    PADDING + Bullet.RADIUS,
                    Bullet.RADIUS,
                    mPaint);
        }

        for(int i = 0; i < P1.getBullets(); i++) {

            canvas.drawCircle(xpos + Bullet.RADIUS + PADDING + i * (2 * Bullet.RADIUS + PADDING),
                    getHeight() - PADDING - Bullet.RADIUS,
                    Bullet.RADIUS,
                    mPaint);
        }

        // Announce the winner!
        if(!gameRunning()) {
            mPaint.setColor(Color.GREEN);
            String s = "You both lose";

            if(!P1.living()) {
                s = context.getString(R.string.red_wins);
                mPaint.setColor(Color.RED);
            }
            else if(!P2.living()) {
                s = context.getString(R.string.blue_wins);
                mPaint.setColor(Color.BLUE);
            }

            int width = (int) mPaint.measureText(s);
            int height = (int) (mPaint.ascent() + mPaint.descent());
            canvas.drawText(s, getWidth() / 2 - width / 2, getHeight() / 2 - height / 2, mPaint);
        }
    }

    /**
     * Touching is the method of movement. Touching the touchscreen, that is.
     * A player can join in simply by touching where they would in a normal
     * game.
     */
    public boolean onTouch(View v, MotionEvent mo) {
//        if(v != this || !gameRunning()) return false;
        if(v != this) return false;

        // We want to support multiple touch and single touch
        InputHandler handle = InputHandler.getInstance();

        // Loop through all the pointers that we detected and
        // process them as normal touch events.
        for(int i = 0; i < handle.getTouchCount(mo); i++) {
            int tx = (int) handle.getX(mo, i);
            int ty = (int) handle.getY(mo, i);

            // Bottom paddle moves when we are playing in one or two player mode and the touch
            // was in the lower quartile of the screen.
            if(P1.player) {
                if(P1.inTouchbox(tx,ty) && mo.getAction() == MotionEvent.ACTION_DOWN) {
                    init_dest1 = P1.destination;
                    init_p1Touch = tx;
                }
                else if(P1.inTouchbox(tx,ty) && mo.getAction() == MotionEvent.ACTION_MOVE) {
                    P1.destination = init_dest1+tx-init_p1Touch;
                }
                if(P1.bulletCount > 0 && P1.inBTouchbox(tx,ty)) {
                    P1.shoot();
                }
            }
            else if(P2.player && P2.inTouchbox(tx,ty)) {
                P2.destination = tx;
            }
            if(mo.getAction() == MotionEvent.ACTION_DOWN && mPauseTouchBox.contains(tx, ty)) {
                if(!gameRunning()) {
                    newGame();
//                    mCurrentState = State.Running;
//                    mLastState = State.Stopped;
//                    mNewRound = true;
//                    mContinue = true;
                }
                else {
                    if (mCurrentState != State.Stopped) {
                        mLastState = mCurrentState;
                        mCurrentState = State.Stopped;
                    } else {
                        mCurrentState = mLastState;
                        mLastState = State.Stopped;
                    }
                }
            }

            // In case a player wants to join in...
            if(mo.getAction() == MotionEvent.ACTION_DOWN) {
                if(!P1.player && P1.inTouchbox(tx,ty)) {
                    P1.player = true;
                }
//                else if(!mRed.player && mRed.inTouchbox(tx,ty)) {
//                    mRed.player = true;
//                }
            }
        }

        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if(!gameRunning()) return false;

        if(P1.player == false) {
            P1.player = true;
            P1.destination = P1.centerX();
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                P1.destination = (int) Math.max(0, Math.min(getWidth(), P1.destination + SCROLL_SENSITIVITY * event.getX()));
                break;
        }

        return true;
    }

    /**
     * Reset the lives, paddles and the like for a new game.
     */
    public void newGame() {
        resetPaddles();
        serveBall(mBalls);
        resumeLastState();
    }

    /**
     * Resets the lives and the position of the paddles.
     */
    private void resetPaddles() {
        int mid = getWidth() / 2;
        P2.setPosition(mid);
        P1.setPosition(mid);
        P2.destination = mid;
        P1.destination = mid;
        P2.setLives(STARTING_LIVES + mLivesModifier);
        P1.setLives(STARTING_LIVES + mLivesModifier);
        P1.setBullet(STARTING_BULLETS);
        P2.setBullet(STARTING_BULLETS);

        getBalls(NUM_BALLS);
        loadStage();
    }

    /**
     * This is kind of useless as well.
     */
    private void resumeLastState() {
        if(mLastState == State.Stopped && mCurrentState == State.Stopped) {
            mCurrentState = State.Running;
        }
        else if(mCurrentState != State.Stopped) {
            // Do nothing
        }
        else if(mLastState != State.Stopped) {
            mCurrentState = mLastState;
            mLastState = State.Stopped;
        }
    }

    public boolean gameRunning() {
        return mInitialized && P2 != null && P1 != null
                && P2.living() && P1.living();
    }

    public void pause() {
        mLastState = mCurrentState;
        mCurrentState = State.Stopped;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void setPlayerControl(boolean red, boolean blue) {
        mP2_Player = red;
        mP1_Player = blue;
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
        mPool.release();
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

    @Override
    public void onMessageReceived(Game message) {
//        MessageParser.getInstance().parseMessage(message);
    }

    class Ball {
        public float x, y, xp, yp, vx, vy;
        public float px, py;
        public float speed = SPEED;

        protected double mAngle;
        protected boolean mNextPointKnown = false;
        protected int mCounter = 0;

        public Ball() {
            findVector();
        }

        public Ball(Ball other) {
            x = other.x;
            y = other.y;
            xp = other.xp;
            yp = other.yp;
            vx = other.vx;
            vy = other.vy;
            speed = other.speed;
            mAngle = other.mAngle;
        }

        protected void findVector() {
            vx = (float) (speed * Math.cos(mAngle));
            vy = (float) (speed * Math.sin(mAngle));
        }

        public boolean goingUp() {
            return mAngle >= Math.PI;
        }

        public boolean goingDown() {
            return !goingUp();
        }

        public boolean goingLeft() {
            return mAngle <= 3 * Math.PI / 2 && mAngle > Math.PI / 2;
        }

        public boolean goingRight() {
            return !goingLeft();
        }

        public double getAngle() {
            return mAngle;
        }

        public boolean serving() {
            return mCounter > 0;
        }

        public void pause() {
            mCounter = 60;
        }

        public void move() {
            px = x;
            py = y;
            if(mCounter <= 0) {
                x = keepX(x + vx);
                y += vy;
            }
            else {
                mCounter--;
            }
        }

        public void randomAngle() {
            setAngle( Math.PI / 2 + RNG.nextInt(2) * Math.PI + Math.PI / 2 * RNG.nextGaussian() );
        }

        public void randomAngle(boolean up) {
            double angle;
            if(up){
                angle = 3*Math.PI/2 + RNG.nextGaussian();
                if(angle < Math.PI/6 + Math.PI) angle = Math.PI/6 + Math.PI;
                else if(angle > 5*Math.PI/6 + Math.PI) angle = 5*Math.PI/6 + Math.PI;
            }
            else{
                angle = Math.PI/2 + RNG.nextGaussian();
                if(angle < Math.PI/6) angle = Math.PI/6;
                else if(angle > 5*Math.PI/6) angle = 5*Math.PI/6;
            }
            setAngle(angle);
        }

        public void setAngle(double angle) {
            mAngle = angle % (2 * Math.PI);
            mAngle = boundAngle(mAngle);
            findVector();
        }

        public int[] getBall() {
            int[] ball = {(int)x,(int)y,Ball.RADIUS};
            return ball;
        }
        public void setBall(float[] ball) {
            x = ball[0];
            y = ball[1];
//            Ball.RADIUS = ball[2];
        }

        public void draw(Canvas canvas) {
            if((mCounter / 10) % 2 == 1 || mCounter == 0)
                canvas.drawCircle(x, y, Ball.RADIUS, mPaint);
        }

        /**
         * Tells us if the ball collides with a rectangle.
         * @param p, the rectangle
         * @return true if the ball is colliding, false if not
         */
        public boolean collides(Paddle p) {
            return p.collides(this);
        }

        /**
         * Method bounces the ball across a vertical axis. Seriously it's that easy.
         * Math failed me when figuring this out so I guessed instead.
         */
        public void bouncePaddle(Paddle p) {
            double angle;

            // up-right case
            if(mAngle >= Math.PI) {
                angle = 4 * Math.PI - mAngle;
            }
            // down-left case
            else {
                angle = 2 * Math.PI - mAngle;
            }

            angle %= (2 * Math.PI);
            angle = salt(angle, p);
//			normalize(p);
            setAngle(angle);
        }

        /**
         * Bounce the ball off a horizontal axis.
         */
        public void bounceWall() {
            setAngle(3 * Math.PI - mAngle);
//            vx = -vx;
        }

        public void bounceBaseWall(BaseWall bw) {
            double angle;
            if(y + Ball.RADIUS >= bw.getTop() && bw.centerY() > PongView.this.getHeight()/2){
                angle = 4 * Math.PI - mAngle;
            }
            else if(y-Ball.RADIUS < bw.getBottom() && bw.centerY() < PongView.this.getHeight()/2){
                angle = 2*Math.PI - mAngle;
            }
            else{
                return;
            }
            angle %= (2 * Math.PI);
            setAngle(angle);
        }

        protected double salt(double angle, Paddle tank) {
            int cx = tank.centerX();
            double halfWidth = tank.getWidth() / 2;
            double change = 0.0;

            if(goingUp()) change = SALT * ((cx - x) / halfWidth);
            else change = SALT * ((x - cx) / halfWidth);

            return boundAngle(angle, change);
        }

        /**
         * Normalizes a ball's position after it has hit a paddle.
         * @param p The paddle the ball has hit.
         */
        protected void normalize(Paddle p) {
            // Quit if the ball is outside the width of the paddle
            if(x < p.getLeft() || x > p.getRight()) {
                return;
            }

            // Case if ball is above the paddle
            if(y < p.getTop()) {
                y = Math.min(y, p.getTop() - Ball.RADIUS);
            }
            else if(y > p.getBottom()) {
                y = Math.max(y, p.getBottom() + Ball.RADIUS);
            }
        }

        /**
         * Bounds sum of <code>angle</code> and <code>angleChange</code> to the side of the
         * unit circle that <code>angle</code> is on.
         * @param angle The initial angle.
         * @param angleChange Amount to add to angle.
         * @return bounded angle sum
         */
        protected double boundAngle(double angle, double angleChange) {
            return boundAngle(angle + angleChange, angle >= Math.PI);
        }

        protected double boundAngle(double angle) {
            return boundAngle(angle, angle >= Math.PI);
        }

        /**
         * Bounds an angle in radians to a subset of the top
         * or bottom part of the unit circle.
         * @param angle The angle in radians to bound.
         * @param top Flag which indicates if we should bound to the top or not.
         * @return the bounded angle
         */
        protected double boundAngle(double angle, boolean top) {
            if(top) {
                return Math.max(Math.PI + BOUND, Math.min(2 * Math.PI - BOUND, angle));
            }

            return Math.max(BOUND, Math.min(Math.PI - BOUND, angle));
        }


        /**
         * Given it a coordinate, it transforms it into a proper x-coordinate for the ball.
         * @param x, the x-coord to transform
         * @return
         */
        protected float keepX(float x) {
            return bound(x, Ball.RADIUS, getWidth() - Ball.RADIUS);
        }

        public static final double BOUND = Math.PI / 9;
        public static final float SPEED = 4.0f;
        public static final int RADIUS = 15;
        public static final double SALT = 4 * Math.PI / 9;
    }



    class Paddle {
        protected int mColor;
        protected Rect mRect;
        protected Rect mTouch;
        protected Rect bTouch;
        protected int mHandicap = 0;
        protected int mSpeed = PLAYER_PADDLE_SPEED;
        protected int mLives = STARTING_LIVES;
        protected boolean enableMove = true;
        protected int moveTmr = 0;
        protected int shootDelay = SHOOT_DELAY;

        public boolean player = false;
        public int bulletCount = STARTING_BULLETS;

        public int destination;

        public Paddle(int c){

        }

        public Paddle(int c, int y) {
            mColor = c;

            int mid = PongView.this.getWidth() / 2;
            mRect = new Rect(mid - PADDLE_WIDTH, y,
                    mid + PADDLE_WIDTH, y + PADDLE_THICKNESS);
            destination = mid;
        }

        public void setBullet(int bullets) {
            bulletCount = bullets;
        }

        public boolean canMove() {
            return enableMove;
        }

        public void disableMove() {
            enableMove = false;
            moveTmr = MOVE_TMR;
        }

        public void shoot() {
            if(shootDelay > 0) {
                return;
            }
            Bullet b = new Bullet();
            b.y = this.centerY();
            b.x = this.centerX();
            if(b.y > PongView.this.getHeight()/2){
                b.setAngle(3*Math.PI/2);
            }
            else{
                b.setAngle(Math.PI/2);
            }
            bullets.add(b);
            --bulletCount;
            playSound(mShootSFX);
            shootDelay = SHOOT_DELAY;
        }

        public int getBullets() {
            return bulletCount;
        }

        public void move() {
            if(canMove()) {
                move(mSpeed);
            }
        }

        public void move(boolean handicapped) {
            if(canMove()) {
                move((handicapped) ? mSpeed - mHandicap : mSpeed);
            }
        }

        public void move(int s) {
            if(canMove()) {
                int dx = (int) Math.abs(mRect.centerX() - destination);

                if (destination < mRect.centerX()) {
                    mRect.offset((dx > s) ? -s : -dx, 0);
                } else if (destination > mRect.centerX()) {
                    mRect.offset((dx > s) ? s : dx, 0);
                }
            }
        }

        public int[] getPaddle(){
            int[] p = {mRect.left, mRect.top, mRect.right, mRect.bottom};
            return p;
        }

        public void setPaddle(Rect paddle){
            mRect.left = paddle.left;
            mRect.top = paddle.top;
            mRect.right = paddle.right;
            mRect.bottom = paddle.bottom;
        }

        public void setLives(int lives) {
            mLives = Math.max(0, lives);
        }

        public void setPosition(int x) {
            mRect.offset(x - mRect.centerX(), 0);
        }

        public void setTouchbox(Rect r) {
            mTouch = r;
        }

        public void setBTouchbox(Rect r) {
            bTouch = r;
        }

        public void setSpeed(int s) {
            mSpeed = (s > 0) ? s : mSpeed;
        }

        public void setHandicap(int h) {
            mHandicap = (h >= 0 && h < mSpeed) ? h : mHandicap;
        }

        public boolean inTouchbox(int x, int y) {
            return mTouch.contains(x, y);
        }

        public boolean inBTouchbox(int x, int y) {
            return bTouch.contains(x, y);
        }

        public void loseLife() {
            mLives = Math.max(0, mLives - 1);
            playSound(mMissSFX);
        }

        public boolean living() {
            return mLives > 0;
        }

        public int getWidth() {
            return Paddle.PADDLE_WIDTH;
        }

        public int getTop() {
            return mRect.top;
        }

        public int getBottom() {
            return mRect.bottom;
        }

        public int centerX() {
            return mRect.centerX();
        }

        public int centerY() {
            return mRect.centerY();
        }

        public int getLeft() {
            return mRect.left;
        }

        public int getRight() {
            return mRect.right;
        }

        public int touchCenterY() {
            return mTouch.centerY();
        }

        public int getLives() {
            return mLives;
        }

        public void draw(Canvas canvas) {
            if(shootDelay > 0){
                --shootDelay;
            }
            if(moveTmr > 0) {
                --moveTmr;
            }
            else{
                enableMove = true;
            }
            mPaint.setColor(mColor);
            mPaint.setStyle(Style.FILL);
            canvas.drawRect(mRect, mPaint);
        }

        public void drawTouchbox(Canvas canvas) {
            mPaint.setColor(mColor);
            mPaint.setStyle(Style.STROKE);

            // Heuristic for deciding which line to paint:
            // draw the one closest to middle
            int mid = getHeight() / 2;
            int top = Math.abs(mTouch.top - mid), bot = Math.abs(mTouch.bottom - mid);
            float y = (top < bot) ? mTouch.top : mTouch.bottom;
            canvas.drawLine(mTouch.left, y, mTouch.right, y, mPaint);
        }

        public boolean collides(Ball b) {
            return b.x >= mRect.left && b.x <= mRect.right &&
                    b.y >= mRect.top - Ball.RADIUS && b.y <= mRect.bottom + Ball.RADIUS;
        }

        public boolean collides(Bullet b) {
            return b.x >= mRect.left && b.x <= mRect.right &&
                    b.y >= mRect.top - Bullet.RADIUS && b.y <= mRect.bottom + Bullet.RADIUS;
        }

        /** Thickness of the paddle */
        private static final int PADDLE_THICKNESS = 40;

        /** Width of the paddle */
        private static final int PADDLE_WIDTH = 100;

        private static final int MOVE_TMR = 50;

        private static final int SHOOT_DELAY = 5;
    }


    class Terminator extends Paddle {
        private boolean shootUp = true;

        public Terminator(int c) {
            super(c,PongView.this.getHeight()/2);
            destination = PongView.this.getHeight()/2;
        }

        public Terminator(int c, int y) {
            super(c,y);
            destination = y;
        }

        @Override
        public void shoot() {
            if(shootDelay > 0) {
                return;
            }
            Bullet b = new Bullet();
            b.y = this.centerY();
            b.x = this.centerX();
            if (shootUp) {
                b.setAngle(3 * Math.PI / 2);
            } else {
                b.setAngle(Math.PI / 2);
            }
            bullets.add(b);
            shootUp = !shootUp;
            shootDelay = 60;
        }

        @Override
        public void move() {
            if(canMove()) {
                int dx = (int) Math.abs(mRect.centerX() - destination);
                if(dx < 2){
                    destination = RNG.nextInt(PongView.this.getWidth());
                }

                if (destination < mRect.centerX()) {
                    mRect.offset((dx > MOVE_SPEED) ? -MOVE_SPEED : -dx, 0);
                } else if (destination > mRect.centerX()) {
                    mRect.offset((dx > MOVE_SPEED) ? MOVE_SPEED : dx, 0);
                }
            }
        }

        private int MOVE_SPEED = 10;
    }

    class BaseWall extends Paddle {

        public BaseWall(int c, int y) {
            super(c,y);
            super.mRect.left = 0;
            super.mRect.right = PongView.this.getWidth();
        }
    }


    class BrickWall {
        protected int mColor;
        private int rHeight = PongView.this.getHeight()/5;
        private int rWidth = PongView.this.getWidth();
        private Rect[] bricks;
        private int bHeight;
        private int bWidth;
        private int brickCount;
        public int DelayCollision;

        public BrickWall() {
            bHeight = rHeight / BRICK_THICKNESS;
            bWidth = rWidth / BRICK_WIDTH;
            brickCount = BRICK_WIDTH * BRICK_THICKNESS;
            bricks = new Rect[brickCount];

            int pos = PongView.this.getHeight()/2 - (bHeight*(BRICK_THICKNESS/2));

            for(int i=0; i < bricks.length; i++) {
                Rect brick = new Rect();
                brick.left = (i%BRICK_WIDTH)*bWidth;
                brick.right = brick.left + bWidth;
                brick.top = pos + (int)(i/BRICK_WIDTH)*bHeight;
                brick.bottom = brick.top + bHeight;

                bricks[i] = brick;
            }
        }

        public void draw(Canvas canvas) {
            for(int i = 0; i < brickCount; i++) {
                if(bricks[i] == null) {
                    continue;
                }
                if(i%2 == 0) {
                    mPaint.setColor(Color.GRAY);
                    mPaint.setStyle(Style.FILL);
                }
                else{
                    mPaint.setColor(Color.WHITE);
                    mPaint.setStyle(Style.FILL);
                }
                canvas.drawRect(bricks[i], mPaint);
            }
        }

        public void delayCollision() {
            DelayCollision = 80;
        }

        public void checkCollision(Ball b) {
            if(DelayCollision > 0) {
                return;
            }
            else {
                for (int i = 0; i < brickCount; i++) {
                    Rect brick = bricks[i];
                    if (brick != null && b.x >= brick.left && b.x <= brick.right &&
                            b.y >= brick.top - Ball.RADIUS && b.y <= brick.bottom + Ball.RADIUS) {
                        bricks[i] = null;
                        boolean upwards = b.vy < 0;
                        if ((b.x - Ball.RADIUS <= brick.right || b.x + Ball.RADIUS >= brick.left) && b.y >= brick.top && b.y <= brick.bottom) {
//                            b.vx = -b.vx;
                            b.bounceWall();
                        } else if ((b.y + Ball.RADIUS >= brick.top || b.y - Ball.RADIUS <= brick.bottom) && b.x >= brick.left && b.x <= brick.right) {
//                            b.vy = -b.vy;
                            double angle;
                            double mAngle = b.getAngle();

                            // up-right case
                            if(mAngle >= Math.PI) {
                                angle = 4 * Math.PI - mAngle;
                            }
                            // down-left case
                            else {
                                angle = 2 * Math.PI - mAngle;
                            }
                            angle %= (2 * Math.PI);
                            b.setAngle(angle);
                        }
                        playSound(mWallSFX);
                    }
                }
            }
        }

        public boolean collides(Bullet b) {
//            return b.x >= mRect.left && b.x <= mRect.right &&
//                    b.y >= mRect.top - Ball.RADIUS && b.y <= mRect.bottom + Ball.RADIUS;
            return true;
        }

        /** Thickness of the Brick wall (Use Even number) */
        private static final int BRICK_THICKNESS = 6;

        /** Width of the paddle */
        private static final int BRICK_WIDTH = 11;
    }

    class BlindWall {
        protected int mColor;
        private int rHeight = PongView.this.getHeight()/5;
        private int rWidth = PongView.this.getWidth();
        private Rect[] blinds;
        private int bHeight;
        private int brickCount;

        public BlindWall() {
            bHeight = rHeight / BLING_THICKNESS;
            brickCount = BLING_THICKNESS;
            blinds = new Rect[brickCount];

            int pos = PongView.this.getHeight()/2 - (bHeight*(BLING_THICKNESS /2));

            for(int i = 0; i < blinds.length; i++) {
                Rect blind = new Rect();
                blind.left = 0;
                blind.right = rWidth;
                blind.top = pos + i*bHeight;
                blind.bottom = blind.top + bHeight;

                blinds[i] = blind;
            }
        }

        public void draw(Canvas canvas) {
            for(int i = 0; i < brickCount; i++) {
                if(blinds[i] == null) {
                    continue;
                }
                if(i%2 == 0) {
                    mPaint.setColor(Color.GRAY);
                    mPaint.setStyle(Style.FILL);
                }
                else{
                    mPaint.setColor(Color.WHITE);
                    mPaint.setStyle(Style.FILL);
                }
                canvas.drawRect(blinds[i], mPaint);
            }
        }

        /** Thickness of the Blind wall (Use Even number) */
        private static final int BLING_THICKNESS = 6;
    }

    class Bullet {
        public float x, y, xp, yp, vx, vy;
        public float speed = SPEED;
        protected Rect mTouch;
        protected double mAngle;
        protected boolean mNextPointKnown = false;
        protected int mCounter = 0;
        protected boolean alive = false;
        protected int aliveCounter = ALIVE_TMR;

        public Bullet() {
            findVector();
        }

        public Bullet(Bullet other) {
            x = other.x;
            y = other.y;
            xp = other.xp;
            yp = other.yp;
            vx = other.vx;
            vy = other.vy;
            speed = other.speed;
            mAngle = other.mAngle;
        }

        protected void findVector() {
            vx = (float) (speed * Math.cos(mAngle));
            vy = (float) (speed * Math.sin(mAngle));
        }

        public boolean isAlive() {
            return alive;
        }

        public boolean goingUp() {
            return mAngle >= Math.PI;
        }

        public boolean goingDown() {
            return !goingUp();
        }

        public boolean goingLeft() {
            return mAngle <= 3 * Math.PI / 2 && mAngle > Math.PI / 2;
        }

        public boolean goingRight() {
            return !goingLeft();
        }

        public double getAngle() {
            return mAngle;
        }

        public boolean serving() {
            return mCounter > 0;
        }

        public void pause() {
            mCounter = 60;
        }

        public void move() {
            if(mCounter <= 0) {
                x = keepX(x + vx);
                y += vy;
            }
            else {
                mCounter--;
            }
        }

        public void randomAngle() {
            setAngle( Math.PI / 2 + RNG.nextInt(2) * Math.PI + Math.PI / 2 * RNG.nextGaussian() );
        }

        public void setAngle(double angle) {
            mAngle = angle % (2 * Math.PI);
            mAngle = boundAngle(mAngle);
            findVector();
        }

        public float[] getBullet() {
            float[] ball = {x,y,Bullet.RADIUS};
            return ball;
        }

        public void setBullet(float[] bullet) {
            x = bullet[0];
            y = bullet[1];
//            Ball.RADIUS = ball[2];
        }

        public void draw(Canvas canvas) {

            if(aliveCounter > 0) {
                --aliveCounter;
            }
            else {
                alive = true;
            }

            if((mCounter / 10) % 2 == 1 || mCounter == 0)
                canvas.drawCircle(x, y, Bullet.RADIUS, mPaint);
        }

        /**
         * Tells us if the bullet collides with a rectangle.
         * @param p, the rectangle
         * @return true if the bullet is colliding, false if not
         */
        public boolean collides(Paddle p) {
            return p.collides(this);
        }

        /**
         * Method bounces the bullet across a vertical axis. Seriously it's that easy.
         * Math failed me when figuring this out so I guessed instead.
         */
        public void bouncePaddle(Paddle p) {
            double angle;

            // up-right case
            if(mAngle >= Math.PI) {
                angle = 4 * Math.PI - mAngle;
            }
            // down-left case
            else {
                angle = 2 * Math.PI - mAngle;
            }

            angle %= (2 * Math.PI);
            angle = salt(angle, p);
//			normalize(p);
            setAngle(angle);
        }

        /**
         * Bounce the ball off a horizontal axis.
         */
        public void bounceWall() {
            setAngle(3 * Math.PI - mAngle);
        }

        protected double salt(double angle, Paddle tank) {
            int cx = tank.centerX();
            double halfWidth = tank.getWidth() / 2;
            double change = 0.0;

            if(goingUp()) change = SALT * ((cx - x) / halfWidth);
            else change = SALT * ((x - cx) / halfWidth);

            return boundAngle(angle, change);
        }

        /**
         * Normalizes a ball's position after it has hit a paddle.
         * @param p The paddle the ball has hit.
         */
        protected void normalize(Paddle p) {
            // Quit if the ball is outside the width of the paddle
            if(x < p.getLeft() || x > p.getRight()) {
                return;
            }

            // Case if ball is above the paddle
            if(y < p.getTop()) {
                y = Math.min(y, p.getTop() - Ball.RADIUS);
            }
            else if(y > p.getBottom()) {
                y = Math.max(y, p.getBottom() + Ball.RADIUS);
            }
        }

        /**
         * Bounds sum of <code>angle</code> and <code>angleChange</code> to the side of the
         * unit circle that <code>angle</code> is on.
         * @param angle The initial angle.
         * @param angleChange Amount to add to angle.
         * @return bounded angle sum
         */
        protected double boundAngle(double angle, double angleChange) {
            return boundAngle(angle + angleChange, angle >= Math.PI);
        }

        protected double boundAngle(double angle) {
            return boundAngle(angle, angle >= Math.PI);
        }

        /**
         * Bounds an angle in radians to a subset of the top
         * or bottom part of the unit circle.
         * @param angle The angle in radians to bound.
         * @param top Flag which indicates if we should bound to the top or not.
         * @return the bounded angle
         */
        protected double boundAngle(double angle, boolean top) {
            if(top) {
//                return Math.max(Math.PI + BOUND, Math.min(2 * Math.PI - BOUND, angle));
                return Math.max(Math.PI, Math.min(2 * Math.PI, angle));
            }

//            return Math.max(BOUND, Math.min(Math.PI - BOUND, angle));
            return Math.min(Math.PI, angle);
        }


        /**
         * Given it a coordinate, it transforms it into a proper x-coordinate for the ball.
         * @param x, the x-coord to transform
         * @return
         */
        protected float keepX(float x) {
            return bound(x, Ball.RADIUS, getWidth() - Ball.RADIUS);
        }

        public static final double BOUND = Math.PI / 9;
        public static final float SPEED = 50.0f;
        public static final int RADIUS = 6;
        public static final double SALT = 4 * Math.PI / 9;
        private static final int ALIVE_TMR = 5;
    }

}
