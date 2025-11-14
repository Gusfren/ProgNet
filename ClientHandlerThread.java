import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;

class ClientHandlerThread extends Thread { 
    
    private final Socket clientSocket;
    private final ParfumBroadcasterServer server;
    private PrintWriter textOut; // Stream Teks untuk Metadata
    private DataOutputStream binaryOut; // Stream Biner untuk Gambar
    private final String remoteAddress;

    public ClientHandlerThread(Socket socket, ParfumBroadcasterServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.remoteAddress = socket.getInetAddress().getHostAddress();
    }
    
    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public void run() {
        try {
            // Setup Stream untuk Teks
            textOut = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Setup Stream untuk Biner (Harus berada di atas output stream yang sama)
            // Penting: DataOutputStream TIDAK boleh auto-flushed, jadi kita bungkus di atas Socket output stream.
            binaryOut = new DataOutputStream(clientSocket.getOutputStream());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            while (in.readLine() != null) {
                // Thread berjalan, menunggu putus koneksi
            }

        } catch (SocketException e) {
            // ... (Penanganan error yang sama) ...
        } catch (IOException e) {
            // ... (Penanganan error yang sama) ...
        } finally {
            server.removeClient(this); 
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // Abaikan
            }
        }
    }
    
    // Fungsi untuk mengirim Metadata dan Gambar Biner
    public void sendImageAlert(String metadata, File imageFile) {
        try {
            // 1. Kirim METADATA (Teks)
            textOut.println(metadata); 
            textOut.flush(); // Penting: Pastikan metadata terkirim sebagai sinyal
            
            // 2. Kirim GAMBAR (Biner)
            if (imageFile != null && imageFile.exists()) {
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                
                // Kirim ukuran file (PENTING untuk klien)
                binaryOut.writeInt(imageBytes.length); 
                // Kirim array gambar
                binaryOut.write(imageBytes);         
                binaryOut.flush();

                server.log("Gambar (" + imageBytes.length + " bytes) berhasil dikirim ke " + remoteAddress);
            } else {
                 // Jika tidak ada gambar, kirim sinyal ukuran 0
                binaryOut.writeInt(0); 
                binaryOut.flush();
            }

        } catch (IOException e) {
            server.log("Error mengirim data/gambar ke klien " + remoteAddress + ": " + e.getMessage());
        }
    }
}