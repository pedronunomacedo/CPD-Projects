package utils;

public class Constants {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8000;

    public static final int MAX_WAITING_PLAYERS = 20;
    public static final int MAX_NUM_GAMES = 5;
    public static final int PLAYERS_PER_GAME = 2;
    public static final int NUM_QUESTIONS_PER_PLAYER = 6;
    public static final int CORRECT_ANSWER_SCORE = 10;
    public static final int BUFFER_SIZE = 1024;
    public static final int QUESTION_ANSWER_TIME = 10000; // 10 seconds (10000 milliseconds)
    public static final String PROJ_DIR = System.getProperty("user.dir");
    public static final String TOKENS_DIR = System.getProperty("user.dir") + "/data/tokens";
    public static final String USERS_DB = System.getProperty("user.dir") + "/data/users.txt";
    public static final String QUESTIONS_DB = System.getProperty("user.dir") + "/data/questions.txt";
}
