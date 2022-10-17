package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
  public static void main(String[] args){
    String msgFromClient;
    try{
      ServerSocket serverSocket = new ServerSocket(2810);
      System.out.println("Server is running...");
      while(true){
        Socket socket = serverSocket.accept();
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        msgFromClient = inputStream.readUTF();
        inputStream.close();
        socket.close();
        System.out.printf("%s%n", msgFromClient);
      }
    } catch(IOException e){
      System.out.println("Oops! Something went wrong :)");
    }

  }
}
