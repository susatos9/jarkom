import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        String serverHost = "localhost";
        int serverPort = 5000;

        // Initial connection
        Socket socket = new Socket(serverHost, serverPort);
        System.out.println("Connected to server on port " + serverPort);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Read redirection message
        String serverMessage = in.readLine();
        System.out.println("Server message: " + serverMessage);

        if (serverMessage.startsWith("REDIRECT")) {
            // Extract new port
            int newPort = Integer.parseInt(serverMessage.split(" ")[1]);
            socket.close(); // Close current socket

            // Connect to the new port
            socket = new Socket(serverHost, newPort);
            System.out.println("Connected to new server on port " + newPort);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Communication on the new socket
            out.println("Hello, new server!");
            System.out.println("Server reply: " + in.readLine());

            socket.close();
        }
    }
}
