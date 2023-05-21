package client;

import utils.Constants;
import utils.Tcp;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    private String hostname;
    private int port;
    private SocketChannel clientSocket;
    private String userToken;
    private String username;

     public Client(String hostname, int port) throws IOException {
        this.hostname = hostname;
        this.port = port;

        // Open a socket channel and connect to the server
        this.clientSocket = SocketChannel.open();
        this.clientSocket.connect(new InetSocketAddress(hostname, port));
        this.userToken = "";
        this.username = "";
        System.out.println("Connected to server at " + hostname + ":" + port);
    }

    public void clientSessionToken(String username) {

        try (BufferedReader br = new BufferedReader(new FileReader(Constants.TOKENS_DIR + "/username_" + username + ".txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                this.userToken = parts[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGame() throws IOException {
        String line = this.readMessage(); // waits for the game to start!
        System.out.println(line); // "Welcome to the game! participants: (...)" - Start of the game

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String question = this.readMessage().trim(); // question
            if (question.startsWith("Time ended")) continue; // In case the user reconnects in the middle of a question (skips that question)

            System.out.print(question + " ");

            long startTime = System.currentTimeMillis();
            String userResponse = "";

            // Starts the countdown and check always for a user input
            while (System.currentTimeMillis() - startTime < Constants.QUESTION_ANSWER_TIME) {
                if (System.in.available() > 0) { // Check for user input line
                    userResponse = scanner.nextLine().trim();
                    this.writeMessage(userResponse);
                    break;
                }
            }

            String serverResponse = this.readMessage(); // Read the 'Time ended for question _' sent by the server
            System.out.println(serverResponse);

            if (question.startsWith(Integer.toString(Constants.NUM_QUESTIONS_PER_PLAYER))) break; // Last question
        }

        showWinners();
    }

    private void showWinners() throws IOException {
         String playerScoreMsg = this.readMessage();
         System.out.println("\n\n" + playerScoreMsg);
    }

    private boolean authenticate(boolean reconnect) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String serverResponse = null;
        do {
            String authContent = this.readMessage();
            System.out.print(authContent);
            this.username = scanner.nextLine();
            this.writeMessage(this.username);

            authContent = this.readMessage();
            System.out.print(authContent);
            String password = scanner.nextLine();
            this.writeMessage(password);

            serverResponse = this.readMessage();
            System.out.println(serverResponse.split(":")[1].trim());
        } while (Integer.parseInt(serverResponse.split(":")[0].trim()) != Tcp.TCP_OK);

        this.clientSessionToken(this.username);

        if (!reconnect) this.startGame();

        return false;
    }

    public boolean register() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String serverResponse = "", password = "";
        do {
            String authContent = this.readMessage();
            System.out.print(authContent);
            this.username = scanner.nextLine();
            this.writeMessage(this.username);

            authContent = this.readMessage();
            System.out.print(authContent);
            password = scanner.nextLine();
            this.writeMessage(password);

            serverResponse = this.readMessage();
            System.out.println(serverResponse.split(":")[1]);
        } while (!(Integer.parseInt(serverResponse.split(":")[0].trim()) == Tcp.TCP_CREATED));

        this.startGame();

        return false;
    }

    public boolean reconnect() throws IOException {
        boolean clientStop = this.authenticate(true); // Send the username and the password to the server

        // Send the client session token
        this.clientSessionToken(this.username);
        this.writeMessage(this.userToken);

        String serverResponse = this.readMessage();
        System.out.println(serverResponse);
        if (Integer.parseInt(serverResponse.split(":")[0].trim()) == (Tcp.TCP_OK)) {
            this.startGame();
        }

        return false;
    }

    private String mainMenu() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print(this.readMessage());

        String userInput = null;
        do {
            System.out.print((userInput != null) ? "Invalid option. New choice: " : "");
            userInput = scanner.nextLine();
        } while (!(userInput.equals("1") || userInput.equals("2") || userInput.equals("3") || userInput.equals("4")));

        this.writeMessage(userInput);

        return userInput;
    }

    public void start() throws IOException {
        String userInput = mainMenu();
        boolean clientStop = false;

        while (!clientStop) {
            switch (Integer.parseInt(userInput)) {
                case 1: // login
                    clientStop = this.authenticate(false);
                    break;
                case 2: // register
                    clientStop = this.register();
                    break;
                case 3: // reconnect
                    clientStop = this.reconnect();
                    break;
                case 4: // quit
                    this.closeEverything();
                    clientStop = true;
                    System.exit(1);
                    break;
            }
        }

    }

    public void closeEverything() {
        try {
            if (this.clientSocket != null) {
                this.clientSocket.close();
                System.out.println("Disconnected from server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        message = new String(bytes);

        return message;
    }

    public void writeMessage(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        this.clientSocket.write(buffer);
    }
}