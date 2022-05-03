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
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.utils.WifiDialogListener;
import com.gamecentre.classicgames.wifidirect.WifiDialog;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

public class TankActivity extends AppCompatActivity implements View.OnTouchListener, WifiDialogListener {

    public static Button upBtn, dwnBtn, rtBtn, lftBtn, shtBtn;
    public static LinearLayout enemyCount;
    public static ImageView P1StatusImg, P2StatusImg, StageFlag;
    public static TextView P1StatusTxt, P2StatusTxt, StageTxt;
    public static FrameLayout gameFrame;

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

//        gameFrame = findViewById(R.id.gameFrame);

        mTankView = findViewById(R.id.tankView);

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
        enemyCount = findViewById(R.id.enemyCount);

//        navView.setOnTouchListener(this);
        shtBtn.setOnTouchListener(this);
        upBtn.setOnTouchListener(this);
        dwnBtn.setOnTouchListener(this);
        rtBtn.setOnTouchListener(this);
        lftBtn.setOnTouchListener(this);

        P1StatusImg = findViewById(R.id.P1StautusImg);
        P2StatusImg = findViewById(R.id.P2StautusImg);
        P1StatusTxt = findViewById(R.id.P1StautusTxt);
        P2StatusTxt = findViewById(R.id.P2StautusTxt);

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
                R.raw.tnksteel
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
//                WifiDirectManager.getInstance().showDeviceWindow(this);
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