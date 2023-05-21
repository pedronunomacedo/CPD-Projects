package server;

import client.ClientHandler;
import services.NewConnectionsHandler;
import utils.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Server {
    private String gameMode;
    private int port;
    private ServerSocketChannel serverSocket;
    public static CustomExecutorService newConnectionsThreadPool;
    public static CustomExecutorService gamesThreadPool;
    public static ArrayList<ClientHandler> waitingQueue = new ArrayList<>(Constants.MAX_WAITING_PLAYERS); // define waiting queue as an instance variable
    private CustomScheduledExecutor scheduler;
    public static ReentrantLock waitingQueueLock = new ReentrantLock();
    public static ArrayList<Game> activeGames;
    public static ArrayList<ClientHandler> disconnectedClients = new ArrayList<>();


    public Server(String mode, int port) throws IOException {
        this.gameMode = mode;
        this.port = port;
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.socket().bind(new InetSocketAddress(port));
        newConnectionsThreadPool = new CustomExecutorService(Constants.MAX_WAITING_PLAYERS);
        gamesThreadPool = new CustomExecutorService(Constants.MAX_NUM_GAMES);
        this.scheduler = new CustomScheduledExecutor();
        activeGames = new ArrayList<>();
        System.out.println("Server started on port " + port);
    }

    private void periodicalwaitingQueueCheck() {
        // Schedule a task to check the waiting queue for a sufficient number of clients every 5 seconds
        this.scheduler.scheduleAtFixedRate(() -> {
            waitingQueueLock.lock();
            try {
                if (waitingQueue.size() >= Constants.PLAYERS_PER_GAME) {
                    System.out.println("Starting a new game with " + Constants.PLAYERS_PER_GAME + " players");

                    // Remove the waiting players from the waiting queue and add them to a list of game players
                    ArrayList<ClientHandler> gamePlayers = new ArrayList<>(Constants.PLAYERS_PER_GAME);
                    for (int i = 0; i < Constants.PLAYERS_PER_GAME; i++) {
                        gamePlayers.add(waitingQueue.get(0));
                        waitingQueue.remove(0);
                    }

                    // Create a new game instance and add it to the list of active games
                    System.out.println("Creating instance of the game!");
                    Game game = new Game(gamePlayers);
                    activeGames.add(game);
                    System.out.println("Starting the instance of the game");
                    gamesThreadPool.submit(game);
                }
            } finally {
                waitingQueueLock.unlock();
            }
        }, 0, 5, TimeUnits.SECONDS);
    }

    public void start() {
        periodicalwaitingQueueCheck();
        try {
            while (true) {
                SocketChannel clientSocketChannel = this.serverSocket.accept(); // accept client connection
                System.out.println("A new client has connected!");
                NewConnectionsHandler clientConnectionHandler = new NewConnectionsHandler(clientSocketChannel); // each object of this class will be responsible to communicate with the client
                newConnectionsThreadPool.submit(clientConnectionHandler); // run the new clientConnectionHandler
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static synchronized void addClient(ClientHandler client) throws InterruptedException {
        synchronized (waitingQueue) {
            if (waitingQueue.stream().noneMatch(c -> c.client.getUsername().equals(client.client.getUsername()))) {
                waitingQueue.add(client);
                client.client.setIsActive(true);
                System.out.println("There are " + waitingQueue.size() + " clients waiting (" + String.join(",", waitingQueue.stream().map(cl -> cl.client.getUsername()).collect(Collectors.joining(", "))) + ")!");
            }
        }
    }

    public static synchronized void addDisconnectedClient(ClientHandler client) {
        synchronized (disconnectedClients) {
            disconnectedClients.add(client);
        }
    }

    public static ClientHandler getClientHandlerByUsername(String username) {
        for (ClientHandler clientHandler : waitingQueue) {
            if (clientHandler.client.getUsername().equals(username)) {
                return clientHandler;
            }
        }

        return null; // Return null if no matching client handler is found
    }
}