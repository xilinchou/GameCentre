package com.gamecentre.classicgames.tank;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.gamecentre.classicgames.R;

import java.util.Locale;

public class TankPauseGameDialog extends Dialog implements View.OnTouchListener{


    public AppCompatActivity activity;
    public Dialog d;
    TankView mTankView;

    private int state = 0;

    public TankTextView continueBtn, newGameBtn, endGameBtn;
    private TankTextView closeBtn;
    SharedPreferences settings;
    int games;


    public TankPauseGameDialog(AppCompatActivity a, TankView mTankView) {
        super(a);
        this.activity = a;
        this.mTankView = mTankView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        setContentView(R.layout.activity_tank_pause_game);
        continueBtn = (TankTextView) findViewById(R.id.continueBtn);
        newGameBtn = (TankTextView) findViewById(R.id.newGameBtn);
        endGameBtn = (TankTextView) findViewById(R.id.endGameBtn);

        continueBtn.setOnTouchListener(this);
        newGameBtn.setOnTouchListener(this);
        endGameBtn.setOnTouchListener(this);

        closeBtn = (TankTextView) findViewById(R.id.pauseCloseBtn);
        closeBtn.setOnTouchListener(this);

        settings = activity.getSharedPreferences("TankSettings", 0);
        games = settings.getInt(TankActivity.RETRY_COUNT,0);


        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                SharedPreferences.Editor editor;
                switch (state) {
                    case 0:
                        mTankView.resumeNoAds();
                        ((TankActivity)activity).enableControls();
                        break;
                    case 1:
                        games--;
                        editor = settings.edit();
                        editor.putInt(TankActivity.RETRY_COUNT,games);
                        editor.commit();
                        mTankView.resumeNoAds();
                        mTankView.retryStage();
                        break;
                    case 2:
                        games--;
                        editor = settings.edit();
                        editor.putInt(TankActivity.RETRY_COUNT,games);
                        editor.commit();
                        ((TankActivity)activity).endGame();
                        break;
                }
            }
        });


    }



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent m) {

        if(m.getAction() == MotionEvent.ACTION_DOWN){
            switch (v.getId()) {
                case R.id.continueBtn:
                case R.id.pauseCloseBtn:
                    state = 0;
                    dismiss();
                    break;
                case R.id.newGameBtn:
                    if(games > 0) {
                        state = 1;
                        dismiss();
                    }
                    break;
                case R.id.endGameBtn:
                    state = 2;
                    dismiss();
                    break;
            }
        }
        return true;
    }
}
