package chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class ClientHandler implements Runnable{
    private Socket socket;
    private  PrintWriter pw;
    chat.ChatServer chatServer;
    private String userName = "";
    private int closeStatus = 0;

    public String getUserName() {
        return userName;
    }

    public ClientHandler(Socket socket, chat.ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
    }

    public void sendToThisClient(String msg){
        pw.println(msg);
    }



    private boolean handleCommand(String msg, PrintWriter pw,Scanner scanner) throws IOException {
        //System.out.println("Command: "+msg);
        String[] parts = msg.split("#");
        if (parts.length == 1) {
            if (parts[0].equals("CLOSE")) {
                return false;
            }
            throw new IllegalArgumentException("Sent request does not obey the protocol");
        }if (parts.length == 3) {
            //Only one command to handle here, since Connect is taken care of in it's own method
            String token = parts[0];
            String receivers = parts[1];
            String message = parts[2];
            if (token.equals("SEND")) {
                if (receivers.equals("*")) {
                    chatServer.sendToAll(userName + "," +message);
                } else  {
                    chatServer.sendToSingleClient(userName + "," + receivers + "," + message);
                }
            }

        } else if(parts.length == 2) {
            throw new IllegalArgumentException("sent request does not obey protocol");
        }
        return true;
    }

    private boolean handleConnectCommand(String msg, PrintWriter pw,Scanner scanner){
        String[] parts = msg.split("#");
        if(parts.length != 2 || !parts[0].equals("CONNECT")){
            closeStatus = 1;
            throw new IllegalArgumentException("Sent request does not obey protocol");
        }
        userName = parts[1];
        if(chat.ChatServer.doesUserExist(userName)) {
            chatServer.addClientToList(userName, this);
        } else{
            closeStatus = 2;
            throw new IllegalArgumentException();
        }
        return true;
    }

    private void handleClient() throws IOException {
        pw = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(socket.getInputStream());
        try {
            String message = "";
            //This is taken care of here, since it can ONLY HAPPEN HERE and ONLY ONCE
            //Read the CONNECT#user  message
            String connectMsg = scanner.nextLine();
            System.out.println(connectMsg);
            handleConnectCommand(connectMsg,pw,scanner);

            boolean keepRunning = true;
            while (keepRunning) {
                message = scanner.nextLine();  //Blocking call
                keepRunning = handleCommand(message, pw,scanner);
            }
            chatServer.removeClientFromList(userName, this);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            pw.println("CLOSE#"+closeStatus);
            socket.close();
        }

    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}