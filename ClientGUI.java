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
    
    // ===== THEME COLORS (from ServerGUI) =====
    private static final Color BG = new Color(16, 17, 19);
    private static final Color CARD = new Color(28, 29, 31);
    private static final Color TEAL = new Color(0, 235, 200);
    private static final Color TEXT = new Color(220, 220, 220);
    private static final Color BRIGHT_RED = new Color(255, 80, 80); // Untuk error

    public ClientGUI() {
        setTitle("✨ Fragrance Flash");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set main background
        getContentPane().setBackground(BG);
        
        // Atur warna ScrollBar secara global agar sesuai dengan ServerGUI
        UIManager.put("ScrollBar.thumb", new Color(55, 56, 60));
        UIManager.put("ScrollBar.track", new Color(25, 25, 28));

        // --- Status Bar (NORTH) ---
        statusLabel = new JLabel("Status: Disconnected", SwingConstants.CENTER);
        statusLabel.setOpaque(true);
        // Terapkan dark theme
        statusLabel.setBackground(CARD);
        statusLabel.setForeground(TEXT);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(statusLabel, BorderLayout.NORTH);

        // --- Stock Log Area (CENTER) - Menggunakan JTextPane ---
        stockLogPane = new JTextPane(); 
        stockLogPane.setEditable(false);
        // Terapkan dark theme
        stockLogPane.setBackground(BG); 
        stockLogPane.setForeground(TEXT); // Warna teks default untuk antisipasi
        stockLogPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        // === Membuat LINK bisa di-klik ===
stockLogPane.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        int pos = stockLogPane.viewToModel2D(e.getPoint());
        try {
            String text = stockLogPane.getDocument().getText(0, stockLogPane.getDocument().getLength());

            // Cari URL pola http atau https
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(https?://\\S+)");
            java.util.regex.Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                if (pos >= start && pos <= end) {
                    String url = matcher.group();
                    Desktop.getDesktop().browse(new java.net.URI(url));
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
});

        JScrollPane scrollPane = new JScrollPane(stockLogPane);
        // Berikan border tipis agar mirip dengan card di ServerGUI
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD.darker(), 1)); 
        add(scrollPane, BorderLayout.CENTER);

        
        pack();
        setSize(510, 900);     // ukuran default
        setResizable(false);   // kunci ukuran
        setLocationRelativeTo(null);
        setVisible(true);

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
            // Pastikan teks default berwarna terang
            StyleConstants.setForeground(defaultStyle, TEXT); 

            // Style untuk aksen TEAL
            Style tealStyle = stockLogPane.addStyle("Teal", defaultStyle);
            StyleConstants.setForeground(tealStyle, TEAL);
            StyleConstants.setBold(tealStyle, true);
            
            String[] parts = metadata.split(";");

            try {
                // 1. Tambahkan Header Teks
                doc.insertString(doc.getLength(), "\n" + "=".repeat(65) + "\n", defaultStyle);
                // Gunakan TEAL style untuk judul alert
                doc.insertString(doc.getLength(), String.format("🔔 NEW FRAGRANCE DROP! | %tH:%tM:%tS\n", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis()), tealStyle);
                doc.insertString(doc.getLength(), "=".repeat(65) + "\n", defaultStyle);

                // 2. Tambahkan Metadata
                if (parts.length == 3) {
                    doc.insertString(doc.getLength(), "  ⛫💨 Name: " + parts[0] + "\n", defaultStyle);
                    doc.insertString(doc.getLength(), "  💲 Price: " + parts[1] + "\n", defaultStyle);
                    doc.insertString(doc.getLength(), "  🔗 Link: " + parts[2] + "\n", defaultStyle);
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
                            
                            doc.insertString(doc.getLength(), "  Parfum Image:\n", defaultStyle);
                            
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
                
                doc.insertString(doc.getLength(), "=".repeat(65), defaultStyle);
                
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
            
            // Sesuaikan warna status dengan dark theme:
            if (color == Color.RED) {
                statusLabel.setForeground(BRIGHT_RED); // Gunakan merah terang untuk error
            } else if (color == Color.BLUE) {
                statusLabel.setForeground(TEAL); // Gunakan TEAL untuk connected/success
            } else {
                statusLabel.setForeground(TEXT);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI client = new ClientGUI();
            client.startClient(); 
        });
    }
}