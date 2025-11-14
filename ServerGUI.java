import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.border.Border;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JTextField nameField, stockField, linkField, imagePathField; 
    private ParfumBroadcasterServer server; 
    private File selectedImageFile = null; // Variable untuk menyimpan file yang dipilih

    public ServerGUI(ParfumBroadcasterServer server) {
        this.server = server;
        setTitle("🧴 AromaLink - Admin Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Color primaryBg = new Color(250, 250, 255); 
        getContentPane().setBackground(primaryBg);
        setLayout(new BorderLayout(15, 15)); 

        Color accentGreen = new Color(144, 238, 144); 
        Color secondaryGray = new Color(105, 105, 105); 

        // --- Log Area ---
        // ... (Kode Log Area yang sama) ...
        logArea = new JTextArea(15, 55); // Diperkecil sedikit
        logArea.setEditable(false);
        logArea.setBackground(Color.WHITE);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        Border border = BorderFactory.createLineBorder(secondaryGray, 1);
        logArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(border, "SERVER LOG", javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), secondaryGray));
        add(scrollPane, BorderLayout.CENTER);

        // --- Input Panel (Form) ---
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // Ubah menjadi 5 baris
        inputPanel.setBackground(primaryBg);
        inputPanel.setBorder(BorderFactory.createTitledBorder(border, "BROADCAST NEW FRAGRANCE",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 12), secondaryGray));
        
        // Input Text
        inputPanel.add(new JLabel("🧴 Parfum Nama:")); 
        inputPanel.add(nameField = new JTextField());
        inputPanel.add(new JLabel("💰 Harga/Stok:")); 
        inputPanel.add(stockField = new JTextField());
        inputPanel.add(new JLabel("🔗 Link Pembelian:")); 
        inputPanel.add(linkField = new JTextField());
        
        // Input Gambar (Baru)
        inputPanel.add(new JLabel("🖼️ File Gambar:")); 
        JPanel imageSelectPanel = new JPanel(new BorderLayout());
        imagePathField = new JTextField("No file selected");
        imagePathField.setEditable(false);
        JButton chooseImageButton = new JButton("Pilih...");
        
        chooseImageButton.addActionListener(e -> selectImageFile());
        
        imageSelectPanel.add(imagePathField, BorderLayout.CENTER);
        imageSelectPanel.add(chooseImageButton, BorderLayout.EAST);
        inputPanel.add(imageSelectPanel);
        
        // Tombol Broadcast
        inputPanel.add(new JLabel("")); // Placeholder
        JButton broadcastButton = new JButton("✨ AROMA BROADCAST ✨");
        broadcastButton.setBackground(accentGreen); 
        broadcastButton.setForeground(Color.BLACK);
        broadcastButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        broadcastButton.setFocusPainted(false); 
        inputPanel.add(broadcastButton);

        broadcastButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String stock = stockField.getText().trim();
                String link = linkField.getText().trim();
                
                if (!name.isEmpty() && !stock.isEmpty() && !link.isEmpty()) {
                    String metadata = name + ";" + stock + ";" + link; 
                    // Panggil fungsi broadcast BARU
                    server.broadcastImageAlert(metadata, selectedImageFile); 
                    
                    // Bersihkan form
                    nameField.setText("");
                    stockField.setText("");
                    linkField.setText("");
                    imagePathField.setText("No file selected");
                    selectedImageFile = null;
                } else {
                    log("⚠️ Error: Field teks harus diisi.");
                }
            }
        });
        
        add(inputPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null); 
        setVisible(true);
    }
    
    private void selectImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Gambar Parfum (JPG/PNG)");
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            imagePathField.setText(selectedImageFile.getAbsolutePath());
        } else {
            selectedImageFile = null;
            imagePathField.setText("No file selected");
        }
    }
    
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); 
        });
    }
}