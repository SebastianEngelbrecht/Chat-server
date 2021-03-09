package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

class ClientHandler implements Runnable {

    private String username;
    BlockingQueue<String> sendQueue;
    private PrintWriter pw;
    Socket socket;
    ChatServer chatServer;

    //Provides each instance with a unique id. Simulates the unique userid we will need for the chat-server
    private static int id = 0;

    public ClientHandler(Socket s, BlockingQueue<String> sendQueue, ChatServer chatServer) {
        this.socket = s;
        this.id++;
        this.sendQueue = sendQueue;
        this.chatServer = chatServer;
    }

    public String getId() {
        return "user" + id;
    }

    //Sender thread can call this and communicate with this specific client
    public void sendMessage(String msg){
        pw.println(msg);
    }

    void sendToAll(String msg) {
        try {
            sendQueue.put("MSG_ALL#" + msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
            //Todo handle this situation
        }
    }

    private boolean handleCommand(String message, PrintWriter pw) throws InterruptedException {
        String[] parts = message.split("#");
        System.out.println("Size: " + parts.length);
        if (parts.length == 1) {
            if (parts[0].equals("CLOSE")) {
                pw.println("CLOSE#");
                return false;
            }
            throw new IllegalArgumentException("Sent request does not obey the protocol");
        } else if (parts.length == 2) {
            String token = parts[0];
            String param = parts[1];

            if(username == null){
                //TODO CONNECT METODE
                if(token.equals("CONNECT")){

                }
                else{
                    //TODO SMID FEJL
                }
            }

            switch (token) {
                case "ALL":
                    sendToAll(param);
                    break;
                case "CONNECT":
                    if (ChatServer.users.containsKey(param)){
                        username = param;
                        chatServer.addToClientHandler(username,this);
                        chatServer.sendTasks.put(chatServer.showOnlineUsers());
                    }
                    else
                    {
                        pw.println("CLOSE#2");
                        return false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Sent request does not obey the protocol");
            }
        }
        return true;
    }

    private void handleClient() throws IOException {
        pw = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(socket.getInputStream());
        pw.println("Du er connected, send en streng for at få den upper cased, send 'stop' for stop");
        String message = "";
        boolean keepRunning = true;
        try {
            while (keepRunning) {
                message = scanner.nextLine();  //Blocking call
                keepRunning = handleCommand(message, pw);
            }
        } catch (Exception e) {
            System.out.println("UPPPS: " + e.getMessage() + " ---> " + message);
        }
        pw.println("Connection is closing");
        socket.close();
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