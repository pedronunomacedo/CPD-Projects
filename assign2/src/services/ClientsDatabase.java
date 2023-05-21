package services;

import client.ClientHandler;
import models.ClientModel;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import utils.Constants;
import utils.Handlers;


public class ClientsDatabase {
    String filepath;
    private static TreeMap<String, ClientModel> clients;
    private static ReentrantLock clientsLock;
    public ClientsDatabase(String filepath) {
        this.filepath = filepath;
        clients = new TreeMap<>();
        clientsLock = new ReentrantLock();
        this.loadClients();
    }

    private void loadClients() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String password = parts[1];
                String lastToken = parts[2];
                int score = Integer.parseInt(parts[3]);
                this.clients.put(username, new ClientModel(username, password, lastToken, score));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientModel registerNewClient(String username, String password) {
        ClientModel newClient = new ClientModel(username, password, Handlers.generateToken(), 0);

        this.clients.put(username, newClient);
        this.saveClients();

        try {
            File file = new File(Constants.TOKENS_DIR + "/username_" + username + ".txt");
            file.createNewFile();

            PrintWriter fileWriter = new PrintWriter(file);
            fileWriter.println(newClient.getLastToken());
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }

        return newClient;
    }

    private void saveClients() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.filepath))) {
            for (ClientModel client : this.clients.values()) {
                bw.write(client.getUsername() + "," + client.getPassword() + "," + client.getLastToken() + "," + client.getScore() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientModel getUser(String username) {
        return this.clients.get(username);
    }

    public void updateToken(String username, String newToken) {
        this.clients.get(username).setLastToken(newToken);
    }

    public static void setClientScore(ClientHandler client, int newScore) {
        try {
            clientsLock.lock();
            client.client.setScore(newScore);
            client.client.setLastToken("");
            clients.put(client.client.getUsername(), client.client);
        } finally {
            clientsLock.unlock();
        }

    }

    public static void updateClientsDatabase(Set<ClientHandler> gameClients) throws IOException {

        File file = new File(Constants.USERS_DB);
        file.createNewFile();
        PrintWriter fileWriter = new PrintWriter(file);

        for (ClientHandler client : gameClients) {
            clients.put(client.client.getUsername(), client.client);
        }

        for (ClientModel clientDB : clients.values()) {
            fileWriter.println(clientDB.getUsername() + "," + clientDB.getPassword() + "," + clientDB.getLastToken() + "," + clientDB.getScore());

        }
        fileWriter.close();
    }
}
