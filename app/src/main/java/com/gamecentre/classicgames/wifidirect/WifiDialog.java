package com.gamecentre.classicgames.wifidirect;

import android.app.Dialog;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.pingpong.PingPongActivity;
import com.gamecentre.classicgames.utils.MessageRegister;

import java.util.ArrayList;
import java.util.List;

public class WifiDialog extends Dialog implements android.view.View.OnClickListener {
    public AppCompatActivity activity;
    public Dialog d;
    public Button searchBtn, no;
    ListView playerListView;
    ArrayAdapter adapter;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private List<String> peersName = new ArrayList<String>();


    public WifiDialog(AppCompatActivity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_wifi);

        playerListView = findViewById(R.id.search_list);
        adapter = new ArrayAdapter<String>(playerListView.getContext(),
                R.layout.peers_list_view, peersName);
        playerListView.setAdapter(adapter);
        playerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView conState = findViewById(R.id.connStatus);
                String peerName = (String) ((TextView) view).getText();
                WifiP2pDevice device = WifiDirectManager.getInstance().getDevice(peerName);
                if (device != null) {
//                    conState.setText("Connecting");
                    WifiDirectManager.getInstance().connect(conState);
                }
            }
        });

        WifiDirectManager.getInstance().registerDeviceView(adapter, peersName);

        searchBtn = findViewById(R.id.searchBtn);
        no = findViewById(R.id.wifiClose);
        searchBtn.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchBtn:
                WifiDirectManager.getInstance().cancelDisconnect();
                Log.d("Player Search", "Searching for players");
                TextView conState = findViewById(R.id.connStatus);
                conState.setText("Searching");
                WifiDirectManager.getInstance().discoverPeers();
                break;
            case R.id.wifiClose:
//                String message = MessageParser.getInstance().getMessage(1,2,3,4,5,6,7);
//                WifiDirectManager.getInstance().sendMessage(message);
//                if(!WifiDirectManager.getInstance().isConnected()) {
//                    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
//                    alert.setTitle("You need to connect to a player");
//
//                    alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                        }
//                    });
//
//                    alert.setNegativeButton("Cancel",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int whichButton) {
//                                    Intent intent = new Intent(activity, Pong.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                                    activity.startActivity(intent);
//                                    activity.finish();
//                                }
//                            });
//
//                    alert.show();
//                }
//                else{
//                    activity.getPongView().update(true);
//                    dismiss();
//                }
                MessageRegister.getInstance().registerWifiDialog();
                dismiss();
                break;
            default:
                break;
        }
    }
}
