import java.util.*;
import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        int initialPort = 5000;
        boolean waiting = true;

        // Initial ServerSocket
        ServerSocket initialServer = new ServerSocket(initialPort);
        System.out.println("Initial server started on port " + initialPort);

        while (waiting) {
            // Accept client connection on the initial port
            Socket clientSocket = initialServer.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // Handle client redirection in a separate thread
            new Thread(() -> {
                try {
                    handleClientRedirect(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter 'start' to start the quiz: ");
            String start = scanner.nextLine();
            if (start.toLowerCase().equals("start")) {
                waiting = false;
                break;
            }
        }
    }

    private static void handleClientRedirect(Socket clientSocket) throws IOException {
        // Dynamically allocate a new port
        ServerSocket newServer = new ServerSocket(0); // Port 0 means dynamically assigned
        int newPort = newServer.getLocalPort();

        // Inform client of the new port
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("REDIRECT " + newPort);

        // Close the initial connection
        clientSocket.close();
        System.out.println("Client redirected to port " + newPort);

        // Accept the client on the new dynamically assigned port
        Socket newClientSocket = newServer.accept();
        System.out.println("Client connected to dynamically assigned port: " + newPort);

        // Handle client communication
        handleClient(newClientSocket);

        // Close the dynamically created ServerSocket
        newServer.close();
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
