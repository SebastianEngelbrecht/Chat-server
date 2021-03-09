package server;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HandleSendToAllClients implements Runnable {

    BlockingQueue<String> allClientSenderInfo;
    ConcurrentHashMap<String, ClientHandler> allClientHandlers;

    public HandleSendToAllClients(BlockingQueue allClientSenderInfo, ConcurrentHashMap<String, ClientHandler> allClientHandlers) {
        this.allClientSenderInfo = allClientSenderInfo;
        this.allClientHandlers = allClientHandlers;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String messageToSend = allClientSenderInfo.poll(100, TimeUnit.MINUTES);
                if (messageToSend != null) {
                    allClientHandlers.values().forEach(clientHandler -> {
                        clientHandler.sendMessage(messageToSend);
                    });
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}