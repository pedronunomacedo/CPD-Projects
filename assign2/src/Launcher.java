import java.io.IOException;

import client.Client;
import server.Server;

public class Launcher {
    public static void main(String args[]) throws IOException {
        if (args.length != 2 && args.length != 3) {
            System.out.println("Incorrect number of arguments!");
            System.out.println("To run as a client, execute with arguments -c <hostname> <port>.");
            System.out.println("To run the server, execute with arguments -s <mode> <port>.");
            System.exit(-1);
        }

        if (args[0].equals("-c")) { // client side (2 parameters - hostname and port)
            System.out.println("Starting as a client");
            String address = args[1];
            int port = Integer.parseInt(args[2]);
            System.out.println("address = " + address);
            System.out.println("port = " + port);
            Client client = new Client(address, port);
            client.start();
        } else if (args[0].equals("-s")) { // server side (2 parameters - mode and port)
            System.out.println("Starts as a server");
            String mode = args[1];
            int port = Integer.parseInt(args[2]);
            System.out.println("mode = " + mode);
            System.out.println("port = " + port);
            Server server = new Server(mode, port);
            server.start();
        } else {
            System.out.println("Invalid argument " + args[0] + ". Please use \"-c\" or \"-s\" instead!");
            System.exit(-1);
        }
    }
}
