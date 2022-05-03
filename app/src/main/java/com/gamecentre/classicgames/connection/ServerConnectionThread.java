package com.gamecentre.classicgames.connection;

import android.util.Log;

import com.gamecentre.classicgames.utils.CONST;
import com.gamecentre.classicgames.wifidirect.WifiDirectManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnectionThread extends Thread{

    static final int SocketServerPORT = 8080;
    public static Socket socket = null;
    public static boolean serverStarted = false;
    public static ServerSocket serverSocket;
    public static boolean allPlayersJoined = false;
    public static ServerListenerThread socketListener;

    public ServerConnectionThread() {

    }

    @Override
    public void run() {
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                serverStarted = true;
                socket = serverSocket.accept();
                Log.d("SERVER CONNECTION", "CONNECTED");
                socketListener = new ServerListenerThread(socket);
                socketListener.start();
                WifiDirectManager.svSender = new ServerSenderThread(socket, CONST.GAME_NAME);
                WifiDirectManager.svSender.start();
                ServerSenderThread sendGameName = new ServerSenderThread(socket, CONST.GAME_NAME);
                sendGameName.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
