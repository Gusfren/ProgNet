import java.awt.*;
import java.io.File;
import javax.swing.*;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JTextField nameField, stockField, linkField, imageField;
    private File selectedImage = null;
    private final ParfumBroadcasterServer server;

    public ServerGUI(ParfumBroadcasterServer server) {
        this.server = server;

        setTitle("Fragrance Flash — Admin Broadcast Console");
        setSize(430, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ===== THEME COLORS =====
        Color bg = new Color(16, 17, 19);
        Color card = new Color(28, 29, 31);
        Color teal = new Color(0, 235, 200);
        Color text = new Color(220, 220, 220);
        Color textDim = new Color(150, 150, 150);

        UIManager.put("ScrollBar.thumb", new Color(55, 56, 60));
        UIManager.put("ScrollBar.track", new Color(25, 25, 28));

        getContentPane().setBackground(bg);
        setLayout(null);

        // ============= HEADER =============
        JPanel header = roundedPanel(card, 20);
        header.setBounds(20, 20, 380, 55);
        header.setLayout(null);

        JLabel icon = new JLabel("✨");
        icon.setBounds(15, 12, 30, 30);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 23));

        JLabel title = new JLabel("Fragrance Flash — ADMIN BROADCAST CONSOLE");
        title.setBounds(50, 17, 330, 20);
        title.setForeground(text);
        title.setFont(new Font("Inter", Font.BOLD, 13));

        header.add(icon);
        header.add(title);
        add(header);

        // ============= SERVER LOG =============
        JPanel logPanel = roundedPanel(card, 20);
        logPanel.setBounds(20, 90, 380, 200);
        logPanel.setLayout(null);

        JLabel logTitle = new JLabel("SERVER LOG");
        logTitle.setForeground(teal);
        logTitle.setFont(new Font("Inter", Font.BOLD, 13));
        logTitle.setBounds(15, 12, 200, 20);
        logPanel.add(logTitle);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(22, 22, 24));
        logArea.setForeground(new Color(185, 185, 185));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBounds(15, 40, 350, 145);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 42)));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        logPanel.add(scroll);
        add(logPanel);

        // ============= FORM PANEL =============
        JPanel form = roundedPanel(card, 20);
        form.setBounds(20, 300, 380, 240);
        form.setLayout(null);

        JLabel formTitle = new JLabel("BROADCAST NEW FRAGRANCE");
        formTitle.setForeground(teal);
        formTitle.setFont(new Font("Inter", Font.BOLD, 13));
        formTitle.setBounds(15, 12, 260, 20);
        form.add(formTitle);

        nameField = createInput(form, "⛫💨  Parfum Name:", 45);
        stockField = createInput(form, "💲  Price :", 95);
        linkField = createInput(form, "🔗  Buy Link:", 145);

        // Image input
        JLabel imgLabel = new JLabel("Image File:");
        imgLabel.setForeground(text);
        imgLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        imgLabel.setBounds(15, 190, 200, 20);
        form.add(imgLabel);

        imageField = new JTextField("No file selected");
        imageField.setBounds(15, 210, 230, 28);
        imageField.setEditable(false);
        imageField.setBackground(new Color(22, 22, 24));
        imageField.setForeground(textDim);
        imageField.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        form.add(imageField);

        JButton choose = new JButton("PILIH");
        choose.setBounds(255, 210, 90, 28);
        styleButton(choose);
        choose.addActionListener(e -> chooseImage());
        form.add(choose);

        add(form);

        // ============= BROADCAST BUTTON =============
        JButton broadcast = new JButton("✦  BROADCAST NOW  ✦");
        broadcast.setBounds(20, 545, 380, 48);
        broadcast.setFont(new Font("Inter", Font.BOLD, 14));
        broadcast.setForeground(Color.BLACK);
        broadcast.setBackground(teal);
        broadcast.setFocusPainted(false);
        broadcast.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        broadcast.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(broadcast);

        broadcast.addActionListener(e -> sendBroadcast());

        // ============= STATUS BAR =============
        JLabel status = new JLabel("Status: Ready...");
        status.setForeground(new Color(0, 255, 180));
        status.setFont(new Font("Consolas", Font.PLAIN, 12));
        status.setBounds(22, 595, 300, 20);
        add(status);

        setVisible(true);
    }

    // PANEL WITH ROUNDED CORNER
    private JPanel roundedPanel(Color color, int radius) {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
            }
        };
    }

    // INPUT FIELD GENERATOR
    private JTextField createInput(JPanel parent, String label, int y) {
        JLabel l = new JLabel(label);
        l.setForeground(new Color(220, 220, 220));
        l.setFont(new Font("Inter", Font.PLAIN, 12));
        l.setBounds(15, y, 200, 20);
        parent.add(l);

        JTextField f = new JTextField();
        f.setBounds(15, y + 22, 330, 28);
        f.setBackground(new Color(22, 22, 24));
        f.setForeground(new Color(230, 230, 230));
        f.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        parent.add(f);
        return f;
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(60, 60, 65));
        btn.setForeground(new Color(230, 230, 230));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = fc.getSelectedFile();
            imageField.setText(selectedImage.getName());
        }
    }

    private void sendBroadcast() {
        if (nameField.getText().isEmpty()) { log("⚠ Parfum name kosong"); return; }
        if (stockField.getText().isEmpty()) { log("⚠ Price/Stock kosong"); return; }
        if (linkField.getText().isEmpty()) { log("⚠ Link kosong"); return; }

        String meta = nameField.getText() + ";" +
                stockField.getText() + ";" +
                linkField.getText();

        server.broadcastImageAlert(meta, selectedImage);
        log("📡 Broadcast sent!");

        nameField.setText("");
        stockField.setText("");
        linkField.setText("");
        imageField.setText("No file selected");
        selectedImage = null;
    }

    public void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
