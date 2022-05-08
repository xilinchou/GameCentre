package com.gamecentre.classicgames.connection;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ClientSenderThread extends Thread{

    private final Socket hostThreadSocket;
//    Object message;
    public static boolean isActive = true;
    private boolean firstMsg = true;
    private OutputStream os;
    private ObjectOutputStream oos;
    private BufferedOutputStream bos;
    private final LinkedList<Object> messages;
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
                    os = hostThreadSocket.getOutputStream();
                    bos = new BufferedOutputStream(os);
                    oos = new ObjectOutputStream(os);

                    while(RUN) {
                        while (!messages.isEmpty()) {
                            oos.writeObject(messages.removeFirst());
                            oos.flush();
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
        if(oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
