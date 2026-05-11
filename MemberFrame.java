package healthclub.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.*;
import healthclub.filehandler.FileManager;
import healthclub.model.Member;
import healthclub.service.NotificationService;
import java.util.ArrayList;
import java.util.List;
import healthclub.sounds.SoundManager;

public class MemberFrame extends JFrame {

    private final FileManager         fm    = new FileManager();
    private final NotificationService notif = new NotificationService();
    private final Member member;

    // ── Exact Same Color Palette as LoginFrame ────────────────
    private final Color ACCENT      = new Color(0xFF4D6D);
    private final Color ACCENT_DARK = new Color(0xD63B5A);
    private final Color ICE         = new Color(0x8FD3FF);
    private final Color PURPLE      = new Color(0x7B5EA7);
    private final Color WHITE       = new Color(0xF8FAFC);
    private final Color MUTED       = new Color(0x89A4C7);
    private final Color NAVY        = new Color(4, 18, 35);
    private final Color DARK        = new Color(12, 38, 74);
    private final Color SIDEBAR     = new Color(10, 24, 40);
    private final Color CARD_BG     = new Color(15, 35, 60, 220);
    private final Color CARD_BORDER = new Color(70, 110, 170);
    private final Color FIELD_BG    = new Color(8, 22, 42);
    private final Color FIELD_BORD  = new Color(40, 70, 110);
    private final Color AMBER       = new Color(0xF5A623);
    private final Color GREEN       = new Color(0x34D399);

    // Animation (same as LoginFrame)
    private List<Orb> orbs;
    private Timer animationTimer;

    private JPanel     contentPanel;
    private CardLayout cardLayout;
    private JButton    activeBtn;
    private JPanel     mainPanel;

    public MemberFrame(Member member) {
        this.member = member;
        setTitle("FLEX CLUB — Member Dashboard");
        setSize(960, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initOrbs();

        String alert = notif.checkForMember(String.valueOf(member.getId()));
        if (!alert.startsWith("Subscription active")) {
            JOptionPane.showMessageDialog(this, alert,
                "⚠ Subscription Alert", JOptionPane.WARNING_MESSAGE);
        }

        buildUI();
        setVisible(true);
    }

    // ── Orb Animation (identical to LoginFrame) ───────────────
    private void initOrbs() {
        orbs = new ArrayList<>();
        Color accentTrans1 = new Color(0xFF, 0x4D, 0x6D, 60);
        Color iceTrans1    = new Color(0x8F, 0xD3, 0xFF, 50);
        Color purpleTrans  = new Color(0x7B, 0x5E, 0xA7, 50);
        Color iceTrans2    = new Color(0x8F, 0xD3, 0xFF, 40);
        Color accentTrans2 = new Color(0xFF, 0x4D, 0x6D, 40);

        orbs.add(new Orb(100, 100, 250, accentTrans1,  0.4f,  0.2f));
        orbs.add(new Orb(700, 400, 300, iceTrans1,    -0.3f,  0.1f));
        orbs.add(new Orb(400, 500, 220, purpleTrans,   0.2f, -0.4f));
        orbs.add(new Orb(850, 100, 180, iceTrans2,    -0.5f,  0.3f));
        orbs.add(new Orb(550, 200, 260, accentTrans2,  0.1f, -0.2f));
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
            if (x < -radius || x > width  + radius) dx = -dx;
            if (y < -radius || y > height + radius) dy = -dy;
        }
    }

    // ── Main UI ──────────────────────────────────────────────
    private void buildUI() {
        mainPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // 1. Base Gradient — exact same as LoginFrame
                g2.setPaint(new GradientPaint(0, 0, new Color(4, 18, 35),
                        getWidth(), getHeight(), new Color(12, 38, 74)));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 2. Animated Glowing Orbs
                for (Orb orb : orbs) {
                    Point2D center = new Point2D.Float(orb.x, orb.y);
                    float radius = orb.radius;
                    float[] dist = {0.0f, 0.6f, 1.0f};
                    Color[] colors = {
                        orb.color,
                        new Color(orb.color.getRed(), orb.color.getGreen(), orb.color.getBlue(), 30),
                        new Color(0, 0, 0, 0)
                    };
                    RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
                    g2.setPaint(rgp);
                    g2.fillOval((int)(orb.x - radius), (int)(orb.y - radius),
                                (int)(radius * 2), (int)(radius * 2));
                }

                // 3. Diagonal Shape Overlay — exact same as LoginFrame
                Polygon poly = new Polygon();
                poly.addPoint(getWidth() / 2, 0);
                poly.addPoint(getWidth(), 0);
                poly.addPoint(getWidth(), getHeight());
                poly.addPoint(getWidth() / 2 - 80, getHeight());
                g2.setPaint(new GradientPaint(getWidth() / 2, 0, new Color(35, 65, 120, 140),
                        getWidth(), getHeight(), new Color(80, 110, 180, 90)));
                g2.fillPolygon(poly);

                g2.dispose();
            }
        };

        // Animation Loop
        animationTimer = new Timer(35, e -> {
            for (Orb orb : orbs) orb.update(getWidth(), getHeight());
            mainPanel.repaint();
        });
        animationTimer.start();

        mainPanel.add(buildSidebar(), BorderLayout.WEST);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(buildSubPanel(),     "sub");
        contentPanel.add(buildCoachPanel(),   "coach");
        contentPanel.add(buildMsgPanel(),     "msg");
        contentPanel.add(buildProfilePanel(), "profile");

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    // ── Sidebar ──────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(10, 24, 40));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(40, 0, 24, 0));

        // Dumbbell icon — same style as LoginFrame sidebar
        JPanel dumbbell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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
        dumbbell.setOpaque(false);
        dumbbell.setPreferredSize(new Dimension(140, 55));
        dumbbell.setMaximumSize(new Dimension(140, 55));
        dumbbell.setAlignmentX(CENTER_ALIGNMENT);

        JLabel gymName = new JLabel("FLEX CLUB");
        gymName.setFont(new Font("Trebuchet MS", Font.BOLD, 24));
        gymName.setForeground(ICE);
        gymName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Member Panel");
        roleLabel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        roleLabel.setForeground(MUTED);
        roleLabel.setAlignmentX(CENTER_ALIGNMENT);

        sidebar.add(dumbbell);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(gymName);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(35));
        sidebar.add(makeSep());
        sidebar.add(Box.createVerticalStrut(20));

        String[][] nav = {
            {"📋", "My Subscription", "sub"},
            {"🏋️", "Coach & Plan",   "coach"},
            {"✉",  "Messages",       "msg"},
            {"⚙",  "My Profile",     "profile"}
        };

        for (String[] item : nav) {
            JButton btn = makeNavBtn(item[0], item[1], item[2]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
            if (item[2].equals("sub")) { setActive(btn); activeBtn = btn; }
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(makeSep());
        sidebar.add(Box.createVerticalStrut(8));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        statusPanel.setOpaque(false); statusPanel.setMaximumSize(new Dimension(230, 30));
        statusPanel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel dot = new JLabel("●"); dot.setFont(new Font("SansSerif", Font.PLAIN, 10)); dot.setForeground(GREEN);
        JLabel statusLbl = new JLabel("System Online"); statusLbl.setFont(new Font("Trebuchet MS", Font.PLAIN, 10)); statusLbl.setForeground(MUTED);
        statusPanel.add(dot); statusPanel.add(statusLbl);
        sidebar.add(statusPanel);
        sidebar.add(Box.createVerticalStrut(8));

        JButton logout = makeNavBtn("🚪", "Logout", "logout");
        logout.setForeground(new Color(0xFF6B6B));
        sidebar.add(logout);

        return sidebar;
    }

    private JButton makeNavBtn(String icon, String text, String id) {
        JButton btn = new JButton(icon + "   " + text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (this == activeBtn) {
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 6, 3, getHeight() - 12, 2, 2);
                    g2.setPaint(new GradientPaint(4, 0,
                        new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 30),
                        getWidth(), 0, new Color(0, 0, 0, 0)));
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 8, 8);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(MUTED);
        btn.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(11, 24, 11, 24));
        btn.setMaximumSize(new Dimension(230, 44));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) btn.setForeground(WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeBtn)
                    btn.setForeground(id.equals("logout") ? new Color(0xFF6B6B) : MUTED);
            }
        });

        btn.addActionListener(e -> {
            if (id.equals("logout")) {
                int c = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    animationTimer.stop();
                    dispose();
                    new LoginFrame();
                }
                return;
            }
            if (activeBtn != null) {
                activeBtn.setForeground(MUTED);
                activeBtn.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
                activeBtn.repaint();
            }
            setActive(btn); activeBtn = btn;
            cardLayout.show(contentPanel, id);
        });

        return btn;
    }

    private void setActive(JButton btn) {
        btn.setForeground(WHITE);
        btn.setFont(new Font("Trebuchet MS", Font.BOLD, 13));
        btn.repaint();
    }

    // ── Glass Card (same style as LoginFrame's glass panel) ───
    private JPanel makeGlassCard(int width, int height) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        if (width > 0 && height > 0) card.setPreferredSize(new Dimension(width, height));
        return card;
    }

    // ── Subscription Panel ───────────────────────────────────
    private JPanel buildSubPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(28, 30, 28, 30));

        // Header
        JPanel header = new JPanel(); header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("My Subscription");
        title.setFont(new Font("Trebuchet MS", Font.BOLD, 26)); title.setForeground(WHITE);
        JLabel sub = new JLabel("View your current membership status and details");
        sub.setFont(new Font("Trebuchet MS", Font.PLAIN, 13)); sub.setForeground(MUTED);
        header.add(title); header.add(Box.createVerticalStrut(4)); header.add(sub);
        outer.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);

        JPanel card = makeGlassCard(460, 0);
        card.setLayout(new GridBagLayout());
        card.setBorder(new CompoundBorder(
            new LineBorder(CARD_BORDER, 1, true),
            new EmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 12, 10, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Accent top bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(ACCENT);
        accentBar.setPreferredSize(new Dimension(60, 3));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(accentBar, gbc);

        String alert = notif.checkForMember(String.valueOf(member.getId()));
        Color alertColor = alert.startsWith("Subscription active") ? GREEN
                         : alert.contains("expired") ? ACCENT : AMBER;
        JLabel alertLbl = new JLabel("●  " + alert);
        alertLbl.setFont(new Font("Trebuchet MS", Font.BOLD, 14));
        alertLbl.setForeground(alertColor);
        gbc.gridy = 1; card.add(alertLbl, gbc);

        String[][] details = {
            {"👤  Name",         member.getName()},
            {"📧  Email",        member.getEmail()},
            {"📱  Phone",        member.getPhone()},
            {"🎂  Age",          String.valueOf(member.getAge())},
            {"📅  Sub End Date", member.getSubscriptionEndDate()},
            {"🏅  Coach ID",     String.valueOf(member.getCoachId())}
        };

        gbc.gridwidth = 1;
        for (int i = 0; i < details.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 2; gbc.weightx = 0;
            JLabel key = new JLabel(details[i][0]);
            key.setForeground(MUTED); key.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
            key.setPreferredSize(new Dimension(150, 22));
            card.add(key, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            JLabel val = new JLabel(details[i][1]);
            val.setFont(new Font("Trebuchet MS", Font.PLAIN, 13)); val.setForeground(WHITE);
            card.add(val, gbc);
        }

        center.add(card);
        outer.add(center, BorderLayout.CENTER);
        return outer;
    }

    // ── Coach Panel ──────────────────────────────────────────
    private JPanel buildCoachPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel header = new JPanel(); header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("My Coach & Plan");
        title.setFont(new Font("Trebuchet MS", Font.BOLD, 26)); title.setForeground(WHITE);
        JLabel sub = new JLabel("View your assigned coach details and schedule");
        sub.setFont(new Font("Trebuchet MS", Font.PLAIN, 13)); sub.setForeground(MUTED);
        header.add(title); header.add(Box.createVerticalStrut(4)); header.add(sub);
        outer.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);

        JPanel card = makeGlassCard(560, 0);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new CompoundBorder(
            new LineBorder(CARD_BORDER, 1, true),
            new EmptyBorder(28, 36, 28, 36)));

        JLabel cardTitle = new JLabel("🏋️  Coach & Schedule Info");
        cardTitle.setFont(new Font("Trebuchet MS", Font.BOLD, 16)); cardTitle.setForeground(WHITE);

        JLabel infoLbl = new JLabel();
        infoLbl.setFont(new Font("Trebuchet MS", Font.PLAIN, 14)); infoLbl.setForeground(WHITE);

        Runnable loadData = () -> {
            String info = fm.getMemberCoachInfo(member.getId());
            if (info == null || info.isEmpty() || info.equals("No coach assigned yet.")) {
                infoLbl.setText("<html><span style='color:#89A4C7'>" +
                    "No coach assigned yet. Please contact admin.</span></html>");
            } else {
                String formatted = info
                    .replace("Coach: ",     "<b style='color:#8FD3FF'>Coach: </b>")
                    .replace("| Plan: ",    "<br><br><b style='color:#F5A623'>Plan: </b>")
                    .replace("| Schedule: ","<br><br><b style='color:#34D399'>Schedule: </b>");
                infoLbl.setText("<html>" + formatted + "</html>");
            }
        };
        loadData.run();

        // Refresh button — same gradient style as login button
        JButton refreshBtn = new JButton("↻  Refresh") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(ICE.getRed(), ICE.getGreen(), ICE.getBlue(), 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(ICE.getRed(), ICE.getGreen(), ICE.getBlue(), 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        refreshBtn.setForeground(ICE);
        refreshBtn.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
        refreshBtn.setFocusPainted(false); refreshBtn.setBorderPainted(false);
        refreshBtn.setContentAreaFilled(false); refreshBtn.setOpaque(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        refreshBtn.addActionListener(e -> {
            loadData.run();
            JOptionPane.showMessageDialog(this, "Data refreshed!", "✓", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel topBar = new JPanel(new BorderLayout()); topBar.setOpaque(false);
        topBar.add(cardTitle,  BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        card.add(topBar,  BorderLayout.NORTH);
        card.add(infoLbl, BorderLayout.CENTER);

        center.add(card);
        outer.add(center, BorderLayout.CENTER);
        return outer;
    }

    // ── Messages Panel ───────────────────────────────────────
    private JPanel buildMsgPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 20));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel header = new JPanel(); header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Messages");
        title.setFont(new Font("Trebuchet MS", Font.BOLD, 26)); title.setForeground(WHITE);
        JLabel sub = new JLabel("Read messages from your assigned coach");
        sub.setFont(new Font("Trebuchet MS", Font.PLAIN, 13)); sub.setForeground(MUTED);
        header.add(title); header.add(Box.createVerticalStrut(4)); header.add(sub);
        outer.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);

        JPanel card = makeGlassCard(560, 300);
        card.setLayout(new BorderLayout());
        card.setBorder(new CompoundBorder(
            new LineBorder(CARD_BORDER, 1, true),
            new EmptyBorder(24, 30, 24, 30)));

        JLabel cardTitle = new JLabel("✉  Inbox");
        cardTitle.setFont(new Font("Trebuchet MS", Font.BOLD, 16)); cardTitle.setForeground(WHITE);
        card.add(cardTitle, BorderLayout.NORTH);

        String messages = fm.getMessagesString(member.getCoachId());
        JTextArea ta = new JTextArea(messages.isEmpty() ? "No messages yet." : messages);
        ta.setEditable(false); ta.setBackground(FIELD_BG); ta.setForeground(WHITE);
        ta.setFont(new Font("Trebuchet MS", Font.PLAIN, 13)); ta.setCaretColor(ICE);
        ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        card.add(makeScroll(ta), BorderLayout.CENTER);
        center.add(card);
        outer.add(center, BorderLayout.CENTER);
        return outer;
    }

    // ── Profile Panel — GridBagLayout for reliable rendering ──
    private JPanel buildProfilePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Header
        JPanel header = new JPanel(); header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Trebuchet MS", Font.BOLD, 26)); title.setForeground(WHITE);
        JLabel sub = new JLabel("Update your personal information");
        sub.setFont(new Font("Trebuchet MS", Font.PLAIN, 13)); sub.setForeground(MUTED);
        header.add(title); header.add(Box.createVerticalStrut(4)); header.add(sub);
        outer.add(header, BorderLayout.NORTH);

        // Fields
        JTextField nameF  = makeLoginStyleField();  nameF.setText(member.getName());
        JTextField emailF = makeLoginStyleField();  emailF.setText(member.getEmail());
        JPasswordField passF = makeLoginStylePasswordField(); passF.setText(member.getPassword());
        JTextField phoneF = makeLoginStyleField();  phoneF.setText(member.getPhone());
        JTextField ageF   = makeLoginStyleField();  ageF.setText(String.valueOf(member.getAge()));
        applyNumberFilter(passF);
        applyNumberFilter(phoneF);
        applyNumberFilter(ageF);

        JLabel msgLbl = new JLabel(" ");
        msgLbl.setForeground(ACCENT);
        msgLbl.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));

        // Save button
        JButton saveBtn = new JButton("Save Changes") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), 0, PURPLE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(2, 1, getWidth() - 4, getHeight() / 2 - 1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
        saveBtn.setFocusPainted(false); saveBtn.setBorderPainted(false);
        saveBtn.setContentAreaFilled(false); saveBtn.setOpaque(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(340, 46));

        saveBtn.addActionListener(e -> {
            String email = emailF.getText().trim();
            String pass  = new String(passF.getPassword()).trim();
            if (nameF.getText().trim().isEmpty() || email.isEmpty() || pass.isEmpty()) {
                msgLbl.setForeground(ACCENT); msgLbl.setText("Name, Email and Password are required!"); return;
            }
            if (!email.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
                msgLbl.setForeground(ACCENT); msgLbl.setText("Invalid email format."); return;
            }
            try {
                int ageVal = Integer.parseInt(ageF.getText().trim());
                if (ageVal <= 0 || ageVal > 120) { msgLbl.setForeground(ACCENT); msgLbl.setText("Invalid age."); return; }
                fm.updateMember(member.getId(), nameF.getText().trim(), email,
                    pass, phoneF.getText().trim(), ageVal, member.getSubscriptionEndDate());
                msgLbl.setForeground(GREEN); msgLbl.setText("Profile updated successfully!");
            } catch (NumberFormatException ex) {
                msgLbl.setForeground(ACCENT); msgLbl.setText("Age must be a valid number.");
            }
        });

        // ── Glass card using GridBagLayout ────────────────────
        JPanel glass = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        glass.setOpaque(false);
        glass.setPreferredSize(new Dimension(420, 530));

        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(0, 0, 0, 0);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridx   = 0;

        // Avatar row
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sz = 56, cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setPaint(new GradientPaint(cx - sz/2, cy - sz/2, ACCENT, cx + sz/2, cy + sz/2, PURPLE));
                g2.fillOval(cx - sz/2, cy - sz/2, sz, sz);
                g2.setColor(WHITE);
                g2.setFont(new Font("Trebuchet MS", Font.BOLD, 22));
                FontMetrics fmx = g2.getFontMetrics();
                String ini = member.getName().isEmpty() ? "M"
                    : String.valueOf(member.getName().charAt(0)).toUpperCase();
                g2.drawString(ini, cx - fmx.stringWidth(ini) / 2, cy + fmx.getAscent() / 2 - 2);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(340, 66));
        g.gridy = 0; g.insets = new Insets(24, 40, 4, 40);
        glass.add(avatar, g);

        JLabel idLbl = new JLabel("ID: " + member.getId() + "   (Cannot be changed)", SwingConstants.CENTER);
        idLbl.setFont(new Font("Trebuchet MS", Font.PLAIN, 11));
        idLbl.setForeground(new Color(0x3A5A78));
        g.gridy = 1; g.insets = new Insets(0, 40, 18, 40);
        glass.add(idLbl, g);

        // Label + field pairs
        String[] labels = {"NAME", "EMAIL ADDRESS", "PASSWORD", "PHONE", "AGE"};
        JComponent[] fields = {nameF, emailF, passF, phoneF, ageF};

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setForeground(new Color(0x6B9EC7));
            lbl.setFont(new Font("Trebuchet MS", Font.BOLD, 10));
            g.gridy = 2 + i * 2;
            g.insets = new Insets(i == 0 ? 0 : 10, 40, 4, 40);
            glass.add(lbl, g);

            fields[i].setPreferredSize(new Dimension(340, 46));
            g.gridy = 3 + i * 2;
            g.insets = new Insets(0, 40, 0, 40);
            glass.add(fields[i], g);
        }

        // Msg label
        g.gridy = 12; g.insets = new Insets(8, 40, 0, 40);
        glass.add(msgLbl, g);

        // Save button
        g.gridy = 13; g.insets = new Insets(10, 40, 10, 40);
        glass.add(saveBtn, g);

        // Footer
        JLabel footer = new JLabel("Secure Update  ·  Health Club v1.0", SwingConstants.CENTER);
        footer.setFont(new Font("Trebuchet MS", Font.PLAIN, 11));
        footer.setForeground(new Color(0x3A5A78));
        g.gridy = 14; g.insets = new Insets(0, 40, 20, 40);
        glass.add(footer, g);

        // Spacer to push everything up
        g.gridy = 15; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        glass.add(Box.createGlue(), g);

        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        center.add(glass);
        outer.add(center, BorderLayout.CENTER);
        return outer;
    }

    // ── Login-style Field (matches LoginFrame exactly) ────────
    private JTextField makeLoginStyleField() {
        JTextField f = new JTextField();
        f.setBackground(FIELD_BG);
        f.setForeground(WHITE);
        f.setCaretColor(WHITE);
        f.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORD, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ICE, 1),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FIELD_BORD, 1),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            }
        });
        return f;
    }

    private JPasswordField makeLoginStylePasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setBackground(FIELD_BG);
        pf.setForeground(WHITE);
        pf.setCaretColor(WHITE);
        pf.setEchoChar('●');
        pf.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORD, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ICE, 1),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FIELD_BORD, 1),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            }
        });
        return pf;
    }

    // ── Helpers ──────────────────────────────────────────────
    private Component makeSep() {
        JPanel s = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0),
                    getWidth() / 2, 0, new Color(45, 85, 140, 120), true));
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        s.setOpaque(false);
        s.setMaximumSize(new Dimension(190, 1));
        s.setAlignmentX(CENTER_ALIGNMENT);
        return s;
    }

    private JScrollPane makeScroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setOpaque(false); s.getViewport().setOpaque(false);
        s.setBorder(new LineBorder(FIELD_BORD, 1, true));
        s.getVerticalScrollBar().setUnitIncrement(12);
        s.getVerticalScrollBar().setBackground(FIELD_BG);
        return s;
    }

    // ── Numbers-only DocumentFilter ───────────────────────────
    private static class NumberOnlyFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            if (text != null && text.matches("[0-9]+"))
                super.insertString(fb, offset, text, attr);
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            if (text != null && text.matches("[0-9]*"))
                super.replace(fb, offset, length, text, attr);
        }
    }

    // ── Apply filter to any JTextField or JPasswordField ─────
    private static void applyNumberFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumberOnlyFilter());
    }
}