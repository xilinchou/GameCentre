package com.gamecentre.classicgames.tank;

import androidx.appcompat.app.AppCompatActivity;
import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.activity.MenuActivity;
import com.gamecentre.classicgames.pingpong.SettingsDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class TankMenuActivity extends AppCompatActivity {

    public static final String TWO_PLAYERS = "two players";
    public static final String
            PREF_MUTED = "muted",
            PREF_VIBRATE = "vibrate";


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
                        TankMenuActivity.this.startActivity(i);
                        TankMenuActivity.this.finish();
                    }
                });

        this.findViewById(R.id.settingsBtn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openSettings(view);
                    }
                });
    }

    protected void startGame(boolean twoPlayers) {
        Intent i = new Intent(this, TankActivity.class);
        i.putExtra(TankMenuActivity.TWO_PLAYERS, twoPlayers);
        startActivity(i);
        finish();
    }

    public void openSettings(View view) {
        LinearLayout settingsDialog = findViewById(R.id.settings_dialog);
        TankSettingsDialog cdd = new TankSettingsDialog(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(cdd.getWindow().getAttributes());
//        lp.width = settingsDialog.getWidth();//WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = settingsDialog.getHeight();//WindowManager.LayoutParams.MATCH_PARENT;
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        cdd.show();
        cdd.getWindow().setAttributes(lp);
    }
}