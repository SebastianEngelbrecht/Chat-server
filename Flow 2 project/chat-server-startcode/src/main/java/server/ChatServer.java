package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {


    public static final int DEFAULT_PORT = 2345;
    BlockingQueue<String> sendTasks;
    ConcurrentHashMap<String, ClientHandler> allClientHandlers;
    public static Map<String,String> users;
    static {users = new HashMap<>();
    users.put("Peter","Peter");
    users.put("Sebastian","Sebastian");
    users.put("Tobias","Tobias");}

    private void startServer(int port) throws IOException {
        ServerSocket serverSocket;
        serverSocket = new ServerSocket(port);

        allClientHandlers = new ConcurrentHashMap<>();
        sendTasks = new ArrayBlockingQueue<>(10);

        //Start sender thread
        server.HandleSendToAllClients handler;
        handler = new HandleSendToAllClients(sendTasks, allClientHandlers);
        new Thread(handler).start();

        System.out.println("Server started, listening on : " + port);

        while (true) {
            System.out.println("Waiting for a client");
            Socket socket = serverSocket.accept();
            System.out.println("New client connected");
            ClientHandler clientHandler = new ClientHandler(socket, sendTasks,this);
            //allClientHandlers.put(clientHandler.getId(), clientHandler);
            new Thread(clientHandler).start();
        }
    }

    public void addToClientHandler(String username, ClientHandler clientHandler) {
        allClientHandlers.put(username,clientHandler);
    }

    public String showOnlineUsers() {
        StringBuilder result = new StringBuilder();
        result.append("ONLINE#");
        int counter = 0;
        for (Map.Entry<String, ClientHandler> tmp : allClientHandlers.entrySet()) {
            if (counter == 0){
                result.append(tmp.getKey());
                counter++;
            }
            else{
                result.append(","+tmp.getKey());
            }
        }
        return result.toString();
    }


    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;

        try {
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
            } 
        } catch (NumberFormatException ne) {
            System.out.println("Illegal inputs provided when starting the server!" + DEFAULT_PORT);
            return;
        }
        ChatServer server = new ChatServer();
        server.startServer(port);


    }
}