package com.gamecentre.classicgames.connection;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.gamecentre.classicgames.model.Game;
import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.utils.MessageRegister;
import com.gamecentre.classicgames.utils.PlayerInfo;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ServerListenerThread extends Thread{

    private Socket hostThreadSocket;
    private boolean RUN = true;
    ObjectInputStream objectInputStream;
    InputStream inputStream = null;
    BufferedInputStream bis;

    ServerListenerThread(Socket soc) {
        hostThreadSocket = soc;
    }

    @Override
    public void run() {
        try{
            inputStream = hostThreadSocket.getInputStream();
            bis = new BufferedInputStream(inputStream);
            objectInputStream = new ObjectInputStream(bis);
        }catch (IOException e) {
            e.printStackTrace();
        }
        while (RUN) {

            try {
                Object gameObject;
                Bundle data = new Bundle();
                gameObject = objectInputStream.readObject();
                if (gameObject != null) {
                    if (gameObject instanceof PlayerInfo) {
//                        data.putSerializable(CONST.PLAYER_INFO, (PlayerInfo) gameObject);
//                        Log.d("SERVER LISTENER", "GOT PLAYER");
//                        data.putInt(Constants.ACTION_KEY, CONST.PLAYER_INFO.PLAYER_LIST_UPDATE);
//                        ServerConnectionThread.socketUserMap.put(hostThreadSocket, ((PlayerInfo) gameObject).username);
                    } else {
                        data.putSerializable(CONST.GAME_DATA_KEY, (Game) gameObject);
                        MessageRegister.getInstance().registerNewMessage((Game)gameObject);
//                        Log.d("SERVER LISTENER", "GOT GAME");
                    }
                    Message msg = new Message();
                    msg.setData(data);
                    WifiDirectManager.serverHandler.sendMessage(msg);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        RUN = false;
        if(hostThreadSocket != null) {
            try {
                hostThreadSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
