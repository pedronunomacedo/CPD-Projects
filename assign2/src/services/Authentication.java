package services;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import client.ClientHandler;
import models.ClientModel;
import server.Game;
import server.Server;
import utils.Constants;
import utils.Handlers;
import utils.Tcp;

public class Authentication {
    private String address;
    private int port;
    private ClientsDatabase usersDB;
    private SocketChannel clientSocket;
    public ClientModel client;

    public Authentication(SocketChannel clientChannel) throws IOException {
        this.usersDB = new ClientsDatabase(Constants.USERS_DB);
        this.clientSocket = clientChannel;
    }

    private static String mainMenuOptions() {
        String mainMenu = new String();
        mainMenu += "1. Login\n";
        mainMenu += "2. Register\n";
        mainMenu += "3. Reconnect\n";
        mainMenu += "4. Quit\n";
        mainMenu += "Enter choice: ";

        return mainMenu;
    }

    public boolean login(String type) throws IOException {
        boolean validCredentials = false;

        while (!validCredentials) {
            // Prompt the client to input their username
            String prompt = "Enter your username: ";
            this.writeMessage(prompt);
            String username = this.readMessage(); // Read the username from the client

            // Prompt the client to input their password
            prompt = "Enter your password: ";
            this.writeMessage(prompt);
            String password = this.readMessage(); // Read the password from the client

            // Check if the username and password are valid
            this.client = this.usersDB.getUser(username);

            if (this.client == null) { // Username not found
                this.writeMessage(Tcp.TCP_NOT_FOUND + ": Username not found!");
            } else if (password.equals(this.client.getPassword())) { // Passwords match
                Server.waitingQueueLock.lock();
                try {
                    if (Server.waitingQueue.stream().anyMatch(chandler -> chandler.client.getUsername().equals(this.client.getUsername())) && type.equals("login")) { // Username already signed up (only for the login part - in the reconnection part it client is already on the waiting queue)
                        this.writeMessage(Tcp.TCP_CONFLICT + ": User " + this.client.getUsername() + " already signed up. Please use a different username!");
                    } else { // Authentication succeeded
                        this.writeMessage(Tcp.TCP_OK + ": Authentication successful!");
                        this.usersDB.updateToken(this.client.getUsername(), Handlers.generateToken());
                        this.client.setIsActive(true);
                        this.client.setLastToken(Handlers.generateToken());
                        validCredentials = true;
                    }
                } finally {
                    Server.waitingQueueLock.unlock();
                }
            } else { // Authentication failed
                this.writeMessage(Tcp.TCP_INTERNAL_ERROR + ": Authentication failed. Please try again!");
            }
        }

        return true;
    }

    public boolean register() throws IOException {
        boolean validCredentials = false;

        while (!validCredentials) {
            // Prompt the client to input their username
            String prompt = "Enter new username: ";
            this.writeMessage(prompt);
            String newUsername = this.readMessage(); // Read the username from the client

            // Prompt the client to input their password
            prompt = "Enter new password: ";
            this.writeMessage(prompt);
            String newPassword = this.readMessage(); // Read the password from the client

            // Check if the username and password are valid
            this.client = this.usersDB.getUser(newUsername);
            if (this.client == null) { // Username not found (username available)
                this.client = this.usersDB.registerNewClient(newUsername, newPassword);
                if (this.client != null)  {
                    validCredentials = true;
                    this.writeMessage(Tcp.TCP_CREATED + " : Registration successful!");
                }
            } else {
                // Username already exists
                this.writeMessage(Tcp.TCP_CONFLICT + " : Authentication failed. Please try again!");
            }
        }
        System.out.println("New client registered!");

        return true;
    }

    public boolean reconnect() throws IOException {
        System.out.println("Trying to reconnect a client");
        boolean authStop = this.login("reconnect"); // Ask the user for the username and password (assigns the user info to the client class field)

        // Handle the token received!
        String tokenReceived = this.readMessage(); // Read the token from the client side
        boolean isValid = Handlers.sessionTokenIsValid(this.client, tokenReceived);

        if (!isValid) {
            System.out.println("Session token is not valid!");
            this.writeMessage(Tcp.TCP_FORBIDDEN + " : Session token is invalid!");
            return false;
        }

        System.out.println("Session token is valid!");

        try {
            Server.waitingQueueLock.lock();

            ClientHandler clientHandler = Server.getClientHandlerByUsername(this.client.getUsername());
            if (clientHandler != null) { // clients on the waiting queue
                clientHandler.setSocket(this.clientSocket); // just substitute the client socket for the new one
            }
        } finally {
            Server.waitingQueueLock.lock();
        }


        // Check if the client is in the waiting queue (if it is, don't do nothing and let the client on waiting queue, otherwise it means that he's in an active game)
        for (Game actGame : Server.activeGames) {
            for (var obj : actGame.getClients().entrySet()) {
                if (obj.getKey().client.getUsername().equals(this.client.getUsername()) && !obj.getValue()) {
                    obj.getKey().setSocket(this.clientSocket);
                    obj.setValue(true);

                    ClientHandler clientDisc = Server.disconnectedClients.stream()
                            .filter(c -> c.client.getUsername().equals(this.client.getUsername()))
                            .findFirst()
                            .orElse(null);

                    Server.disconnectedClients.remove(clientDisc); // remove the client from the disconnected client list
                    Server.waitingQueue.remove(clientDisc);
                    this.writeMessage(Tcp.TCP_OK + " : Reconnection made successfully");

                    return false; // don't add to the waiting queue
                }
            }
        }

        return true;
    }

    public boolean startAuthentication() throws IOException {
        this.writeMessage(mainMenuOptions());
        String userInput = this.readMessage(); // Read the main menu user option
        boolean authStop = false;

        while (!authStop) {
            switch (Integer.parseInt(userInput)) {
                case 1 : // login
                    authStop = this.login("login");
                    break;
                case 2: // Register
                    authStop = this.register();
                    break;
                case 3: // Reconnect
                    return this.reconnect();
                case 4:
                    this.clientSocket.close();
                    System.exit(1);
                    return false;
            }
        }

        return true;
    }

    public String readMessage() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
        String message = "";

        int bytesRead = this.clientSocket.read(buffer);
        while (bytesRead == 0) {
            bytesRead = this.clientSocket.read(buffer);
        }

        buffer.flip();
        byte[] bytes = new byte[bytesRead];
        buffer.get(bytes, 0, bytesRead);
        message = new String(bytes).trim();

        return message;
    }

    public void writeMessage(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());

        while (buffer.hasRemaining()) {
            this.clientSocket.write(buffer);
        }
        buffer.clear();
    }
}
