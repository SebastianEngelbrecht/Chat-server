package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class ChatServer {
    public static final int DEFAULT_PORT = 2345;
    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, ClientHandler> allClientHandlers = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, ClientHandler> getAllClientHandlers() {
        return allClientHandlers;
    }

    //Value not used, but this gives experience with Map
    private static Map<String,String> allChatUsers = new HashMap<>();
    static {
        allChatUsers.put("Peter","Peter");
        allChatUsers.put("Sebastian","Sebastian");
        allChatUsers.put("Tobias","Tobias");
        allChatUsers.put("Lars","Lars");
        allChatUsers.put("Muslim","Muslim");
        allChatUsers.put("Edvard","Edvard");
        allChatUsers.put("Arkadii","Arkadii");
        allChatUsers.put("Lonardi","Lonardi");
        allChatUsers.put("Kris","Kris");
        allChatUsers.put("Tommy","Tommy");
        allChatUsers.put("Mila","Mila");
    }

    public static boolean doesUserExist(String user){
        return allChatUsers.get(user) != null  ? true : false;
    }


    public void addClientToList(String user, ClientHandler ch){
        allClientHandlers.put(user,ch);
        sendOnlineMessageToAll();
    }

    public void removeClientFromList(String user, ClientHandler ch){
        //TODO  Complete this
        allClientHandlers.remove(user, ch);
        sendOnlineMessageToAll();
    }


    //If you find this CLUMSY replace with the stream-version above
    public void sendOnlineMessageToAll(){
        Set<String> allUserNames = allClientHandlers.keySet();
        String onlineString="ONLINE#";
        //Streams and join will make the following a lot simpler :-)
        for(String user: allUserNames){
            onlineString += user +",";
        };
        //Remove the last comma
        final String onlineStringWithUsers = onlineString.substring(0,onlineString.length()-1);
        allClientHandlers.values().forEach((clientHandler -> {
            clientHandler.sendToThisClient(onlineStringWithUsers);
        }));
    }



    public void sendToAll(String msg) throws IOException {
        String messageString="MESSAGE#";
        String[] parts = msg.split(",");
        for (ClientHandler tmp:allClientHandlers.values()) {
            tmp.sendToThisClient(messageString+ parts[0] +"#" + parts[1]);
        }
    }

    public void sendToSingleClient(String msg) throws IOException {
        String[] parts = msg.split(",");

        if (parts.length >= 3){

            //Used to send to specific users
            ClientHandler clientHandler;
            boolean usersFound = false;
            int receivers = parts.length - 2;
            for (String tmp:allClientHandlers.keySet()) {

                for (int i = 1; i < parts.length-1; i++) {
                    if (parts[i].equals(tmp)){
                        clientHandler = allClientHandlers.get(tmp);
                        clientHandler.sendToThisClient("MESSAGE#"+parts[0]+"#"+parts[parts.length-1]);
                        receivers--;
                        if (receivers == 0) {
                            usersFound = true;
                        }
                    }
                }

            }
            if (usersFound == false) {
                ClientHandler client = allClientHandlers.get(parts[0]);
                client.sendToThisClient("CLOSE#2");
                throw new IllegalArgumentException("User not found");
            }
        }
    }



    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started, listening on : " + port);
        while (true) {
            System.out.println("Waiting for a client");
            Socket socket = serverSocket.accept();
            System.out.println("New client connected");
            ClientHandler clientHandler = new ClientHandler(socket,this);
            Thread t2 = new Thread(clientHandler);
            t2.start();
        }

    }

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number, using default port :" + DEFAULT_PORT);
            }
        }
        ChatServer server = new ChatServer();
        server.startServer(port);
    }
}