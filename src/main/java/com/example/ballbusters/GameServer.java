package com.example.ballbusters;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {

    private static final int PORT = 6000;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        new GameServer().start();
    }

    public void start() {
        System.out.println("Game server avviato sulla porta " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String msg) {
        for (ClientHandler c : clients) {
            c.send(msg);
        }
    }

    private class ClientHandler extends Thread {
        Socket socket;
        PrintWriter out;
        BufferedReader in;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        void send(String msg) {
            out.println(msg);
        }

        public void run() {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    broadcast(msg); // invia a tutti
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
