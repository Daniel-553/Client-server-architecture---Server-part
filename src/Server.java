import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private int port;
    private String name;
    Set<String> bannedWords = new HashSet<>();
    Map<String, ClientHandler> clients = new HashMap<>();

    public Server(String configFile) {
        loadConfig(configFile);

    }

    private void loadConfig(String configFile) {
        Properties config = new Properties();
        try(FileInputStream fileInputStream = new FileInputStream(configFile)) {
            config.load(fileInputStream);
            this.port = Integer.parseInt(config.getProperty("Port"));
            this.name = config.getProperty("Name");
            String bannedPhrases = config.getProperty("BannedWOrds");
            if (bannedPhrases != null) {
                bannedWords.addAll(Arrays.asList(bannedPhrases.split(" ")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    synchronized void registerlient(String name, ClientHandler clientHandler) {
        clients.put(name, clientHandler);
        broadcastToAll("User " + name + " has joined the server.", null);

    }
    synchronized void removeClient(String name) {
        clients.remove(name);
        broadcastToAll("User " + name + " has left the chat.", null);


    }
    synchronized void broadcastToAll(String message,ClientHandler excludeClient) {
        clients.forEach((username, client) -> {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        });
    }
    synchronized void broadcastToAllExceptExcluded(String message,ClientHandler excludeClient, String excludedUser) {
        clients.forEach((username, client) -> {
            if (client != excludeClient && !excludedUser.contains(username)) {
                client.sendMessage(message);
            }
        });
    }
    synchronized void privateMessage(String message, String meantUser) {
        clients.forEach((username, client) -> {
            if (meantUser.contains(username)) {
                client.sendMessage(message);
            }
        });


    }
    void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(name + " server started on port " + port + bannedWords);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkMessageForBannedPhrase(String message) {
        boolean check = false;
        for(String phrase : bannedWords) {
            if (message.contains(phrase)) {
                check = true;
                break;
            }
        }
        return check;
    }





}
