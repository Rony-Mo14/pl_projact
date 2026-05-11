package healthclub.ui;

import healthclub.filehandler.FileManager;
import healthclub.model.Coach;
import healthclub.model.Member;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import healthclub.sounds.SoundManager;

public class CoachFrame extends JFrame {

    private Coach coach;
    private FileManager fm = new FileManager();

    private final Color ACCENT    = new Color(0xFF4D6D);
    private final Color ICE       = new Color(0x8FD3FF);
    private final Color PURPLE    = new Color(0x7B5EA7);
    private final Color WHITE     = new Color(0xF8FAFC);
    private final Color MUTED     = new Color(0x89A4C7);
    private final Color CARD      = new Color(15, 35, 60, 220);
    private final Color FIELD_BG  = new Color(8, 22, 42);
    private final Color FIELD_BRD = new Color(40, 70, 110);
    private final Color SIDEBAR   = new Color(10, 24, 40);
    private final Color BRAND_COLOR = new Color(0xFFF5E4);

    private List<Orb> orbs;
    private Timer animationTimer;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    private JButton activeNavBtn = null;

    public CoachFrame(Coach coach) {
        this.coach = coach;
        setTitle("Coach Dashboard - FLEX CLUB");
        setSize(960, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initOrbs();
        buildUI();
        setVisible(true);
    }

    private void buildUI() {

        JPanel main = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setPaint(new GradientPaint(0, 0, new Color(4, 18, 35), getWidth(), getHeight(), new Color(12, 38, 74)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                for (Orb orb : orbs) {
                    Point2D center = new Point2D.Float(orb.x, orb.y);
                    float radius = orb.radius;
                    float[] dist = {0.0f, 0.6f, 1.0f};
                    Color[] colors = {orb.color,
                        new Color(orb.color.getRed(), orb.color.getGreen(), orb.color.getBlue(), 30),
                        new Color(0, 0, 0, 0)};
                    RadialGradientPaint rgp = new RadialGradientPaint(center, radius, dist, colors);
                    g2.setPaint(rgp);
                    g2.fillOval((int)(orb.x - radius), (int)(orb.y - radius), (int)(radius*2), (int)(radius*2));
                }
                g2.dispose();
            }
        };

        animationTimer = new Timer(35, e -> {
            for (Orb orb : orbs) { orb.update(getWidth(), getHeight()); }
            main.repaint();
        });
        animationTimer.start();

        // ── Sidebar ──
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(35, 20, 35, 20));

        JLabel title = new JLabel("FLEX CLUB");
        title.setForeground(BRAND_COLOR);
        title.setFont(new Font("Trebuchet MS", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel role = new JLabel("Coach Panel");
        role.setForeground(MUTED);
        role.setFont(new Font("Trebuchet MS", Font.PLAIN, 11));
        role.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel sepLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0),
                    getWidth()/2, 0, new Color(70, 130, 200, 180)));
                g2.fillRect(0, 0, getWidth()/2, getHeight());
                g2.setPaint(new GradientPaint(getWidth()/2, 0, new Color(70, 130, 200, 180),
                    getWidth(), 0, new Color(0, 0, 0, 0)));
                g2.fillRect(getWidth()/2, 0, getWidth()/2, getHeight());
                g2.dispose();
            }
        };
        sepLine.setOpaque(false);
        sepLine.setMaximumSize(new Dimension(190, 2));
        sepLine.setPreferredSize(new Dimension(190, 2));
        sepLine.setAlignmentX(CENTER_ALIGNMENT);

        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(18));
        sidebar.add(sepLine);
        sidebar.add(Box.createVerticalStrut(25));

        String[][] nav = {
            {"👥", "My Members",     "members"},
            {"📋", "Plan & Schedule","plan"},
            {"💬", "Messages",       "msg"},
            {"👤", "My Profile",     "profile"}
        };

        for (String[] item : nav) {
            JButton btn = makeNavBtn(item[0], item[1], item[2].equals("members"));
            btn.setActionCommand(item[2]);

            btn.addActionListener(e -> {
                cardLayout.show(contentPanel, e.getActionCommand());
                for (Component c : sidebar.getComponents()) {
                    if (c instanceof JButton && c != findLogoutBtn(sidebar)) {
                        setNavInactive((JButton) c);
                    }
                }
                setNavActive(btn);
                activeNavBtn = btn;
            });

            if (item[2].equals("members")) {
                activeNavBtn = btn;
            }

            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logout = makeLogoutBtn();
        logout.setName("logout");
        logout.addActionListener(e -> {
            animationTimer.stop();
            dispose();
            new LoginFrame();
        });
        sidebar.add(logout);

        // ── Content ──
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(buildMembersPanel(), "members");
        contentPanel.add(buildPlanPanel(),    "plan");
        contentPanel.add(buildMessagePanel(), "msg");
        contentPanel.add(buildProfilePanel(), "profile");

        main.add(sidebar, BorderLayout.WEST);
        main.add(contentPanel, BorderLayout.CENTER);

        setContentPane(main);
        cardLayout.show(contentPanel, "members");
    }

    private JButton findLogoutBtn(JPanel sidebar) {
        for (Component c : sidebar.getComponents()) {
            if (c instanceof JButton && "logout".equals(((JButton)c).getName())) return (JButton) c;
        }
        return null;
    }

    private void setNavActive(JButton btn) {
        btn.setBackground(new Color(20, 50, 90));
        btn.setForeground(ICE);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 180, 255, 180), 1),
            new EmptyBorder(11, 14, 11, 14)));
    }

    private void setNavInactive(JButton btn) {
        btn.setBackground(new Color(12, 28, 50));
        btn.setForeground(MUTED);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 55, 90), 1),
            new EmptyBorder(11, 14, 11, 14)));
    }

    // ── MEMBERS PANEL ──
    private JPanel buildMembersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel glass = new JPanel(new BorderLayout(0, 15));
        glass.setBackground(CARD);
        glass.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 110, 170, 160), 1, true),
            new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel lbl = new JLabel("My Members");
        lbl.setForeground(WHITE);
        lbl.setFont(new Font("Trebuchet MS", Font.BOLD, 24));
        glass.add(lbl, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Email", "Phone", "Subscription End"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        styleTable(table);

        ArrayList<Member> members = fm.getAllMembers();
        for (Member m : members) {
            if (m.getCoachId() == coach.getId()) {
                model.addRow(new Object[]{m.getId(), m.getName(), m.getEmail(), m.getPhone(), m.getSubscriptionEndDate()});
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(FIELD_BRD, 1));
        scrollPane.getViewport().setBackground(FIELD_BG);

        glass.add(scrollPane, BorderLayout.CENTER);
        wrapper.add(glass, BorderLayout.CENTER);
        return wrapper;
    }

    // ── PLAN PANEL ──
    private JPanel buildPlanPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel glass = new JPanel(new BorderLayout(0, 20));
        glass.setBackground(CARD);
        glass.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 110, 170, 160), 1, true),
            new EmptyBorder(30, 35, 30, 35)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lbl = new JLabel("Plan & Schedule");
        lbl.setForeground(WHITE);
        lbl.setFont(new Font("Trebuchet MS", Font.BOLD, 24));
        JLabel subLbl = new JLabel("Changes will apply to all your members");
        subLbl.setForeground(MUTED);
        subLbl.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
        header.add(lbl,    BorderLayout.NORTH);
        header.add(subLbl, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets  = new Insets(0, 0, 6, 0);

        JTextField planF  = styleField(new JTextField(coach.getPlan()));
        JTextField schedF = styleField(new JTextField(coach.getSchedule()));

        gbc.gridx=0; gbc.gridy=0;
        form.add(makeSmallLabel("TRAINING PLAN"), gbc);
        gbc.gridy=1;
        form.add(planF, gbc);
        gbc.gridy=2;
        gbc.insets = new Insets(16, 0, 6, 0);
        form.add(makeSmallLabel("SCHEDULE / TIMELINE"), gbc);
        gbc.gridy=3;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(schedF, gbc);

        JPanel currentCard = new JPanel(new GridLayout(1, 2, 20, 0));
        currentCard.setBackground(new Color(8, 22, 42));
        currentCard.setBorder(new CompoundBorder(
            new LineBorder(new Color(40, 70, 110), 1, true),
            new EmptyBorder(14, 16, 14, 16)));

        JPanel planInfo = new JPanel();
        planInfo.setOpaque(false);
        planInfo.setLayout(new BoxLayout(planInfo, BoxLayout.Y_AXIS));
        JLabel planKey = makeSmallLabel("CURRENT PLAN");
        JLabel planVal = new JLabel(coach.getPlan().isEmpty() ? "Not set yet" : coach.getPlan());
        planVal.setForeground(ICE);
        planVal.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        planInfo.add(planKey);
        planInfo.add(Box.createVerticalStrut(4));
        planInfo.add(planVal);

        JPanel schedInfo = new JPanel();
        schedInfo.setOpaque(false);
        schedInfo.setLayout(new BoxLayout(schedInfo, BoxLayout.Y_AXIS));
        JLabel schedKey = makeSmallLabel("CURRENT SCHEDULE");
        JLabel schedVal = new JLabel(coach.getSchedule().isEmpty() ? "Not set yet" : coach.getSchedule());
        schedVal.setForeground(new Color(0xF5A623));
        schedVal.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        schedInfo.add(schedKey);
        schedInfo.add(Box.createVerticalStrut(4));
        schedInfo.add(schedVal);

        currentCard.add(planInfo);
        currentCard.add(schedInfo);

        JButton save = makeGradientBtn("💾  Save Plan for All Members");
        save.addActionListener(e -> {
            String plan  = planF.getText().trim();
            String sched = schedF.getText().trim();
            if (plan.isEmpty() || sched.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Plan and Schedule cannot be empty!");
                return;
            }
            coach.setPlan(plan);
            coach.setSchedule(sched);
            fm.updateCoach(coach);
            fm.updateCoachPlan(coach.getId(), plan, sched);
            planVal.setText(plan);
            schedVal.setText(sched);
            JOptionPane.showMessageDialog(this, "Plan & Schedule saved for all your members! ✓");
        });

        glass.add(header,      BorderLayout.NORTH);
        glass.add(form,        BorderLayout.CENTER);
        glass.add(currentCard, BorderLayout.EAST);
        glass.add(save,        BorderLayout.SOUTH);

        wrapper.add(glass, BorderLayout.CENTER);
        return wrapper;
    }

    // ── MESSAGE PANEL ──
    private JPanel buildMessagePanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel glass = new JPanel(new BorderLayout(0, 15));
        glass.setBackground(CARD);
        glass.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 110, 170, 160), 1, true),
            new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel lbl = new JLabel("Send Message", SwingConstants.CENTER);
        lbl.setForeground(WHITE);
        lbl.setFont(new Font("Trebuchet MS", Font.BOLD, 24));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        glass.add(lbl, BorderLayout.NORTH);

        JPanel formBox = new JPanel();
        formBox.setOpaque(false);
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));

        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(FIELD_BG);
        area.setForeground(WHITE);
        area.setCaretColor(WHITE);
        area.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BRD, 1),
            new EmptyBorder(10, 14, 10, 14)
        ));

        JScrollPane areaScroll = new JScrollPane(area);
        areaScroll.setOpaque(false);
        areaScroll.getViewport().setOpaque(false);
        areaScroll.setBorder(BorderFactory.createLineBorder(FIELD_BRD, 1));
        areaScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton send = makeGradientBtn("Send Message");
        send.addActionListener(e -> {
            String msg = area.getText().trim();
            if (msg.isEmpty()) return;
            fm.sendMessageToMembers(coach.getId(), msg);
            SoundManager.playMessage();
            JOptionPane.showMessageDialog(this, "Message Sent!");
            area.setText("");
        });

        formBox.add(makeSmallLabel("MESSAGE"));
        formBox.add(Box.createVerticalStrut(5));
        formBox.add(areaScroll);
        formBox.add(Box.createVerticalStrut(25));
        formBox.add(send);

        glass.add(formBox, BorderLayout.CENTER);
        wrapper.add(glass, BorderLayout.CENTER);
        return wrapper;
    }

    // ── PROFILE PANEL ──
    private JPanel buildProfilePanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel glass = new JPanel(new BorderLayout(0, 20));
        glass.setBackground(CARD);
        glass.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 110, 170, 160), 1, true),
            new EmptyBorder(25, 30, 25, 30)
        ));

        // Header
        JLabel lbl = new JLabel("My Profile");
        lbl.setForeground(WHITE);
        lbl.setFont(new Font("Trebuchet MS", Font.BOLD, 24));
        glass.add(lbl, BorderLayout.NORTH);

        // Form using GridBagLayout: label left / field right
        JPanel formBox = new JPanel(new GridBagLayout());
        formBox.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, 0);

        JTextField nameF  = styleField(new JTextField(coach.getName()));
        JTextField emailF = styleField(new JTextField(coach.getEmail()));
        JTextField phoneF = styleField(new JTextField(coach.getPhone()));
        JTextField passF  = styleField(new JTextField(coach.getPassword()));

        // Name → letters + spaces only
        nameF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && c != ' '
                        && c != java.awt.event.KeyEvent.VK_BACK_SPACE)
                    e.consume();
            }
        });

        // Phone → digits only
        phoneF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()))
                    e.consume();
            }
        });

        // Password → digits only
        passF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()))
                    e.consume();
            }
        });

        // Row helper
        java.util.function.BiConsumer<String, JTextField> addRow = (labelText, field) -> {
            int row = formBox.getComponentCount() / 2;

            gbc.gridx   = 0;
            gbc.gridy   = row;
            gbc.weightx = 0;
            gbc.insets  = new Insets(0, 0, 14, 14);
            JLabel rowLbl = makeSmallLabel(labelText);
            rowLbl.setPreferredSize(new Dimension(90, 20));
            formBox.add(rowLbl, gbc);

            gbc.gridx   = 1;
            gbc.weightx = 1;
            gbc.insets  = new Insets(0, 0, 14, 0);
            formBox.add(field, gbc);
        };

        addRow.accept("NAME",     nameF);
        addRow.accept("EMAIL",    emailF);
        addRow.accept("PHONE",    phoneF);
        addRow.accept("PASSWORD", passF);

        // Save button
        JButton save = makeGradientBtn("Save Changes");
        save.addActionListener(e -> {
            String name  = nameF.getText().trim();
            String email = emailF.getText().trim();
            String phone = phoneF.getText().trim();
            String pass  = passF.getText().trim();

            if (name.isEmpty() || !name.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(this,
                    "Name must contain letters only!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email.toLowerCase().endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this,
                    "Email must end with @gmail.com!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (phone.isEmpty() || !phone.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                    "Phone must contain digits only!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pass.isEmpty() || !pass.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                    "Password must contain digits only!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            coach.setName(name);
            coach.setEmail(email);
            coach.setPhone(phone);
            coach.setPassword(pass);
            fm.updateCoach(coach);
            JOptionPane.showMessageDialog(this, "Profile Updated! ✓");
        });

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(formBox, BorderLayout.NORTH);
        centerPanel.add(save,    BorderLayout.SOUTH);

        glass.add(centerPanel, BorderLayout.CENTER);
        wrapper.add(glass, BorderLayout.CENTER);
        return wrapper;
    }

    // ── UI HELPERS ──

    private JLabel makeSmallLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(0x6B9EC7));
        lbl.setFont(new Font("Trebuchet MS", Font.BOLD, 10));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styleField(JTextField field) {
        field.setBackground(FIELD_BG);
        field.setForeground(WHITE);
        field.setCaretColor(WHITE);
        field.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BRD, 1),
            new EmptyBorder(11, 14, 11, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ICE, 1),
                    new EmptyBorder(11, 14, 11, 14)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FIELD_BRD, 1),
                    new EmptyBorder(11, 14, 11, 14)));
            }
        });
        return field;
    }

    private JButton makeGradientBtn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, PURPLE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(2, 1, getWidth()-4, getHeight()/2-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Trebuchet MS", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setPreferredSize(new Dimension(300, 46));
        return btn;
    }

    private JButton makeNavBtn(String icon, String label, boolean isActive) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setText("<html><table cellpadding='0' cellspacing='0'>" +
            "<tr><td width='28' style='color:#8FD3FF;font-size:14px'>" + icon + "</td>" +
            "<td style='font-family:Trebuchet MS;font-size:13px'>" + label + "</td></tr>" +
            "</table></html>");

        btn.setFocusPainted(false);
        btn.setBackground(isActive ? new Color(20, 50, 90) : new Color(12, 28, 50));
        btn.setForeground(isActive ? ICE : MUTED);
        btn.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(isActive ? new Color(100, 180, 255, 180) : new Color(30, 55, 90), 1),
            new EmptyBorder(11, 14, 11, 14)
        ));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 46));
        btn.setPreferredSize(new Dimension(190, 46));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != activeNavBtn) {
                    btn.setBackground(new Color(18, 42, 75));
                    btn.setForeground(WHITE);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != activeNavBtn) {
                    btn.setBackground(new Color(12, 28, 50));
                    btn.setForeground(MUTED);
                }
            }
        });

        return btn;
    }

    private JButton makeLogoutBtn() {
        JButton btn = new JButton("⟵  Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFocusPainted(false);
        btn.setBackground(new Color(60, 15, 25));
        btn.setForeground(new Color(0xFF7A90));
        btn.setFont(new Font("Trebuchet MS", Font.BOLD, 13));
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 40, 60, 180), 1),
            new EmptyBorder(11, 14, 11, 14)
        ));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 46));
        btn.setPreferredSize(new Dimension(190, 46));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(90, 20, 35));
                btn.setForeground(ACCENT);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 15, 25));
                btn.setForeground(new Color(0xFF7A90));
            }
        });

        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setBackground(FIELD_BG);
        table.setForeground(WHITE);
        table.setSelectionBackground(PURPLE);
        table.setSelectionForeground(WHITE);
        table.setGridColor(FIELD_BRD);
        table.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
        table.setOpaque(false);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(FIELD_BG);
        centerRenderer.setForeground(WHITE);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(8, 22, 42));
        header.setForeground(ICE);
        header.setFont(new Font("Trebuchet MS", Font.BOLD, 12));
        header.setBorder(BorderFactory.createLineBorder(FIELD_BRD));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
    }

    private void initOrbs() {
        orbs = new ArrayList<>();
        Color accentTrans1 = new Color(0xFF4D6D >> 16 & 0xFF, 0xFF4D6D >> 8 & 0xFF, 0xFF4D6D & 0xFF, 60);
        Color iceTrans1    = new Color(0x8FD3FF >> 16 & 0xFF, 0x8FD3FF >> 8 & 0xFF, 0x8FD3FF & 0xFF, 50);
        Color purpleTrans  = new Color(0x7B5EA7 >> 16 & 0xFF, 0x7B5EA7 >> 8 & 0xFF, 0x7B5EA7 & 0xFF, 50);
        Color iceTrans2    = new Color(0x8FD3FF >> 16 & 0xFF, 0x8FD3FF >> 8 & 0xFF, 0x8FD3FF & 0xFF, 40);
        Color accentTrans2 = new Color(0xFF4D6D >> 16 & 0xFF, 0xFF4D6D >> 8 & 0xFF, 0xFF4D6D & 0xFF, 40);

        orbs.add(new Orb(100, 100, 250, accentTrans1, 0.4f, 0.2f));
        orbs.add(new Orb(600, 400, 300, iceTrans1, -0.3f, 0.1f));
        orbs.add(new Orb(300, 500, 220, purpleTrans, 0.2f, -0.4f));
        orbs.add(new Orb(800, 100, 180, iceTrans2, -0.5f, 0.3f));
        orbs.add(new Orb(500, 200, 260, accentTrans2, 0.1f, -0.2f));
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
}