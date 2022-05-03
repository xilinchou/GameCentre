package com.gamecentre.classicgames.utils;

import android.view.MotionEvent;
import android.view.View;

import com.gamecentre.classicgames.model.Game;

public class MessageRegister {

    private static final MessageRegister instance = new MessageRegister();
    private  RemoteMessageListener msgListener;
    private ButtonListener btnListener;
    private WifiDialogListener wdListener;

    public static MessageRegister getInstance() {
        return instance;
    }

    public void setMsgListener(RemoteMessageListener l) {
        msgListener = l;
    }

    public void setButtonListener(ButtonListener l) {btnListener = l;}

    public void setwifiDialogListener(WifiDialogListener l) {wdListener = l;}

    public void registerNewMessage(Game message) {
        msgListener.onMessageReceived(message);
    }

    public void registerButtonAction(View v, MotionEvent m) {btnListener.onButtonPressed(v,m);}

    public void registerWifiDialog() {
        wdListener.onWifiDilogClosed();
    }
}
