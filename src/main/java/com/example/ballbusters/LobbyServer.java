package com.example.ballbusters;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyServer {

    private static final int PORT = 5000;
    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;

    private final Map<Integer, ClientHandler> clients = new HashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final Random random = new Random();

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server avviato");

                while (true) {
                    Socket socket = serverSocket.accept();
                    int id = idCounter.getAndIncrement();

                    double x = random.nextInt(WIDTH - 120);
                    double y = random.nextBoolean() ? 40 : HEIGHT - 60;
                    String color = randomColor();

                    ClientHandler client =
                            new ClientHandler(socket, id, x, y, color);
                    clients.put(id, client);
                    new Thread(client).start();

                    broadcast("SPAWN:" + id + ":" + x + ":" + y + ":" + color);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void broadcast(String msg) {
        for (ClientHandler c : clients.values()) {
            c.out.println(msg);
        }
    }

    private String randomColor() {
        String[] c = {
                "CYAN","HOTPINK","LIME","ORANGE",
                "YELLOW","VIOLET","AQUA"
        };
        return c[random.nextInt(c.length)];
    }

    class ClientHandler implements Runnable {
        int id;
        PrintWriter out;
        BufferedReader in;

        ClientHandler(Socket socket, int id,
                      double x, double y, String color) {
            this.id = id;
            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(
                        socket.getOutputStream(), true);

                out.println("ID:" + id);
                out.println("SPAWN:" + id + ":" + x + ":" + y + ":" + color);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    broadcast(msg);
                }
            } catch (IOException ignored) {
            } finally {
                clients.remove(id);
            }
        }
    }
}
