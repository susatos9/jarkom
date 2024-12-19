import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
  public static ArrayList<ClientHandler> clientHandlers=new ArrayList<>();
  private Socket socket;
  private BufferedReader bufferedReader;
  private BufferedWriter bufferedWriter;

  private String clientUsername;
  private boolean clientHostStatus = false;
  
  public ClientHandler(Socket socket){
    try {
      this.socket = socket;
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.clientUsername = bufferedReader.readLine();
      this.clientHostStatus = (bufferedReader.readLine().equals("1"));
      clientHandlers.add(this);

      // send this username to host
      sendToHost(clientUsername + " has entered the quiz room");
    }
    catch (IOException e){
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  @Override
  public void run(){  // listening for message
    while(socket.isConnected()){
      try {
        if(!clientHostStatus){ // terima pesan bukan dari host, kirim hanya ke host
          String messageFromClient = bufferedReader.readLine();
          sendToHost(messageFromClient);
        }
        else { // terima pesan dari host, kirim ke semua peserta
          String messageFromHost = bufferedReader.readLine();
          sendToClient(messageFromHost);
        }
      }
      catch (IOException e){
        closeEverything(socket, bufferedReader, bufferedWriter);
        break;
      }
    }
  }
  // send message to host
  public void sendToHost(String msg){
    for(ClientHandler clientHandler : clientHandlers){
      try{
        if(clientHandler.clientHostStatus){
          clientHandler.bufferedWriter.write(msg);
          clientHandler.bufferedWriter.newLine();
          clientHandler.bufferedWriter.flush();
        }
      }
      catch (IOException e){
        closeEverything(socket, bufferedReader, bufferedWriter);
      }
    }
  }
  // send message to client
  public void sendToClient(String msg){
    for(ClientHandler clientHandler : clientHandlers){
      try{
        if(!clientHandler.clientHostStatus){
          clientHandler.bufferedWriter.write(msg);
          clientHandler.bufferedWriter.newLine();
          clientHandler.bufferedWriter.flush();
        }
      }
      catch (IOException e){
        closeEverything(socket, bufferedReader, bufferedWriter);
      }
    }
  }

  public void removeClientHandler(){
    sendToHost(clientUsername + " has left the room");
    clientHandlers.remove(this);
  }

  public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
    removeClientHandler();
    try {
      if(bufferedReader != null) bufferedReader.close();
      if(bufferedWriter != null) bufferedWriter.close();
      if(socket != null) socket.close();
    }
    catch (IOException e){
      e.printStackTrace();
    }
  }

}