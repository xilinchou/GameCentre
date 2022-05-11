package com.gamecentre.classicgames.tank;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.gamecentre.classicgames.R;
import com.gamecentre.classicgames.connection.ClientConnectionThread;
import com.gamecentre.classicgames.connection.ServerConnectionThread;
import com.gamecentre.classicgames.model.Game;
import com.gamecentre.classicgames.model.TankGameModel;
import com.gamecentre.classicgames.utils.CVTR;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.utils.RemoteMessageListener;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

public class TankStageDialog extends Dialog implements android.view.View.OnClickListener, RemoteMessageListener {
    CheckBox soundCheck;
    CheckBox vibrateCheck;

    public AppCompatActivity activity;
    public Dialog d;
//    public Button yes, no;
    SharedPreferences settings;
    boolean twoPlayers;

    GridLayout stageBtns;

    public TankStageDialog(AppCompatActivity a, boolean twoPlayers) {
        super(a);
        this.activity = a;
        this.twoPlayers = twoPlayers;
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

        setContentView(R.layout.tank_stages);
        MessageRegister.getInstance().setMsgListener(this);
        float SCALE = activity.getResources().getDisplayMetrics().density;
        stageBtns = (GridLayout) findViewById(R.id.stage_grid);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams((int) CVTR.toDp(80), (int) CVTR.toDp(80));
        cardParams.setMargins((int)CVTR.toDp(20),(int)CVTR.toDp(20),0,0);


        LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        txtParams.setMargins(0,0,0,0);

        settings = activity.getSharedPreferences("TankSettings", 0);
        int unlockLevel = settings.getInt(TankMenuActivity.PREF_LEVEL,1);


        for(int i = 1 ; i <= 35; i++) {
            CardView card = new CardView(this.getContext());
            card.setLayoutParams(cardParams);
            card.setCardBackgroundColor(Color.TRANSPARENT);
            ImageView img = new ImageView(this.getContext());

            if(i <= unlockLevel) {
                img.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.unlocked,null));
            }
            else {
                img.setBackground(ResourcesCompat.getDrawable(activity.getResources(),R.drawable.locked,null));
            }
            card.setRadius((int)CVTR.toDp(10));
            TankTextView stg = new TankTextView(this.getContext());
            stg.setText(String.valueOf(i));
            stg.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            stg.setTextSize(CVTR.toDp(10));
            stg.setBackgroundColor(Color.TRANSPARENT);

            stg.setLayoutParams(txtParams);
            int h = (int)((card.getLayoutParams().height - stg.getTextSize() )/2);
            txtParams.setMargins(0,h,0,0);
            stg.setLayoutParams(txtParams);
            card.addView(img);
            card.addView(stg);
            if(i <= unlockLevel) {
                card.setOnClickListener(this);
            }
            card.setTag(i);
            stageBtns.addView(card);
        }


    }

    @Override
    public void onClick(View v) {
        int level = (int)(v.getTag());

        if(twoPlayers && !WifiDirectManager.getInstance().isServer() && ClientConnectionThread.serverStarted) {

            Toast toast = Toast.makeText(activity.getApplicationContext(),
                    "Wait for player 1 to select stage!",
                    Toast.LENGTH_SHORT);

            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(25);

            toast.show();
            return;
        }
        else if(twoPlayers && WifiDirectManager.getInstance().isServer() && ServerConnectionThread.serverStarted) {
            TankGameModel model = new TankGameModel();
            model.mlevelInfo = true;
            model.mlevel = level;
            WifiDirectManager.getInstance().sendMessage(model);
        }

        TankView.level = level;
        ((TankMenuActivity)activity).startGame(twoPlayers);
    }

    @Override
    public void onMessageReceived(Game message) {
        if(message instanceof TankGameModel) {
            TankGameModel msg = (TankGameModel)message;
            if(msg.mlevelInfo) {
                TankView.level = msg.mlevel;
                dismiss();
                ((TankMenuActivity) activity).startGame(twoPlayers);
            }

        }
    }
}