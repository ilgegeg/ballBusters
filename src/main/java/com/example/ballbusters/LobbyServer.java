package com.example.ballbusters;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LobbyServer {

    private static final int PORT = 5000;
    private static final int MAX_PLAYERS = 6;

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private ClientHandler host;

    public static void main(String[] args) {
        new LobbyServer().start();
    }

    public void start() {
        System.out.println("Lobby server avviato su porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();

                if (clients.size() >= MAX_PLAYERS) {
                    new PrintWriter(socket.getOutputStream(), true)
                            .println("FULL");
                    socket.close();
                    continue;
                }

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

    private void broadcastPlayers() {
        StringBuilder sb = new StringBuilder("PLAYERS:");
        for (ClientHandler c : clients) {
            sb.append(c.name).append(",");
        }
        broadcast(sb.substring(0, sb.length() - 1));
    }

    private class ClientHandler extends Thread {

        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String name;
        private boolean isHost = false;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        void send(String msg) {
            out.println(msg);
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String msg;
                while ((msg = in.readLine()) != null) {

                    if (msg.startsWith("JOIN:")) {
                        name = msg.substring(5);

                        if (host == null) {
                            host = this;
                            isHost = true;
                            send("ROLE:HOST");
                        } else {
                            send("ROLE:CLIENT");
                        }

                        broadcastPlayers();
                    }

                    if (msg.equals("START")) {
                        if (this == host) {
                            System.out.println("Partita avviata dall'host");
                            broadcast("START");
                        } else {
                            send("ERROR:NOT_HOST");
                        }
                    }
                }
            } catch (IOException ignored) {
            } finally {
                clients.remove(this);

                if (this == host) {
                    host = clients.isEmpty() ? null : clients.get(0);
                    if (host != null) {
                        host.isHost = true;
                        host.send("ROLE:HOST");
                    }
                }

                broadcastPlayers();

                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
