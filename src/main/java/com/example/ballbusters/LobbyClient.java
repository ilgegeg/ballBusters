package com.example.ballbusters;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class LobbyClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Consumer<String> messageHandler;

    public LobbyClient(String host, int port, Consumer<String> handler) {
        this.messageHandler = handler;

        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true); // AUTO-FLUSH

        } catch (IOException e) {
            System.err.println("Impossibile connettersi al server");
            e.printStackTrace();
            return;
        }

        Thread listener = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    String finalMsg = msg;
                    Platform.runLater(() -> messageHandler.accept(finalMsg));
                }
            } catch (IOException e) {
                System.out.println("Connessione al server chiusa");
            }
        });

        listener.setDaemon(true);
        listener.start();
    }

    private void listen() {
        Thread listener = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    String finalMsg = msg;
                    Platform.runLater(() -> messageHandler.accept(finalMsg));
                }
            } catch (IOException e) {
                System.out.println("Connessione al server chiusa");
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        } else {
            System.err.println("Client non connesso, messaggio non inviato");
        }
    }
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}
