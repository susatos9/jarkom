import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client{
  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;

  private String username;
  private boolean quiz_mode = false;
  private ArrayList<Question> questions = new ArrayList<>();
  private int questionIdx = 0;

  public Client(Socket socket, String username){
    try {
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.username = username;
    }
    catch(IOException e){
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public void listenMessageFromHost(){
    new Thread(new Runnable() {
      @Override
      public void run(){
        String msg;
        while(socket.isConnected()){
          try {
            msg = bufferedReader.readLine();

            if(!quiz_mode && msg.equals("start quiz")){ 
              quiz_mode = true;
              System.out.println("quiz has started!");

              // terima semua pertanyaan, jangan ditampilkan terlebih dahulu
              msg = bufferedReader.readLine();
              while(!msg.equals("end-questions")){
                questions.add(Question.unWrap(msg));
                msg = bufferedReader.readLine();
              }
              // tampilkan pertanyaan pertama
              questions.get(questionIdx++).print_question();
            }
            else { // pesan biasa
              System.out.println(msg);
            }
          }
          catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
          }
        }
      }
    }).start();
  }

  public void sendMessageToHost(){
    try {
      // send username
      bufferedWriter.write(username);
      bufferedWriter.newLine();
      bufferedWriter.flush();

      // send host status
      bufferedWriter.write("0");
      bufferedWriter.newLine();
      bufferedWriter.flush();

      // send answers
      Scanner scanner = new Scanner(System.in);
      while(socket.isConnected()){
        String msg = scanner.nextLine();
        bufferedWriter.write(username + ": " + msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        if(quiz_mode){ // dalam quiz mode, pesan berarti jawaban, maka tampilkan soal selanjutnya
          if(questionIdx == questions.size()){ // jawaban sudah ditampilkan semua, maka quiz sudah selesai
            questionIdx = 0;
            quiz_mode = false;
          }
          else questions.get(questionIdx++).print_question();
        }
      }
    }
    catch(IOException e){
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
    try {
      if(bufferedReader != null) bufferedReader.close();
      if(bufferedWriter != null) bufferedWriter.close();
      if(socket != null) socket.close();
    }
    catch (IOException e){
      e.printStackTrace();
    }
  }

  public static void main(String[]args) throws IOException {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter your username for this quiz: ");
    String username = scanner.nextLine();

    Socket socket = new Socket("localhost", 1234);
    Client client = new Client(socket, username);

    client.listenMessageFromHost();
    client.sendMessageToHost();
  }
}