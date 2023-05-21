package services;

import models.ClientModel;
import utils.Constants;
import utils.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
//import io.github.cdimascio.dotenv.Dotenv;

public class QuestionsDatabase {
    String filepath;
    HashMap<String, Pair<String, String>> questions;
    private static final Pattern PATTERN = Pattern.compile("\\[(.*?),\\s*\"(.*?)\"\\]");
    // Dotenv dotenv = Dotenv.load();
    // int num_players = Integer.parseInt(this.dotenv.get("PLAYERS_PER_GAME"));
    // int num_questions_per_player = Integer.parseInt(this.dotenv.get("NUM_QUESTIONS_PER_PLAYER"));

    public QuestionsDatabase(String filepath) {
        this.filepath = filepath;
        this.questions = new HashMap<>();
        this.loadQuestions();
    }

    private void loadQuestions() {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.filepath))) {
            String line;
            int index = 1;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = PATTERN.matcher(line);
                if (matcher.find()) {
                    String question = matcher.group(1);
                    String answer = matcher.group(2);
                    this.questions.put(Integer.toString(index), new Pair<>(question, answer));
                    index++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Pair<String, String>> getRandomQuestions() {
        int num_questions = Constants.NUM_QUESTIONS_PER_PLAYER * Constants.PLAYERS_PER_GAME;
        HashMap<Integer, Pair<String, String>> randomQuestions = new HashMap<>();
        Random random = new Random();
        int i = 1;

        while (randomQuestions.size() < num_questions) {
            int index = random.nextInt(this.questions.size());
            Pair<String, String> randQuest = this.questions.get(Integer.toString(index));
            if (!randomQuestions.containsValue(randQuest)) {
                randomQuestions.put(i, questions.get(Integer.toString(index)));
                i++;
            }
        }

        return randomQuestions;
    }
}
