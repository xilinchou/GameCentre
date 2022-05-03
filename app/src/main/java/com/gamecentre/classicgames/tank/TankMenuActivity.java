package com.gamecentre.classicgames.tank;

import androidx.appcompat.app.AppCompatActivity;
import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.activity.MenuActivity;
import com.gamecentre.classicgames.pingpong.PingPongActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class TankMenuActivity extends AppCompatActivity {

    public static final String TWO_PLAYERS = "two players";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tank_menu);
        setListeners();
    }

    protected void setListeners () {
        this.findViewById(R.id.tnkP1menu)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startGame(false);
                    }
                });

        this.findViewById(R.id.tnkP2menu)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startGame(true);
                    }
                });

        this.findViewById(R.id.tnkExitmenu)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(TankMenuActivity.this, MenuActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
    }

    protected void startGame(boolean twoPlayers) {
        Intent i = new Intent(this, TankActivity.class);
        i.putExtra(TankMenuActivity.TWO_PLAYERS, twoPlayers);
        startActivity(i);
        finish();
    }
}