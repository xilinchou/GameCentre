package com.gamecentre.classicgames.tank;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.utils.CONST;

@SuppressLint("ClickableViewAccessibility")
public class TankTypeActivity extends AppCompatActivity {
    TankTextView classic, arcade;
    static final String TANK_TYPE = "TANK_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tank_type);

        findViewById(R.id.classic).setOnClickListener(onClickListener);
        findViewById(R.id.classic_lo).setOnClickListener(onClickListener);
        findViewById(R.id.arcade).setOnClickListener(onClickListener);
        findViewById(R.id.arcade_lo).setOnClickListener(onClickListener);





//        classic.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    startActivity(new Intent(TankTypeActivity.this, TankMenuActivity.class));
//                    finish();
//                }
//                return true;
//            }
//        });
//
//        arcade.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    startActivity(new Intent(TankTypeActivity.this, TankMenuActivity.class));
//                    finish();
//                }
//                return true;
//            }
//        });

        this.findViewById(R.id.settingsBtn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openSettings(view);
                    }
                });


        TimerBroadcastService.settings = getSharedPreferences("TankSettings", 0);

        int games = TimerBroadcastService.settings.getInt(TankActivity.RETRY_COUNT, CONST.Tank.MAX_GAME_COUNT);
        if(games == CONST.Tank.MAX_GAME_COUNT) {
            SharedPreferences.Editor editor = TimerBroadcastService.settings.edit();
            editor.putInt(TankActivity.RETRY_COUNT,games);
            editor.commit();
        }


        long life_time = TimerBroadcastService.settings.getLong(TankActivity.LIFE_TIME,0);
        if(life_time == 0) {
            SharedPreferences.Editor editor = TimerBroadcastService.settings.edit();
            editor.putLong(TankActivity.LIFE_TIME,life_time);
            editor.commit();
        }
        else {
            long current_time = System.currentTimeMillis();
            long time_passed = current_time - life_time;
            int added_games = (int)(time_passed/(CONST.Tank.LIFE_DURATION_MINS*60000));
            games += added_games;
            if(games > CONST.Tank.MAX_GAME_COUNT) {
                games = CONST.Tank.MAX_GAME_COUNT;
            }
            SharedPreferences.Editor editor = TimerBroadcastService.settings.edit();
            editor.putInt(TankActivity.RETRY_COUNT,games);
            editor.commit();
        }



        startService(new Intent(TankTypeActivity.this, TimerBroadcastService.class));
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(TankTypeActivity.this, TankMenuActivity.class);
            switch (view.getId()) {
                case R.id.classic:
                case R.id.classic_lo:
                    i.putExtra(TankTypeActivity.TANK_TYPE, "CLASSIC");
                    startActivity(i);
                    finish();
                    break;
                case R.id.arcade:
                case R.id.arcade_lo:
                    i.putExtra(TankTypeActivity.TANK_TYPE, "ARCADE");
                    startActivity(i);
                    finish();
                    break;
                case R.id.construct:
                    break;
            }
        }
    };

    public void openSettings(View view) {
        TankSettingsDialog cdd = new TankSettingsDialog(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(cdd.getWindow().getAttributes());
        cdd.show();
        cdd.getWindow().setAttributes(lp);
    }
}