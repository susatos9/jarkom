import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        int initialPort = 5000;
        int newPort = 6000;

        // Initial ServerSocket
        ServerSocket initialServer = new ServerSocket(initialPort);
        System.out.println("Initial server started on port " + initialPort);

        while (true) {
            Socket clientSocket = initialServer.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // Inform client of new port
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("REDIRECT " + newPort);

            // Close initial connection
            clientSocket.close();
            System.out.println("Client redirected to port " + newPort);

            // New ServerSocket for handling redirected clients
            ServerSocket newServer = new ServerSocket(newPort);
            Socket newClientSocket = newServer.accept();
            System.out.println("Client connected to new socket: " + newClientSocket.getInetAddress());

            // Handle new client connection
            handleClient(newClientSocket);

            newServer.close(); // Close the new server socket after handling
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Received: " + message);
            out.println("Echo: " + message);
        }

        clientSocket.close();
    }
}
