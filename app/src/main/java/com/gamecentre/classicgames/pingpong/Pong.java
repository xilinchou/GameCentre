package com.gamecentre.classicgames.pingpong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.gamecentre.classicgames.R;

public class Pong extends AppCompatActivity{

    AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pong_menu);
        setListeners();
        activity = this;
    }

    public void openSettings(View view) {

        SettingsDialog cdd=new SettingsDialog(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(cdd.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        cdd.show();
        cdd.getWindow().setAttributes(lp);
    }

    protected void setListeners () {
        this.findViewById(R.id.title_btnNoPlayer)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startGame(false,false);
                    }
                });

        this.findViewById(R.id.title_btnOnePlayer)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startGame(false,true);
                    }
                });

        this.findViewById(R.id.title_btnTwoPlayer)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startGame(true,true);
                    }
                });
    }

    protected void startGame(boolean redPlayer, boolean bluePlayer) {
        Intent i = new Intent(this, PingPongActivity.class);
        i.putExtra(PingPongActivity.EXTRA_BLUE_PLAYER, bluePlayer);
        i.putExtra(PingPongActivity.EXTRA_RED_PLAYER, redPlayer);
        startActivity(i);
        finish();
    }


    public static final String
            PREF_BALL_SPEED = "ball_speed",
            PREF_STRATEGY = "strategy",
            PREF_LIVES = "lives",
            PREF_HANDICAP = "handicap",
            PREF_MUTED = "muted";

    public static final String
            KEY_AI_STRATEGY = "key_ai_strategy";
}