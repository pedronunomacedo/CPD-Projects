package services;

import client.ClientHandler;
import server.Server;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class NewConnectionsHandler extends Thread {
    private SocketChannel clientSocketChannel;

    public NewConnectionsHandler(SocketChannel clientSocketChannel) throws IOException {
        this.clientSocketChannel = clientSocketChannel;
    }
    @Override
    public void run() {
        Authentication auth;
        boolean addToQueue;
        try {
            auth = new Authentication(clientSocketChannel); // Authenticate the client
            addToQueue = auth.startAuthentication();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            throw new RuntimeException(e);
        }


        if (addToQueue) {
            // Once authenticated, add the client to a waiting queue for a game
            try {
                ClientHandler clientHandler = new ClientHandler(clientSocketChannel, auth.client);
                Server.waitingQueueLock.lock();
                try {
                    Server.addClient(clientHandler);
                } finally {
                    Server.waitingQueueLock.unlock();
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
