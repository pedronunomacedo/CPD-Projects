package models;

import utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ClientModel {
    private String username;
    private String password;
    private String lastToken;
    private int score;
    private boolean active;

    public ClientModel(String username, String password, String lastToken, int score) {
        this.username = username;
        this.password = password;
        this.lastToken = lastToken;
        this.score = score;
        this.active = false;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getLastToken() {
        return this.lastToken;
    }

    public int getScore() { return this.score; }
    public boolean isActive() { return this.active; }
    public void setScore(int score) { this.score = score; }

    public void setLastToken(String lastToken) {
        this.lastToken = lastToken;

        try (FileOutputStream fos = new FileOutputStream(Constants.TOKENS_DIR + "/username_" + this.username + ".txt", false)) {
            byte[] bytes = lastToken.getBytes();
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            System.err.println("Error overwriting file: " + e.getMessage());
        }
    }

    public void setIsActive(boolean active) { this.active = active; }
}
