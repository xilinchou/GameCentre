package com.gamecentre.classicgames.connection;

import com.gamecentre.classicgames.model.Game;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class ClientSenderThread extends Thread{

    private Socket hostThreadSocket;
//    Object message;
    public static boolean isActive = true;
    private boolean firstMsg = true;
    OutputStream outputStream;
    ObjectOutputStream objectOutputStream;
    BufferedOutputStream bos;
    private LinkedList<Object> messages;
    private boolean RUN = true;

    public ClientSenderThread(Socket socket, Object message) {
        messages = new LinkedList<>();
        hostThreadSocket = socket;
        this.messages.add(message);
    }

    public ClientSenderThread(Socket socket) {
        messages = new LinkedList<>();
        hostThreadSocket = socket;
    }


    @Override
    public void run() {

        if (hostThreadSocket.isConnected()) {
            try {
                if (isActive) {
                    outputStream = hostThreadSocket.getOutputStream();
                    bos = new BufferedOutputStream(outputStream);
                    objectOutputStream = new ObjectOutputStream(bos);

                    while(RUN) {
                        while (!messages.isEmpty()) {
                            objectOutputStream.writeObject(messages.removeFirst());
                            objectOutputStream.flush();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void sendMessage(Object message) {
        messages.add(message);
    }

    public void disconnect() {
        RUN = false;
        if(objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
