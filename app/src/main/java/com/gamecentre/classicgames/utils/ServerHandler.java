package com.gamecentre.classicgames.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.gamecentre.classicgames.connection.ServerConnectionThread;
import com.gamecentre.classicgames.connection.ServerSenderThread;
import com.gamecentre.classicgames.model.Game;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

import java.net.Socket;

public class ServerHandler extends Handler {

    Bundle messageData;

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        messageData = msg.getData();
        Object serverObject = messageData.getSerializable(CONST.GAME_DATA_KEY);
        if (serverObject instanceof Game) {
            // TODO
            MessageRegister.getInstance().registerNewMessage((Game)serverObject);
        }
    }

    public static void sendToClient(Object gameObject) {
        WifiDirectManager.svSender.sendMessage(gameObject);
    }
}
