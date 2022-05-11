package com.gamecentre.classicgames.tank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.activity.MenuActivity;
import com.gamecentre.classicgames.connection.ClientConnectionThread;
import com.gamecentre.classicgames.connection.ServerConnectionThread;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.utils.WifiDialogListener;
import com.gamecentre.classicgames.wifidirect.WifiDialog;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class TankMenuActivity extends AppCompatActivity implements WifiDialogListener {

    TankTextView inviteTxt;

    public static final String TWO_PLAYERS = "two players";
    public static final String
            PREF_MUTED = "muted",
            PREF_VIBRATE = "vibrate",
            PREF_LEVEL = "level";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tank_menu);
        MessageRegister.getInstance().setwifiDialogListener(this);
        setListeners();
        inviteTxt = (TankTextView) findViewById(R.id.ivName);
        inviteTxt.setSelected(true);
    }

    protected void setListeners () {
        this.findViewById(R.id.tnkP1menu)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openStages(v, false);
                    }
                });

        this.findViewById(R.id.tnkP2menu)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if((WifiDirectManager.getInstance().isServer() && ServerConnectionThread.serverStarted) ||
                                (!WifiDirectManager.getInstance().isServer() && ClientConnectionThread.serverStarted)) {
                            openStages(v, true);
                        }
                        else {
                            Toast toast = Toast.makeText(TankMenuActivity.this.getApplicationContext(),
                                    "Invite a player first",
                                    Toast.LENGTH_SHORT);

                            ViewGroup group = (ViewGroup) toast.getView();
                            TextView messageTextView = (TextView) group.getChildAt(0);
                            messageTextView.setTextSize(20);

                            toast.show();
                        }
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

        this.findViewById(R.id.inviteBtn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        WifiDirectManager.getInstance().initialize(TankMenuActivity.this);
                        WifiDirectManager.getInstance().registerBReceiver();

                        WifiDialog wd = new WifiDialog(TankMenuActivity.this);
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(wd.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                        wd.show();
                        wd.getWindow().setAttributes(lp);
                    }
                });
    }

    public void startGame(boolean twoPlayers) {
        Intent i = new Intent(this, TankActivity.class);
        i.putExtra(TankMenuActivity.TWO_PLAYERS, twoPlayers);
        startActivity(i);
        finish();
    }

    public void openSettings(View view) {
        TankSettingsDialog cdd = new TankSettingsDialog(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(cdd.getWindow().getAttributes());
        cdd.show();
        cdd.getWindow().setAttributes(lp);
    }


    public void openStages(View view, boolean twoPlayers) {
        TankStageDialog cdd = new TankStageDialog(this, twoPlayers);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(cdd.getWindow().getAttributes());
        cdd.show();
        cdd.getWindow().setAttributes(lp);
    }

    @Override
    public void onWifiDilogClosed() {
//        this.getView().update(true);
        if(WifiDirectManager.getInstance().isServer() && ServerConnectionThread.serverStarted) {
            findViewById(R.id.inviteBtn).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.p2,null));
            inviteTxt = (TankTextView) findViewById(R.id.ivName);
            inviteTxt.setText(WifiDirectManager.getInstance().getDeviceName());
            inviteTxt.setSelected(true);

        }
        else if(!WifiDirectManager.getInstance().isServer() && ClientConnectionThread.serverStarted) {
            findViewById(R.id.inviteBtn).setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.p1,null));
            inviteTxt = (TankTextView) findViewById(R.id.ivName);
            inviteTxt.setText(WifiDirectManager.getInstance().getDeviceName());
            inviteTxt.setSelected(true);
        }
        else {
            inviteTxt.setText(R.string.invite_def_txt);
        }
    }
}