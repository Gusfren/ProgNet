import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ClientGUI extends JFrame {
    private final JTextPane stockLogPane; 
    private final JLabel statusLabel;
    
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 8888;

    public ClientGUI() {
        setTitle("✨ Scent Tracker - Realtime Alert (Gambar Aktif)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        Color statusBg = new Color(220, 220, 220); 

        // --- Status Bar (NORTH) ---
        statusLabel = new JLabel("Status: Disconnected", SwingConstants.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(statusBg);
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(statusLabel, BorderLayout.NORTH);

        // --- Stock Log Area (CENTER) - Menggunakan JTextPane ---
        stockLogPane = new JTextPane(); 
        stockLogPane.setEditable(false);
        stockLogPane.setBackground(new Color(245, 245, 245)); 
        stockLogPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(stockLogPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        add(scrollPane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public void startClient() { 
        // Multithreading: Koneksi dijalankan di thread terpisah (Non-EDT)
        new Thread(() -> { 
            try {
                Socket socket = new Socket(SERVER_IP, PORT);
                
                BufferedReader textIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataInputStream binaryIn = new DataInputStream(socket.getInputStream());

                updateStatus("Connected (Listening for Alerts)", Color.BLUE);
                
                String metadata;
                while ((metadata = textIn.readLine()) != null) { 
                    // === LOGIC I/O BINER (Di thread terpisah) ===
                    // Baca UKURAN file biner
                    int imageSize = binaryIn.readInt();
                    byte[] imageBytes = null;
                    
                    if (imageSize > 0) {
                        // Baca BYTE ARRAY GAMBAR (Operasi Biner Blocking)
                        imageBytes = new byte[imageSize];
                        binaryIn.readFully(imageBytes); 
                    }
                    
                    // Panggil fungsi display di EDT dengan data yang sudah LENGKAP
                    displayAlert(metadata, imageBytes); 
                    // ============================================
                }
                
                socket.close();
            } catch (UnknownHostException e) {
                updateStatus("Server IP Address not found.", Color.RED);
            } catch (IOException e) {
                updateStatus("Connection Lost or Failed.", Color.RED);
            }
        }).start();
    }
    
    // Fungsi untuk menampilkan Metadata dan Gambar (Dipanggil dari thread startClient)
    private void displayAlert(String metadata, byte[] imageBytes) {
        // Semua operasi GUI harus di EDT
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = stockLogPane.getStyledDocument();
            Style defaultStyle = stockLogPane.addStyle("Default", null);
            StyleConstants.setFontFamily(defaultStyle, "Monospaced");
            StyleConstants.setFontSize(defaultStyle, 12);
            
            String[] parts = metadata.split(";");

            try {
                // 1. Tambahkan Header Teks
                doc.insertString(doc.getLength(), "\n" + "=".repeat(45) + "\n", defaultStyle);
                doc.insertString(doc.getLength(), String.format("🔔 NEW FRAGRANCE DROP! | %tH:%tM:%tS\n", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis()), defaultStyle);
                doc.insertString(doc.getLength(), "=".repeat(45) + "\n", defaultStyle);

                // 2. Tambahkan Metadata
                if (parts.length == 3) {
                    doc.insertString(doc.getLength(), "  🧴 NAMA: " + parts[0] + "\n", defaultStyle);
                    doc.insertString(doc.getLength(), "  🏷️ HARGA: " + parts[1] + "\n", defaultStyle);
                    doc.insertString(doc.getLength(), "  🛒 LINK: " + parts[2] + "\n", defaultStyle);
                } else {
                    doc.insertString(doc.getLength(), "  [Pesan Admin]: " + metadata + "\n", defaultStyle);
                }

                // 3. Tambahkan Gambar (Jika ada)
                if (imageBytes != null && imageBytes.length > 0) {
                    try {
                        // Memproses gambar di EDT (dengan data yang sudah dibaca)
                        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        
                        if (originalImage != null) {
                            ImageIcon imageIcon = new ImageIcon(originalImage);
                            
                            // Scaling gambar
                            int width = imageIcon.getIconWidth();
                            int height = imageIcon.getIconHeight();
                            if (width > 200) { 
                                height = (200 * height) / width;
                                width = 200;
                            }
                            Image scaledImage = imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                            imageIcon.setImage(scaledImage);
                            
                            doc.insertString(doc.getLength(), "  🖼️ Parfum Image:\n", defaultStyle);
                            
                            Style iconStyle = stockLogPane.addStyle("IconStyle", defaultStyle);
                            StyleConstants.setIcon(iconStyle, imageIcon);
                            doc.insertString(doc.getLength(), "  ", iconStyle); 
                            doc.insertString(doc.getLength(), "\n", defaultStyle); 
                        } else {
                            doc.insertString(doc.getLength(), "  ❌ Gambar tidak valid atau rusak.\n", defaultStyle);
                        }
                    } catch (IOException e) {
                        doc.insertString(doc.getLength(), "  ❌ Error memproses gambar: " + e.getMessage() + "\n", defaultStyle);
                    }
                } else {
                    doc.insertString(doc.getLength(), "  (Tidak ada gambar yang dikirim)\n", defaultStyle);
                }
                
                doc.insertString(doc.getLength(), "-------------------------------------------\n", defaultStyle);
                
            } catch (BadLocationException e) {
                System.err.println("Error inserting text/image: " + e.getMessage());
            }
            
            // Auto-scroll ke bawah
            stockLogPane.setCaretPosition(stockLogPane.getDocument().getLength());
        });
    }
    
    private void updateStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + text);
            statusLabel.setForeground(color);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI client = new ClientGUI();
            client.startClient(); 
        });
    }
}