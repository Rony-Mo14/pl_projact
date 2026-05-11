package healthclub.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import healthclub.service.AuthService;
import healthclub.model.*;
import healthclub.filehandler.FileManager;
import healthclub.service.NotificationService;
import java.util.ArrayList;
import java.util.List;
import healthclub.sounds.SoundManager;




public class LoginFrame extends JFrame {

    private AuthService authService = new AuthService();

    // ── Original Premium Color Palette ─────────────────────────
    private final Color ACCENT    = new Color(0xFF4D6D);
    private final Color ICE       = new Color(0x8FD3FF);
    private final Color PURPLE    = new Color(0x7B5EA7);
    private final Color WHITE     = new Color(0xF8FAFC);
    private final Color MUTED     = new Color(0x89A4C7);
    private final Color CARD      = new Color(15, 35, 60, 220);
    private final Color FIELD_BG  = new Color(8, 22, 42);
    private final Color FIELD_BRD = new Color(40, 70, 110);

    // Animation Components
    private List<Orb> orbs;
    private Timer animationTimer;

    public LoginFrame() {
        SoundManager.installGlobalButtonClickSound();
        setTitle("Health Club Management System");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initOrbs();

        JPanel main = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // 1. Base Gradient (Original Navy)
                g2.setPaint(new GradientPaint(0, 0, new Color(4, 18, 35), getWidth(), getHeight(), new Color(12, 38, 74)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // 2. Animated Glowing Orbs (Using Original Colors with Transparency)
                for (Orb orb : orbs) {
                    Point2D center = new Point2D.Float(orb.x, orb.y);
                    float radius = orb.radius;
                    float[] dist = {0.0f, 0.6f, 1.0f};
                    Color[] colors = {orb.color, new Color(orb.color.getRed(), orb.color.getGreen(), orb.color.getBlue(), 30), new Color(0,0,0,0)};
                    RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
                    g2.setPaint(rgp);
                    g2.fillOval((int)(orb.x - radius), (int)(orb.y - radius), (int)(radius*2), (int)(radius*2));
                }
                
                // 3. Original Diagonal Shape Overlay
                Polygon poly = new Polygon();
                poly.addPoint(getWidth() / 2, 0);
                poly.addPoint(getWidth(), 0);
                poly.addPoint(getWidth(), getHeight());
                poly.addPoint(getWidth() / 2 - 80, getHeight());
                g2.setPaint(new GradientPaint(getWidth() / 2, 0, new Color(35, 65, 120, 140), getWidth(), getHeight(), new Color(80, 110, 180, 90)));
                g2.fillPolygon(poly);

                g2.dispose();
            }
        };

        // Animation Loop
        animationTimer = new Timer(35, e -> {
            for (Orb orb : orbs) {
                orb.update(getWidth(), getHeight());
            }
            main.repaint();
        });
        animationTimer.start();

        // ── Sidebar ──────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(10, 24, 40));
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(60, 40, 60, 40));

        JPanel dumbbellPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(45, 85, 140));
                g2.fillRoundRect(20, 22, 100, 10, 5, 5);
                g2.setColor(ACCENT);
                g2.fillRoundRect(10, 14, 16, 26, 4, 4);
                g2.fillRoundRect(114, 14, 16, 26, 4, 4);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(20, 22, 30, 4, 3, 3);
            }
        };
        dumbbellPanel.setOpaque(false);
        dumbbellPanel.setPreferredSize(new Dimension(140, 55));
        dumbbellPanel.setMaximumSize(new Dimension(140, 55));
        dumbbellPanel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel gymName = new JLabel("FLEX CLUB");
        gymName.setFont(new Font("Trebuchet MS", Font.BOLD, 24));
        gymName.setForeground(ICE);
        gymName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Health Club Management");
        tagline.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        tagline.setForeground(MUTED);
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        JPanel sepLine = new JPanel();
        sepLine.setBackground(new Color(45, 85, 140));
        sepLine.setMaximumSize(new Dimension(240, 2));
        sepLine.setAlignmentX(CENTER_ALIGNMENT);

        JLabel welcomeTitle = new JLabel("Welcome Back!");
        welcomeTitle.setFont(new Font("Trebuchet MS", Font.BOLD, 28));
        welcomeTitle.setForeground(WHITE);
        welcomeTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel welcomeSub = new JLabel("Sign in to your account");
        welcomeSub.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
        welcomeSub.setForeground(MUTED);
        welcomeSub.setAlignmentX(CENTER_ALIGNMENT);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 10, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(260, 70));

        for (String s : new String[]{"Members", "Coaches", "Active"}) {
            JPanel card = new JPanel();
            card.setBackground(new Color(20, 38, 60));
            card.setBorder(new EmptyBorder(15, 10, 15, 10));
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            JLabel label = new JLabel(s);
            label.setForeground(ICE);
            label.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
            label.setAlignmentX(CENTER_ALIGNMENT);
            card.add(Box.createVerticalGlue());
            card.add(label);
            card.add(Box.createVerticalGlue());
            statsRow.add(card);
        }

        sidebar.add(dumbbellPanel);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(gymName);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(35));
        sidebar.add(sepLine);
        sidebar.add(Box.createVerticalStrut(40));
        sidebar.add(welcomeTitle);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(welcomeSub);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(statsRow);

        // ── Right form ───────────────────────────────────────
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);

        JPanel glass = new JPanel();
        glass.setBackground(CARD);
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));
        glass.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 110, 170), 1, true),
            new EmptyBorder(40, 45, 40, 45)));
        glass.setPreferredSize(new Dimension(400, 420));

        // ── Title ─────────────────────────────────────────────
        JLabel formTitle = new JLabel("Sign In");
        formTitle.setFont(new Font("Trebuchet MS", Font.BOLD, 30));
        formTitle.setForeground(WHITE);
        formTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel formSub = new JLabel("Enter your credentials");
        formSub.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        formSub.setForeground(MUTED);
        formSub.setAlignmentX(CENTER_ALIGNMENT);

        // ── Email ─────────────────────────────────────────────
        JPanel emailBox = new JPanel();
        emailBox.setOpaque(false);
        emailBox.setLayout(new BoxLayout(emailBox, BoxLayout.Y_AXIS));
        emailBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailBox.setMaximumSize(new Dimension(320, 75));

        JLabel emailLbl = new JLabel("EMAIL ADDRESS");
        emailLbl.setForeground(new Color(0x6B9EC7));
        emailLbl.setFont(new Font("Trebuchet MS", Font.BOLD, 10));
        emailLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailLbl.setPreferredSize(new Dimension(320, 15));
        emailLbl.setMaximumSize(new Dimension(320, 15));
        emailLbl.setHorizontalAlignment(SwingConstants.LEFT);
        
        JTextField emailField = new JTextField("your@email.com");
        emailField.setBackground(FIELD_BG);
        emailField.setForeground(MUTED);
        emailField.setCaretColor(WHITE);
        emailField.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BRD, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        emailField.setMaximumSize(new Dimension(320, 46));

        emailField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (emailField.getText().equals("your@email.com")) { emailField.setText(""); emailField.setForeground(WHITE); }
                emailField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ICE, 1), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (emailField.getText().isEmpty()) { emailField.setText("your@email.com"); emailField.setForeground(MUTED); }
                emailField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FIELD_BRD, 1), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            }
        });
        emailBox.add(emailLbl);
        emailBox.add(Box.createVerticalStrut(5));
        emailBox.add(emailField);

        JPanel passBox = new JPanel();
        passBox.setOpaque(false);
        passBox.setLayout(new BoxLayout(passBox, BoxLayout.Y_AXIS));
        passBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        passBox.setMaximumSize(new Dimension(320, 75));

        JLabel passLbl = new JLabel("PASSWORD");
        passLbl.setForeground(new Color(0x6B9EC7));
        passLbl.setFont(new Font("Trebuchet MS", Font.BOLD, 10));
        passLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        passLbl.setPreferredSize(new Dimension(320, 15));
        passLbl.setMaximumSize(new Dimension(320, 15));
        passLbl.setHorizontalAlignment(SwingConstants.LEFT);

        JPasswordField passField = new JPasswordField("Enter password");
        passField.setBackground(FIELD_BG);
        passField.setForeground(MUTED);
        passField.setCaretColor(WHITE);
        passField.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BRD, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        passField.setMaximumSize(new Dimension(320, 46));
        passField.setEchoChar((char) 0);

        passField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(passField.getPassword()).equals("Enter password")) { passField.setText(""); passField.setEchoChar('●'); passField.setForeground(WHITE); }
                passField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ICE, 1), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (passField.getPassword().length == 0) { passField.setEchoChar((char) 0); passField.setText("Enter password"); passField.setForeground(MUTED); }
                passField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FIELD_BRD, 1), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            }
        });

        passBox.add(passLbl);
        passBox.add(Box.createVerticalStrut(5));
        passBox.add(passField);

        JLabel msgLbl = new JLabel(" ");
        msgLbl.setForeground(ACCENT);
        msgLbl.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
        msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, PURPLE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(2, 1, getWidth()-4, getHeight()/2-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setOpaque(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(320, 50));
        loginBtn.setPreferredSize(new Dimension(320, 50));

        loginBtn.addActionListener(e -> {
            String email    = emailField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (email.isEmpty() || email.equals("your@email.com") || password.isEmpty() || password.equals("Enter password")) {
                msgLbl.setText("Please enter email and password!");
                return;
            }

            String role = authService.login(email, password);

            if (role == null) {
                msgLbl.setText("Invalid email or password!");
                SoundManager.playMessage();
                return;
            }

            SoundManager.playSuccess();
            dispose();
            animationTimer.stop(); // Stop animation on close

            FileManager fm = new FileManager();

            switch (role) {
                case "admin"  -> new AdminFrame();
                case "coach"  -> {
                    Coach c = fm.getAllCoaches().stream().filter(co -> co.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
                    if (c != null) new CoachFrame(c);
                    else JOptionPane.showMessageDialog(null, "Error loading Coach data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                case "member" -> {
                    Member m = fm.getAllMembers().stream().filter(me -> me.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
                    if (m == null) {
                        JOptionPane.showMessageDialog(null, "Error loading Member data.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    NotificationService ns = new NotificationService();
                    String status = ns.checkForMember(String.valueOf(m.getId()));
                    if (status.contains("expired")) {
                        JOptionPane.showMessageDialog(null, "Your subscription has expired.\nPlease renew your membership.", "Subscription Expired", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    new MemberFrame(m);
                }
            }
        });

        // ── Footer ────────────────────────────────────────────
        JLabel footer = new JLabel("Secure Sign In  ·  Health Club v1.0");
        footer.setFont(new Font("Trebuchet MS", Font.PLAIN, 11));
        footer.setForeground(new Color(0x3A5A78));
        footer.setAlignmentX(CENTER_ALIGNMENT);

        // ── Assemble glass ────────────────────────────────────
        glass.add(formTitle);
        glass.add(Box.createVerticalStrut(6));
        glass.add(formSub);
        glass.add(Box.createVerticalStrut(30));
        glass.add(emailLbl);
        glass.add(Box.createVerticalStrut(5));
        glass.add(emailField);
        glass.add(Box.createVerticalStrut(16));
        glass.add(passLbl);
        glass.add(Box.createVerticalStrut(5));
        glass.add(passField);
        glass.add(Box.createVerticalStrut(8));
        glass.add(msgLbl);
        glass.add(Box.createVerticalStrut(16));
        glass.add(loginBtn);
        glass.add(Box.createVerticalStrut(16));
        glass.add(footer);

        right.add(glass);

        main.add(sidebar, BorderLayout.WEST);
        main.add(right,   BorderLayout.CENTER);

        add(main);
        setVisible(true);
    }

    // ── Animation Orbs Logic (Adapted for Original Colors) ────
        // ── Animation Orbs Logic (Fixed Colors) ────
    private void initOrbs() {
        orbs = new ArrayList<>();
        
        // تفكيك الألوان الأصلية وإضافة الشفافية لها (RGBA)
        Color accentTrans1 = new Color(0xFF4D6D >> 16 & 0xFF, 0xFF4D6D >> 8 & 0xFF, 0xFF4D6D & 0xFF, 60);
        Color iceTrans1    = new Color(0x8FD3FF >> 16 & 0xFF, 0x8FD3FF >> 8 & 0xFF, 0x8FD3FF & 0xFF, 50);
        Color purpleTrans  = new Color(0x7B5EA7 >> 16 & 0xFF, 0x7B5EA7 >> 8 & 0xFF, 0x7B5EA7 & 0xFF, 50);
        Color iceTrans2    = new Color(0x8FD3FF >> 16 & 0xFF, 0x8FD3FF >> 8 & 0xFF, 0x8FD3FF & 0xFF, 40);
        Color accentTrans2 = new Color(0xFF4D6D >> 16 & 0xFF, 0xFF4D6D >> 8 & 0xFF, 0xFF4D6D & 0xFF, 40);

        orbs.add(new Orb(100, 100, 250, accentTrans1, 0.4f, 0.2f));  // Accent Pink
        orbs.add(new Orb(600, 400, 300, iceTrans1, -0.3f, 0.1f));   // ICE Blue
        orbs.add(new Orb(300, 500, 220, purpleTrans, 0.2f, -0.4f)); // Purple
        orbs.add(new Orb(800, 100, 180, iceTrans2, -0.5f, 0.3f));   // ICE Blue
        orbs.add(new Orb(500, 200, 260, accentTrans2, 0.1f, -0.2f));// Accent Pink
    }

    static class Orb {
        float x, y, radius, dx, dy;
        Color color;

        Orb(float x, float y, float radius, Color color, float dx, float dy) {
            this.x = x; this.y = y; this.radius = radius;
            this.color = color; this.dx = dx; this.dy = dy;
        }

        void update(int width, int height) {
            x += dx; y += dy;
            if (x < -radius || x > width + radius) dx = -dx;
            if (y < -radius || y > height + radius) dy = -dy;
        }
    }
}