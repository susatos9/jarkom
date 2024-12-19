import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Host {
  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  
  private String username;

  private ArrayList<Question> questions = new ArrayList<>();
  private Map<String, String> clientAnswer = new HashMap<>(); // client's answer: name, answers
  private boolean quiz_mode = false;

  public Host(Socket socket, String username){
    try {
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.username = "Host["+username+"]";
    }
    catch (IOException e){
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public String getUsernameFromResponse(String ans){
    for(int i = 0; i < ans.length(); i++){
      if(ans.charAt(i) == ':'){
        return ans.substring(0, i);
      }
    }
    return "username not found";
  }
  public String getAnswerFromResponse(String ans){
    for(int i = 0; i < ans.length(); i++){
      if(ans.charAt(i) == ':'){
        return ans.substring(i + 2);
      }
    }
    return "answer not found";
  }

  public void listenMessage(){
    new Thread(new Runnable() {
      @Override
      public void run(){
        String msg;

        while(socket.isConnected()){
          try {
            msg = bufferedReader.readLine();
            System.out.println(msg);

            if(quiz_mode){ // ambil jawaban dari nama peserta
              String usr = getUsernameFromResponse(msg);
              String ans = getAnswerFromResponse(msg);

              // simpan di map, tambahkan jawaban ke client's username
              String old_value = clientAnswer.get(usr);
              if(old_value != null) clientAnswer.put(usr, old_value + ans);
              else clientAnswer.put(usr, ans);
            }
          } 
          catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
          }
        }
      }
    }).start();
  }

  public void sendMessage(){
    try {
      // send username (host), for client handler
      bufferedWriter.write("Host");
      bufferedWriter.newLine();
      bufferedWriter.flush();

      // send host status, for client handler
      bufferedWriter.write("1");
      bufferedWriter.newLine();
      bufferedWriter.flush();

      // send messages
      Scanner scanner = new Scanner(System.in);
      while(socket.isConnected()){
        String msg = scanner.nextLine();

        if(!quiz_mode && msg.equals("start quiz")){ // mulai quiz
          quiz_mode = true;
          bufferedWriter.write(msg);
          bufferedWriter.newLine();
          bufferedWriter.flush();

          for(Question q : questions){ // langsung kirim semua pertanyaan
            bufferedWriter.write(q.wrap());
            bufferedWriter.newLine();
            bufferedWriter.flush();
          }

          bufferedWriter.write("end-questions");
          bufferedWriter.newLine();
          bufferedWriter.flush();
        }
        else if(quiz_mode && msg.equals("send leaderboard")){
          send_leaderboard();
        }
        else {
          bufferedWriter.write(this.username + ": " + msg);
          bufferedWriter.newLine();
          bufferedWriter.flush();
        }
      }
    }
    catch(IOException e){
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
    try {
      if(bufferedReader != null){
        bufferedReader.close();
      }
      if(bufferedWriter != null){
        bufferedWriter.close();
      } 
      if(socket != null){
        socket.close();
      }
    }
    catch (IOException e){
      e.printStackTrace();
    }
  }

  public void read_question(){
    questions.clear();
    questions.add(new Question("Di dalam sebuah router, HOL (head-of-the-line) blocking disebabkan oleh:"));
    questions.get(0).add_option("Queueing delay dan kehilangan paket di sebuah input buffer");
    questions.get(0).add_option("queueing delay dan kehilangan paket di sebuah output buffer");
    questions.get(0).add_option("saling menghalangi paket-paket yang berada di input buffer yang berbeda");
    questions.get(0).add_option("saling menghalangi paket-paket yang berada di output buffer yang berbeda");
    questions.get(0).set_answer("c");

    questions.add(new Question("Andaikan bit error adalah independed secara statistik ketika sebuah frame ditransmisikan di suatu link; andaikan p adalah probabilitas terjadinya sebuah bit error. Berapakah probabilitas bahwa paling sedikit dua bit error terjadi ketika k buah bit ditransmisikan?"));
    questions.get(1).add_option("1 - (1 - p)^k");
    questions.get(1).add_option("1 - (1 - p)^k - kp(1 - p)^(k - 1)");
    questions.get(1).add_option("1 - (1 - p)^(k+1) - p(1 - p)^k");
    questions.get(1).add_option("p");
    questions.get(1).set_answer("b");
  }

  public void send_leaderboard(){
    try {
      bufferedWriter.write("ingfo leaderboard");
      bufferedWriter.newLine();
      bufferedWriter.flush();

      for(Map.Entry<String, String> entry : clientAnswer.entrySet()){
        bufferedWriter.write(entry.getKey() + ": " + entry.getValue());
        bufferedWriter.newLine();
        bufferedWriter.flush();
      }
    }
    catch (IOException e){
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public static void main(String[]args) throws IOException{
    Scanner scanner = new Scanner(System.in);
    System.out.print("You are the host for this quiz. Enter username: ");
    String username = scanner.nextLine();
  
    Socket socket = new Socket("localhost", 1234);
    Host host = new Host(socket, username);

    host.read_question();
    host.listenMessage();
    host.sendMessage();
  }
}
