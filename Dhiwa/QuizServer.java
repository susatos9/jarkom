// QuizServer.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;


public class QuizServer {
    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static volatile boolean quizStarted = false;
    private static final int QUIZ_DURATION = 20; // Quiz duration in seconds
    private static Map<String,Integer> scoreboard = new HashMap(); 
    private static final int question = 3;
        
        public static void main(String[] args) {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Quiz server started on port " + PORT);
                System.out.println("Waiting for the host to start the quiz...");
    
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
    
                    
    
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        static class ClientHandler implements Runnable {
            private Socket clientSocket;
            private PrintWriter out;
            private BufferedReader in;
            private String playerName;
            private int score = 0 ;
            private boolean isHost = false;
            private static final List<String[]> questions = Arrays.asList(
                new String[]{"What is the capital of France?", "Paris"},
                new String[]{"2 + 2 = ?", "4"},
                new String[]{"Who wrote 'Hamlet'?", "Shakespeare"}
            );
    
            public ClientHandler(Socket socket) {
                this.clientSocket = socket;
            }
    
            
    
            @Override
            public void run() {
                try {
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
    
                    out.println("Enter your name:");
                    playerName = in.readLine();
                    
                    if(playerName != null && playerName.equalsIgnoreCase("HOST")){ 
                        isHost = true; 
                    }
                    else { 
                        System.out.println(playerName + " has joined the quiz.");
                    }
                    if (isHost) {
                        out.println("You are the host. Type 'START' to begin the quiz.");
                        while (true) {
                            String command = in.readLine();
                            if (command != null && command.equalsIgnoreCase("START")) {
                                quizStarted = true;
                                broadcast("The quiz has started! You have " + QUIZ_DURATION + " seconds to complete it.");
                                new Thread(QuizServer::startTimer).start();
                                break;
                            } else {
                                out.println("Type 'START' to begin the quiz.");
                            }
                        }
                    } else {
                        out.println("Waiting for the host to start the quiz...");
                        while (!quizStarted) {
                            Thread.sleep(500);
                        }
                        scoreboard.put(playerName, score);
                        for (String[] question : questions) {
                            if (!quizStarted) break;
    
                            out.println(question[0]);
                            String answer = in.readLine();
    
                            if (!quizStarted) break;
                            if (answer != null && answer.equalsIgnoreCase(question[1])) {
                                out.println("Correct!");
                                score++;
                                scoreboard.put(playerName, score);
                            } else {
                                out.println("Wrong! The correct answer was: " + question[1]);
                            }
                        }
    
                        out.println("Your score: " + 100*score/questions.size());
                        out.println("Thank you for playing!");
                        
                        scoreboard.put(playerName, score);
                    //System.out.println(playerName + " has score " + 100*score/questions.size());
                }

                

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                if(!client.playerName.equalsIgnoreCase("HOST") ) {
                    client.sendMessage(message);
                }
            }
        }

        private void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }

    private static void startTimer() {
        try {
            Thread.sleep(QUIZ_DURATION*1000);
            quizStarted = false;
            broadcastToAll("Time's up! The quiz has ended.");
            for(Entry<String, Integer> i : scoreboard.entrySet()) {
                System.out.println(i.getKey() + " has score " + 100*i.getValue()/question);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastToAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
