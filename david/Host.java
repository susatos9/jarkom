import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Host {
  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;
  
  private String username;

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

  public void listenMessage(){
    new Thread(new Runnable() {
      @Override
      public void run(){
        String msg;

        while(socket.isConnected()){
          try {
            msg = bufferedReader.readLine();
            System.out.println(msg);
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
      // send username (host)
      bufferedWriter.write("Host");
      bufferedWriter.newLine();
      bufferedWriter.flush();

      // send host status
      bufferedWriter.write("1");
      bufferedWriter.newLine();
      bufferedWriter.flush();

      // send messages
      Scanner scanner = new Scanner(System.in);
      while(socket.isConnected()){
        String msg = scanner.nextLine();
        bufferedWriter.write(this.username + ": " + msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
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

  public static void main(String[]args) throws IOException{
    Scanner scanner = new Scanner(System.in);
    System.out.print("You are the host for this quiz, please input username: ");
    String username = scanner.nextLine();
    

    Socket socket = new Socket("localhost", 1234);
    Host host = new Host(socket, username);

    host.listenMessage();
    host.sendMessage();
  }
}
