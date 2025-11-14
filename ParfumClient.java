import java.io.*;
import java.net.Socket;

public class ParfumClient {

    private static final String SERVER_IP = "127.0.0.1"; // Ganti dengan IP server jika di jaringan berbeda
    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println("⏳ Mencoba terhubung ke server...");
        try (
            // Implementasi Socket: Membuat koneksi
            Socket socket = new Socket(SERVER_IP, PORT);
            // Implementasi Stream: Input Stream untuk membaca data dari server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.println("🎉 Terhubung ke Broadcaster Server!");
            System.out.println("------------------------------------");

            String stockInfo;
            // Blocking: Klien menunggu data datang melalui Stream
            while ((stockInfo = in.readLine()) != null) { 
                displayStockInfo(stockInfo);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Koneksi ke Server terputus: " + e.getMessage());
        }
    }
    
    // Fungsi untuk menampilkan data yang diterima dengan rapi
    private static void displayStockInfo(String data) {
        // Contoh Parsing data yang masuk dengan format: NAMA_PARFUM;STOK;LINK_MARKETPLACE
        String[] parts = data.split(";");
        
        if (parts.length == 3) {
            System.out.println("\n------------------------------------");
            System.out.println("✨ [STOCK BARU MASUK] ✨");
            System.out.println("📦 Nama Parfum: " + parts[0]);
            System.out.println("🔢 Stok Tersedia: " + parts[1]);
            System.out.println("🔗 Link Pembelian: " + parts[2]);
            System.out.println("------------------------------------");
        } else {
            System.out.println("\n[Pesan Server]: " + data);
        }
    }
}