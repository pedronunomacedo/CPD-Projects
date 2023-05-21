package utils;
import models.ClientModel;

import java.util.UUID;

public class Handlers {
    public static String generateToken() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static boolean sessionTokenIsValid(ClientModel client, String token) {
        return client.getLastToken().equals(token);
    }
}
