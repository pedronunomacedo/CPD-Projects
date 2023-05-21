package server;

import client.ClientHandler;
import services.ClientsDatabase;
import services.QuestionsDatabase;
import utils.Pair;
import utils.Constants;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
// import io.github.cdimascio.dotenv.Dotenv;

public class Game extends Thread {
    private HashMap<ClientHandler, Boolean> clients;
    private QuestionsDatabase questionsDB;
    private HashMap<Integer, Pair<String, String>> randomQuestions;
    private HashMap<ClientHandler, ArrayList<Pair<String, String>>> playersQuestions;
    private HashMap<ClientHandler, String> playersResponse;
    private HashMap<String, Integer> playersScore;
    public ReentrantLock clientsListLock;

    public Game(ArrayList<ClientHandler> clients) {
        this.clients = new HashMap<>();
        this.clientsListLock = new ReentrantLock();
        for (ClientHandler client : clients) {
            this.clients.put(client, true);
        }
        this.questionsDB = new QuestionsDatabase(Constants.QUESTIONS_DB);
        this.randomQuestions = this.questionsDB.getRandomQuestions();
        this.playersQuestions = new HashMap<>();
        this.associateQuestionsWithPlayers();
        this.playersResponse = new HashMap<>();
        this.playersScore = new HashMap<>();

        System.out.println("Before initializing players' score!");
        for (ClientHandler clientHandler : clients) {
            this.playersScore.put(clientHandler.client.getUsername(), 0);
        }
    }

    public void associateQuestionsWithPlayers() {
        Iterator<Map.Entry<Integer, Pair<String, String>>> it = this.randomQuestions.entrySet().iterator();

        for (ClientHandler client : this.clients.keySet()) {
            ArrayList<Pair<String, String>> playerQuestions = new ArrayList<>();
            for (int i = 0; i < Constants.NUM_QUESTIONS_PER_PLAYER; i++) {
                if (!it.hasNext()) {
                    it = this.randomQuestions.entrySet().iterator(); // start from the beginning
                }
                Map.Entry<Integer, Pair<String, String>> entry = it.next();
                playerQuestions.add(entry.getValue());
            }
            this.playersQuestions.put(client, playerQuestions);
        }
    }

    public HashMap<ClientHandler, Boolean> getClients() {
        return this.clients;
    }

    public void setClientActive(ClientHandler client, SocketChannel clientSocket) {
        this.clients.remove(client);
        client.setSocket(clientSocket);
        this.clients.put(client, true);
    }

    @Override
    public void run() {
        Iterator<ClientHandler> iterator = this.clients.keySet().iterator();
        while (iterator.hasNext()) {
            ClientHandler client = iterator.next();
            if (this.clients.get(client)) { // if client is not disconnected
                try {
                    client.writeMessage("Welcome to the game " + client.client.getUsername() + "! participants: (" + String.join(",", this.clients.keySet().stream().map(cl -> cl.client.getUsername()).collect(Collectors.joining(", "))) + ")\n");
                } catch (IOException e) {
                    System.out.println("Client " + client.client.getUsername() + " disconnected!");
                    if (!this.clients.get(client)) {
                        this.clients.put(client, false);
                        Server.addDisconnectedClient(client);
                        iterator.remove(); // Remove the disconnected client using the iterator
                    }
                }
            }
        }

        System.out.println("Sending questions to the players ...");
        for (int quest_num = 0; quest_num < Constants.NUM_QUESTIONS_PER_PLAYER; quest_num++) {
            for (Map.Entry<ClientHandler, ArrayList<Pair<String, String>>> entry : this.playersQuestions.entrySet()) {
                if (this.clients.get(entry.getKey())) { // if client is connected
                    String question = "\n" + (quest_num + 1) + ") " + entry.getValue().get(quest_num).getFirst();
                    try {
                        System.out.println("Sending question " + (quest_num + 1) + " to " + entry.getKey().client.getUsername());
                        entry.getKey().writeMessage(question);
                    } catch (IOException e) {
                        if (!this.clients.get(entry.getKey())) {
                            System.out.println("Client " + entry.getKey().client.getUsername() + " disconnected!");
                            this.clients.put(entry.getKey(), false);
                            Server.addDisconnectedClient(entry.getKey());
                        }
                    }
                }
            }

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < Constants.QUESTION_ANSWER_TIME) {
                for (ClientHandler client : this.playersQuestions.keySet()) {
                    if (this.clients.get(client)) {
                        String playerAnswer = "";
                        try {
                            playerAnswer = client.readInstantMessage();
                            if (playerAnswer == null) {
                                System.out.println("Client "  + client.client.getUsername() + " disconnected!");
                                this.clients.put(client, false);
                                Server.addDisconnectedClient(client);;
                            } else if (!playerAnswer.equals("")) {
                                if (!this.playersResponse.containsKey(client)) { // Check if it already answered the question
                                    this.playersResponse.put(client, playerAnswer);
                                    if (this.playersQuestions.get(client).get(quest_num).getSecond().equals(playerAnswer)) { // correct answer
                                        client.client.setScore(client.client.getScore() + Constants.CORRECT_ANSWER_SCORE); // update client score
                                        this.playersScore.replace(client.client.getUsername(), this.playersScore.get(client.client.getUsername()) + Constants.CORRECT_ANSWER_SCORE);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            if (!this.clients.get(client)) {
                                System.out.println("Client " + client.client.getUsername() + " disconnected!");
                                this.clients.put(client, false);
                                Server.addDisconnectedClient(client);
                            }
                        }
                    }
                }
            }

            System.out.println("\nTime ended for question " + (quest_num + 1) + "!\n");
            for (ClientHandler client : this.playersQuestions.keySet()) {
                if (this.clients.get(client)) {
                    try {
                        client.writeMessage("\nTime ended for question " + (quest_num + 1) + "!");
                    } catch (IOException e) {
                        if (!this.clients.get(client)) {
                            System.out.println("Client " + client.client.getUsername() + " disconnected!\n");
                            this.clients.put(client, false);
                            Server.addDisconnectedClient(client);
                        }
                    }
                }
            }
        }

        System.out.println("players score: " + this.playersScore);
        for (ClientHandler client : this.clients.keySet()) {
            if (this.clients.get(client)) {
                try {
                    client.writeMessage("players score: " + this.playersScore);
                } catch (IOException e) {
                    if (!this.clients.get(client)) {
                        //throw new RuntimeException(e);
                        System.out.println("Client " + client.client.getUsername() + " disconnected!");
                        this.clients.put(client, false);
                        Server.addDisconnectedClient(client);
                    }
                }
            }
        }

        for (ClientHandler client : this.clients.keySet()) {
            ClientsDatabase.setClientScore(client, client.client.getScore());
        }

        try {
            ClientsDatabase.updateClientsDatabase(this.clients.keySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.clients = new HashMap<>(); // clear the clients list
    }
}
