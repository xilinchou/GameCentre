package com.gamecentre.classicgames.tank;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gamecentre.classicgames.pingpong.PongView;
import com.gamecentre.classicgames.sound.SoundManager;
import com.gamecentre.classicgames.sound.Sounds;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.utils.WifiDialogListener;
import com.gamecentre.classicgames.wifidirect.WifiDialog;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

public class TankActivity extends AppCompatActivity implements View.OnTouchListener, WifiDialogListener {

    public Button upBtn, dwnBtn, rtBtn, lftBtn, shtBtn, menuBtn, nxtBtn;
    public LinearLayout enemyCount;
    public ImageView P1StatusImg, P2StatusImg, StageFlag;
    public TextView P1StatusTxt, P2StatusTxt, StageTxt;
    public TankTextView curtainTxt,gameOverTxt;

    public RelativeLayout scoreView;
    public TankTextView hiScore, stageScore, p1Score, p2Score;
    public TankTextView p1AScore, p1BScore, p1CScore, p1DScore;
    public TankTextView p2AScore, p2BScore, p2CScore, p2DScore;
    public TankTextView p1ACount, p1BCount, p1CCount, p1DCount, p1Count;
    public TankTextView p2ACount, p2BCount, p2CCount, p2DCount, p2Count;
    public FrameLayout boarder;

    public BrickTextView pauseBtn, endGameBtn;

    boolean twoPlayers = false;
    boolean first_start;

    Typeface typeface;


    private  TankView mTankView;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tank);
        MessageRegister.getInstance().setwifiDialogListener(this);

        mTankView = findViewById(R.id.tankView);
//        boarder = findViewById(R.id.boarder);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        boolean two_Players = b.getBoolean(TankMenuActivity.TWO_PLAYERS, false);
        mTankView.setPlayerControl(two_Players);
        if(two_Players) {
            // Two players
            WifiDirectManager.getInstance().initialize(this);
            twoPlayers = true;
        }
        else {
            twoPlayers = false;
        }

        shtBtn = findViewById(R.id.shootBtn);
        upBtn = findViewById(R.id.upBtn);
        dwnBtn = findViewById(R.id.downBtn);
        rtBtn = findViewById(R.id.rightBtn);
        lftBtn = findViewById(R.id.leftBtn);

        menuBtn = findViewById(R.id.menuBtn);
        nxtBtn = findViewById(R.id.nxtBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        endGameBtn = findViewById(R.id.endBtn);
        enemyCount = findViewById(R.id.enemyCount);

        shtBtn.setOnTouchListener(this);
        upBtn.setOnTouchListener(this);
        dwnBtn.setOnTouchListener(this);
        rtBtn.setOnTouchListener(this);
        lftBtn.setOnTouchListener(this);

        menuBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent i = new Intent(TankActivity.this, TankMenuActivity.class);
                TankActivity.this.startActivity(i);
                TankActivity.this.finish();
                return true;
            }
        });

        nxtBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(TankView.gameover) {
                    mTankView.retryStage();
                }
                else if(TankView.stageComplete) {
                    TankView.mNewRound = true;
                }
                return true;
            }
        });


        endGameBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent i = new Intent(TankActivity.this, TankMenuActivity.class);
                TankActivity.this.startActivity(i);
                TankActivity.this.finish();
                return true;
            }
        });

        pauseBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mTankView.pause();
                    SoundManager.playSound(Sounds.TANK.PAUSE);
                }
                return true;
            }
        });


        enableControls();

        P1StatusImg = findViewById(R.id.P1StautusImg);
        P2StatusImg = findViewById(R.id.P2StautusImg);
        P1StatusTxt = findViewById(R.id.P1StautusTxt);
        P2StatusTxt = findViewById(R.id.P2StautusTxt);
        curtainTxt = findViewById(R.id.curtainText);
        gameOverTxt = findViewById(R.id.gameOverText);

        StageFlag = findViewById(R.id.stageImg);
        StageTxt = findViewById(R.id.stageTxt);

        typeface = Typeface.createFromAsset(getAssets(),"prstartk.ttf");
        P1StatusTxt.setTypeface(typeface);
        P1StatusTxt.setTextSize(8);
        P1StatusTxt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        P2StatusTxt.setTypeface(typeface);
        P2StatusTxt.setTextSize(8);
        P2StatusTxt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        StageTxt.setTypeface(typeface);
        StageTxt.setTextSize(10);
        StageTxt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


        scoreView = findViewById(R.id.scoreView);
        hiScore = findViewById(R.id.HiScore);
        stageScore = findViewById(R.id.StageScore);

        p1Score = findViewById(R.id.P1Score);
        p2Score = findViewById(R.id.P2Score);

        p1AScore = findViewById(R.id.enemyAScoreP1);
        p1BScore = findViewById(R.id.enemyBScoreP1);
        p1CScore = findViewById(R.id.enemyCScoreP1);
        p1DScore = findViewById(R.id.enemyDScoreP1);

        p2AScore = findViewById(R.id.enemyAScoreP2);
        p2BScore = findViewById(R.id.enemyBScoreP2);
        p2CScore = findViewById(R.id.enemyCScoreP2);
        p2DScore = findViewById(R.id.enemyDScoreP2);

        p1ACount = findViewById(R.id.enemyACountP1);
        p1BCount = findViewById(R.id.enemyBCountP1);
        p1CCount = findViewById(R.id.enemyCCountP1);
        p1DCount = findViewById(R.id.enemyDCountP1);
        p1Count = findViewById(R.id.totalCountP1);

        p2ACount = findViewById(R.id.enemyACountP2);
        p2BCount = findViewById(R.id.enemyBCountP2);
        p2CCount = findViewById(R.id.enemyCCountP2);
        p2DCount = findViewById(R.id.enemyDCountP2);
        p2Count = findViewById(R.id.totalCountP2);


        SoundManager.getInstance();
        SoundManager.initSounds(this);
        int[] sounds = {
                R.raw.tnkbackground,
                R.raw.tnkbonus,
                R.raw.tnkbrick,
                R.raw.tnkexplosion,
                R.raw.tnkfire,
                R.raw.tnkgameover,
                R.raw.tnkgamestart,
                R.raw.tnkscore,
                R.raw.tnksteel,
                R.raw.tnkpowerup,
                R.raw.tnkpause,
        };
        SoundManager.loadSounds(sounds);

        if(!twoPlayers) {
            mTankView.update();
        }

        first_start = true;
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        MessageRegister.getInstance().registerButtonAction(view, motionEvent);
        return false;
    }

    protected void onResume() {
        super.onResume();
        if(twoPlayers) {
            WifiDirectManager.getInstance().registerBReceiver();

            if(first_start){
                WifiDialog wd = new WifiDialog(this);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(wd.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                wd.show();
                wd.getWindow().setAttributes(lp);

                first_start = false;
            }
        }
        if(mTankView.isStarted() || !twoPlayers) {
            mTankView.resume();
        }
    }

    public void enableControls() {
        shtBtn.setEnabled(true);
        upBtn.setEnabled(true);
        dwnBtn.setEnabled(true);
        rtBtn.setEnabled(true);
        lftBtn.setEnabled(true);
    }

    public void disableControls() {
        shtBtn.setEnabled(false);
        upBtn.setEnabled(false);
        dwnBtn.setEnabled(false);
        rtBtn.setEnabled(false);
        lftBtn.setEnabled(false);
    }

    protected void onStop() {
        super.onStop();
        mTankView.stop();
    }

    public TankView getView() {
        return mTankView;
    }

    @Override
    public void onWifiDilogClosed() {
        this.getView().update(true);
    }

    protected void onDestroy() {
        super.onDestroy();
        mTankView.release();
        WifiDirectManager.getInstance().cancelDisconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(twoPlayers) {
            WifiDirectManager.getInstance().unregisterBReceiver();
        }
    }
}