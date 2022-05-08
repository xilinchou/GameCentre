package com.gamecentre.classicgames.connection;

import android.util.Log;

import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.utils.PlayerInfo;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClientConnectionThread extends Thread{

    public static Socket socket;
    String dstAddress;
    int dstPort = 8080;
    public static boolean serverStarted = false;
    String userName;
    public static ClientListenerThread clientListener;

    public ClientConnectionThread(String userName, String dstAddress) {
        this.userName = userName;
        this.dstAddress = dstAddress;
    }

    public ClientConnectionThread(String dstAddress) {
        this.userName = null;
        this.dstAddress = dstAddress;
    }

    @Override
    public void run() {
        if (socket == null) {
            try {
                if (dstAddress != null) {
                    socket = new Socket(dstAddress, dstPort);
                    if (socket.isConnected()) {
                        Log.d("CLIENT CONNECTION", "CONNECTED");
                        serverStarted = true;
                        clientListener = new ClientListenerThread(socket);
                        clientListener.start();
                        PlayerInfo playerInfo = new PlayerInfo(userName);
                        WifiDirectManager.clSender = new ClientSenderThread(socket, playerInfo);
                        WifiDirectManager.clSender.start();
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
