package com.example.ballbusters;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class LobbyClient {

    private PrintWriter out;

    public LobbyClient(String host, int port, Consumer<String> handler) {
        try {
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        String finalMsg = msg;
                        Platform.runLater(() -> handler.accept(finalMsg));
                    }
                } catch (IOException ignored) {}
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        out.println(msg);
    }
}

