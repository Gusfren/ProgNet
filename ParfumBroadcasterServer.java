import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

public class ParfumBroadcasterServer {

    private static final int PORT = 8888;
    private final List<ClientHandlerThread> activeClients = Collections.synchronizedList(new ArrayList<>());
    private ServerGUI gui;
 
    public static void main(String[] args) {
        ParfumBroadcasterServer server = new ParfumBroadcasterServer();
        
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI(server);
            server.setGUI(gui);
        });
        
        server.startServer();
    }

    public void setGUI(ServerGUI gui) {
        this.gui = gui;
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                log("✅ Server berjalan, mendengarkan pada port " + PORT);
                
                while (true) {
                    Socket clientSocket = serverSocket.accept(); 
                    
                    ClientHandlerThread clientThread = new ClientHandlerThread(clientSocket, this);
                    
                    activeClients.add(clientThread);
                    clientThread.start();
                    
                    log("➡️ Klien baru terhubung. IP: " + clientThread.getRemoteAddress() + 
                        ". Total klien aktif: " + activeClients.size());
                }
            } catch (IOException e) {
                log("❌ Error Server Listener: " + e.getMessage());
            }
        }).start();
    }

    // Fungsi Broadcast yang Menerima METADATA dan FILE GAMBAR
    public void broadcastImageAlert(String metadata, File imageFile) {
        log("\n📣 [BROADCASTING] Data: " + metadata);
        
        activeClients.forEach(client -> client.sendImageAlert(metadata, imageFile));
    }
    
    // Fungsi log terpusat
    public void log(String message) {
        if (gui != null) {
            SwingUtilities.invokeLater(() -> gui.log(message));
        } else {
            System.out.println(message);
        }
    }
    
    public void removeClient(ClientHandlerThread client) {
        if (activeClients.remove(client)) {
             log("🔌 Klien terputus. IP: " + client.getRemoteAddress() + ". Total klien aktif: " + activeClients.size());
        }
    }
}