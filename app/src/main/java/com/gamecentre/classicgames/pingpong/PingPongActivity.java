package com.gamecentre.classicgames.pingpong;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.utils.WifiDialogListener;
import com.gamecentre.classicgames.wifidirect.WifiDialog;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

public class PingPongActivity extends AppCompatActivity implements WifiDialogListener {

    private PongView mPongView;
    private AlertDialog mAboutBox;

    boolean first_start;
    boolean p2 = false;

//    protected PowerManager.WakeLock mWakeLock;

    public static final String
            EXTRA_RED_PLAYER = "red-is-player",
            EXTRA_BLUE_PLAYER = "blue-is-player";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.pong_view);
        mPongView = findViewById(R.id.pong);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        boolean redP = b.getBoolean(EXTRA_RED_PLAYER, false);
        boolean blueP = b.getBoolean(EXTRA_BLUE_PLAYER, false);
        mPongView.setPlayerControl(redP,blueP);
        if(redP && blueP) {
            // Two players
            WifiDirectManager.getInstance().initialize(this);
            p2 = true;
        }
        if(!p2) {
            mPongView.update();
        }
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

//        final PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Pong:WAKELOCK");
//        mWakeLock.acquire();

        first_start = true;
    }



    protected void onStop() {
        super.onStop();
        mPongView.stop();
    }

    protected void onResume() {
        super.onResume();
        if(p2) {
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
        if(mPongView.isStarted() || !p2) {
            mPongView.resume();
        }
    }

    public PongView getView() {
        return mPongView;
    }

    protected void onDestroy() {
        super.onDestroy();
        mPongView.release();
        WifiDirectManager.getInstance().cancelDisconnect();
//        mWakeLock.release();
    }

//    public void hideAboutBox() {
//        if(mAboutBox != null) {
//            mAboutBox.hide();
//            mAboutBox = null;
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        if(p2) {
            WifiDirectManager.getInstance().unregisterBReceiver();
        }
    }

    @Override
    public void onWifiDilogClosed() {
        this.getView().update(true);
    }
}