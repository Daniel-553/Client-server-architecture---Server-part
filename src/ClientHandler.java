import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private PrintWriter out;
    private String username;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if (!server.clients.isEmpty()) {
                out.println("Connected users: " + server.clients.keySet());
            } else {
                out.println("no users connected");
            }
            out.println("Enter your username:");
            this.username = in.readLine();

            server.registerlient(username, this);
            String message;


            while ((message = in.readLine()) != null) {
                if (server.checkMessageForBannedPhrase(message)) {
                    out.println("Message contains banned content and was not sent.");
                } else  if (message.equals("Query")) {
                    out.println(server.bannedWords);
                } else if (message.contains("exclude")){
                    out.println("You have excluded the user from sent message");
                    server.broadcastToAllExceptExcluded(username + ": " + message, this, message);
                } else if(message.contains("whisper to")) {
                    server.privateMessage(username + ": " + message, message);
                }
                else {
                    server.broadcastToAll(username + ": " + message, this);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            server.removeClient(username);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

}

