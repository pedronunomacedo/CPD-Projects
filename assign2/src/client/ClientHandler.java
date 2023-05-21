package client;

import models.ClientModel;
import server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private SocketChannel socket;
    public ClientModel client;

    public ClientHandler (SocketChannel socket, ClientModel client) throws IOException {
        this.socket = socket;
        this.client = client;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public SocketChannel getSocket() {
        return this.socket;
    }

    public String readMessage() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String message = new String();

        int bytesRead = this.socket.read(buffer);
        while (bytesRead == 0) {
            bytesRead = this.socket.read(buffer);
        }

        buffer.flip();
        byte[] bytes = new byte[bytesRead];
        buffer.get(bytes, 0, bytesRead);
        message = new String(bytes);

        return message;
    }

    public String readInstantMessage() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String message = "";
        this.socket.configureBlocking(false); // set non-blocking mode
        int bytesRead = this.socket.read(buffer);

        if (bytesRead == 0) return message;
        if (bytesRead == -1) return null; // client disconnected

        buffer.flip();
        byte[] bytes = new byte[bytesRead];
        buffer.get(bytes, 0, bytesRead);
        message = new String(bytes);

        return message;
    }

    public void writeMessage(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());

        while (buffer.hasRemaining()) {
            this.socket.write(buffer);
        }
        buffer.clear();
    }

    @Override
    public void run() {
        try {
            // Create a new socket channel and connect to the server
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 1234));
            System.out.println("Connected to server");

            // Create a scanner to read user input from the console
            Scanner scanner = new Scanner(System.in);

            while (true) {
                // Read a line of input from the console
                String message = scanner.nextLine();

                // Write the message to the server
                this.writeMessage(message);

                // Read the response message from the server
                String line = this.readMessage();
                System.out.println("Server response: " + line);
            }

            // Close the socket channel
            /*
            socket.close();
            System.out.println("Disconnected from server");
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeEverything(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
