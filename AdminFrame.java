package healthclub.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import healthclub.filehandler.FileManager;
import healthclub.model.*;
import healthclub.service.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import healthclub.sounds.SoundManager;

public class AdminFrame extends JFrame {

    private final FileManager         fm    = new FileManager();
    private final BillingService      bill  = new BillingService();
    private final NotificationService notif = new NotificationService();
    private final AuthService         auth  = new AuthService();

    private final Color ACCENT      = new Color(0xFF4D6D);
    private final Color ACCENT_DARK = new Color(0xD63B5A);
    private final Color ICE         = new Color(0x8FD3FF);
    private final Color WHITE       = new Color(0xF8FAFC);
    private final Color MUTED       = new Color(0x7B93B0);
    private final Color NAVY        = new Color(4, 18, 35);
    private final Color DARK        = new Color(12, 38, 74);
    private final Color SIDEBAR     = new Color(8, 18, 32);
    private final Color CARD_BG     = new Color(18, 36, 64);
    private final Color CARD_BORDER = new Color(50, 80, 130);
    private final Color FIELD_BG    = new Color(10, 22, 40);
    private final Color FIELD_BORD  = new Color(40, 65, 105);
    private final Color AMBER       = new Color(0xF5A623);
    private final Color PURPLE      = new Color(0x7B5EA7);
    private final Color GREEN       = new Color(0x34D399);
    private final Color ROW_ALT     = new Color(18, 36, 62);
    private final Color ROW_NORM    = new Color(12, 28, 50);
    private final Color HEADER_BG   = new Color(6, 14, 28);

    private JPanel     contentPanel;
    private CardLayout cardLayout;
    private JButton    activeBtn;

    // ── Validation Helpers ───────────────────────────────────

    /**
     * الاسم لازم يحتوي على حروف فقط (مسافات مسموح بيها) — بدون أرقام
     */
    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.matches("[\\p{L} .'-]+");
    }

    /**
     * الباسورد أرقام فقط
     */
    private boolean isValidPassword(String pass) {
        return pass != null && !pass.trim().isEmpty() && pass.matches("\\d+");
    }

    /**
     * رقم التليفون أرقام فقط
     */
    private boolean isValidPhone(String phone) {
        return phone != null && !phone.trim().isEmpty() && phone.matches("\\d+");
    }

    
    private boolean isValidAge(String ageStr) {
        if (ageStr == null || ageStr.trim().isEmpty()) return false;
        if (!ageStr.trim().matches("\\d+")) return false;
        int age = Integer.parseInt(ageStr.trim());
        return age > 0 && age <= 120;
    }

    // ── Full Validation for Member/Coach ────────────────────

    
    private String validatePersonFields(String name, String email, String pass,
                                         String phone, String age,
                                         String subOrPlan, String coachIdOrSchedule,
                                         boolean isMember, boolean isAdd) {
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()
                || phone.isEmpty() || age.isEmpty() || subOrPlan.isEmpty()) {
            return "All fields are required.";
        }
        if (!isValidName(name)) {
            return "Name must contain letters only (no numbers).";
        }
        if (!email.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
            return "Invalid email format.";
        }
        if (isAdd && (fm.emailExists("members.txt", email) || fm.emailExists("coaches.txt", email))) {
            return "Email already exists.";
        }
        if (!isValidPassword(pass)) {
            return "Password must contain numbers only.";
        }
        if (!isValidPhone(phone)) {
            return "Phone must contain numbers only.";
        }
        if (!isValidAge(age)) {
            return "Age must be a number between 1 and 120.";
        }
        if (isMember) {
            // subOrPlan = subscription end date
            try {
                LocalDate.parse(subOrPlan, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ex) {
                return "Invalid date! Use yyyy-MM-dd";
            }
            // coachIdOrSchedule = coach id
            if (!coachIdOrSchedule.isEmpty()) {
                try { Integer.parseInt(coachIdOrSchedule); }
                catch (NumberFormatException ex) { return "Coach ID must be a number."; }
            }
        }
        return null; // كل حاجة تمام
    }

    public AdminFrame() {
        setTitle("FLEX CLUB — Admin Dashboard");
        setSize(960, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        String alerts = notif.checkSubscriptions();
        if (!alerts.equals("All subscriptions are active.")) {
            JOptionPane.showMessageDialog(this,
                "Subscription Alerts:\n\n" + alerts,
                "Alerts", JOptionPane.WARNING_MESSAGE);
        }

        buildUI();
        setVisible(true);
    }

    // ── Main UI ──────────────────────────────────────────────
    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0,0,NAVY,w,h,DARK));
                g2.fillRect(0,0,w,h);
                Path2D p = new Path2D.Double();
                p.moveTo(w*0.45,0); p.lineTo(w,0); p.lineTo(w,h); p.lineTo(w*0.35,h); p.closePath();
                g2.setPaint(new GradientPaint(w*0.4f,0, new Color(30,60,110,100), w,h, new Color(60,100,160,60)));
                g2.fill(p);
                g2.dispose();
            }
        };

        main.add(buildSidebar(), BorderLayout.WEST);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(buildDashboard(), "dashboard");
        contentPanel.add(buildCrudPanel("Members","members.txt",
            new String[]{"ID","Name","Email","Password","Phone","Age","Sub End","CoachID"},
            "member"), "members");
        contentPanel.add(buildCrudPanel("Coaches","coaches.txt",
            new String[]{"ID","Name","Email","Password","Phone","Age","Plan","Schedule"},
            "coach"), "coaches");
        contentPanel.add(buildAssignPanel(),  "assign");
        contentPanel.add(buildBillingPanel(), "billing");
        contentPanel.add(buildReportPanel(),  "reports");
        contentPanel.add(buildProfilePanel(), "profile");

        main.add(contentPanel, BorderLayout.CENTER);
        setContentPane(main);
    }

    // ── Sidebar ──────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,SIDEBAR, 0,getHeight(),new Color(6,14,28)));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(230,0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(28,0,20,0));

        JPanel dumbbell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth()/2, cy = getHeight()/2;
                g2.setColor(new Color(50,90,150)); g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.drawLine(cx-32,cy,cx+32,cy);
                g2.setColor(ACCENT); g2.fillRoundRect(cx-40,cy-14,14,28,5,5); g2.fillRoundRect(cx+26,cy-14,14,28,5,5);
            }
        };
        dumbbell.setOpaque(false); dumbbell.setPreferredSize(new Dimension(100,50)); dumbbell.setMaximumSize(new Dimension(100,50)); dumbbell.setAlignmentX(CENTER_ALIGNMENT);

        JLabel gymName = new JLabel("FLEX CLUB"); gymName.setFont(new Font("Trebuchet MS",Font.BOLD,22)); gymName.setForeground(WHITE); gymName.setAlignmentX(CENTER_ALIGNMENT);
        JLabel roleLabel = new JLabel("Administration"); roleLabel.setFont(new Font("Trebuchet MS",Font.PLAIN,11)); roleLabel.setForeground(MUTED); roleLabel.setAlignmentX(CENTER_ALIGNMENT);

        sidebar.add(dumbbell); sidebar.add(Box.createVerticalStrut(6)); sidebar.add(gymName); sidebar.add(Box.createVerticalStrut(2)); sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(20)); sidebar.add(makeSep()); sidebar.add(Box.createVerticalStrut(12));

        JLabel navLabel = new JLabel("  NAVIGATION"); navLabel.setFont(new Font("Trebuchet MS",Font.BOLD,9)); navLabel.setForeground(new Color(60,90,130)); navLabel.setAlignmentX(CENTER_ALIGNMENT); navLabel.setMaximumSize(new Dimension(230,20));
        sidebar.add(navLabel); sidebar.add(Box.createVerticalStrut(8));

        String[][] nav = { {"◆","Dashboard","dashboard"}, {"👤","Members","members"}, {"🏅","Coaches","coaches"}, {"🔗","Assign","assign"}, {"💳","Billing","billing"}, {"📊","Reports","reports"}, {"⚙", "Profile","profile"} };
        for (String[] item : nav) { JButton btn = makeNavBtn(item[0],item[1],item[2]); sidebar.add(btn); sidebar.add(Box.createVerticalStrut(2)); if (item[2].equals("dashboard")) { setActive(btn); activeBtn = btn; } }

        sidebar.add(Box.createVerticalGlue());
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,12,4)); statusPanel.setOpaque(false); statusPanel.setMaximumSize(new Dimension(230,30)); statusPanel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel dot = new JLabel("●"); dot.setFont(new Font("SansSerif",Font.PLAIN,10)); dot.setForeground(GREEN);
        JLabel statusLbl = new JLabel("System Online"); statusLbl.setFont(new Font("Trebuchet MS",Font.PLAIN,10)); statusLbl.setForeground(MUTED);
        statusPanel.add(dot); statusPanel.add(statusLbl); sidebar.add(statusPanel); sidebar.add(Box.createVerticalStrut(8)); sidebar.add(makeSep()); sidebar.add(Box.createVerticalStrut(8));
        JButton logout = makeNavBtn("⟵","Logout","logout"); logout.setForeground(new Color(0xFF6B6B)); sidebar.add(logout);
        return sidebar;
    }

    private JButton makeNavBtn(String icon, String text, String id) {
        JButton btn = new JButton(icon + "   " + text) {
            @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); if (this == activeBtn) { g2.setColor(ACCENT); g2.fillRoundRect(0,6,3,getHeight()-12,2,2); g2.setPaint(new GradientPaint(4,0, new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(),30), getWidth(),0, new Color(0,0,0,0))); g2.fillRoundRect(6,2,getWidth()-12,getHeight()-4,8,8); } g2.dispose(); super.paintComponent(g); }
        };
        btn.setForeground(MUTED); btn.setFont(new Font("Trebuchet MS",Font.PLAIN,13)); btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT); btn.setBorder(new EmptyBorder(11,24,11,24)); btn.setMaximumSize(new Dimension(230,44)); btn.setAlignmentX(CENTER_ALIGNMENT); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseEntered(java.awt.event.MouseEvent e) { if (btn != activeBtn) btn.setForeground(WHITE); } public void mouseExited(java.awt.event.MouseEvent e) { if (btn != activeBtn) btn.setForeground(id.equals("logout") ? new Color(0xFF6B6B) : MUTED); } });
        btn.addActionListener(e -> { if (id.equals("logout")) { int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION); if (c == JOptionPane.YES_OPTION) { dispose(); new LoginFrame(); } return; } if (activeBtn != null) { activeBtn.setForeground(MUTED); activeBtn.setFont(new Font("Trebuchet MS",Font.PLAIN,13)); activeBtn.repaint(); } setActive(btn); activeBtn = btn; cardLayout.show(contentPanel, id); });
        return btn;
    }

    private void setActive(JButton btn) { btn.setForeground(WHITE); btn.setFont(new Font("Trebuchet MS",Font.BOLD,13)); btn.repaint(); }

    // ── Dashboard ────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel outer = new JPanel(new BorderLayout(0,20)); outer.setOpaque(false); outer.setBorder(new EmptyBorder(28,30,28,30));

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel titleBlock = new JPanel(); titleBlock.setOpaque(false); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Dashboard"); title.setFont(new Font("Trebuchet MS",Font.BOLD,30)); title.setForeground(WHITE);
        JLabel sub = new JLabel("Welcome back, Admin — here's your overview"); sub.setFont(new Font("Trebuchet MS",Font.PLAIN,13)); sub.setForeground(MUTED);
        titleBlock.add(title); titleBlock.add(Box.createVerticalStrut(4)); titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST); header.add(buildRunnerPanel(), BorderLayout.EAST);

        int membersCount  = fm.getAllMembers().size(); int coachesCount  = fm.getAllCoaches().size(); int expiringCount = fm.checkExpiringSubscriptions().size();
        JPanel statsRow = new JPanel(new GridLayout(1,3,16,0)); statsRow.setOpaque(false); statsRow.setPreferredSize(new Dimension(0,105));
        statsRow.add(makeStatCard("👥", String.valueOf(membersCount), "Total Members", ICE, "Active memberships"));
        statsRow.add(makeStatCard("🏅", String.valueOf(coachesCount), "Active Coaches", ACCENT, "Fully staffed"));
        statsRow.add(makeStatCard("⏰", String.valueOf(expiringCount), "Expiring Soon", AMBER, expiringCount > 0 ? "Needs attention" : "All clear"));

        JPanel formCard = new JPanel(new BorderLayout(0,14)) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(CARD_BG); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.setColor(CARD_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.setColor(ACCENT); g2.fillRoundRect(20,0,80,4,2,2); g2.dispose(); } };
        formCard.setOpaque(false); formCard.setBorder(new EmptyBorder(18,24,18,24));

        JPanel formHeader = new JPanel(new BorderLayout()); formHeader.setOpaque(false);
        JLabel formTitle = new JLabel("Quick Add Member"); formTitle.setFont(new Font("Trebuchet MS",Font.BOLD,14)); formTitle.setForeground(WHITE);
        JLabel formSub = new JLabel("ID is auto-generated"); formSub.setFont(new Font("Trebuchet MS",Font.PLAIN,11)); formSub.setForeground(MUTED);
        formHeader.add(formTitle, BorderLayout.NORTH); formHeader.add(formSub, BorderLayout.SOUTH);

        JPanel fields = new JPanel(new GridLayout(2,4,12,10)); fields.setOpaque(false);

        JTextField idField = makeField(); idField.setText("AUTO #" + fm.generateMemberId()); idField.setEditable(false); idField.setForeground(GREEN); idField.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField nameF  = makeDashField("Full Name");
        JTextField phoneF = makeDashField("Phone (numbers only)");
        JTextField ageF   = makeDashField("Age (numbers only)");
        JTextField emailF = makeDashField("Email");
        JPasswordField passF = makeDashPass("Password (numbers only)");
        JTextField subF   = makeDashField("Sub End (yyyy-MM-dd)");
        JTextField cidF   = makeDashField("Coach ID (0=none)");

        fields.add(idField); fields.add(nameF); fields.add(phoneF); fields.add(ageF);
        fields.add(emailF); fields.add(passF); fields.add(subF); fields.add(cidF);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); btnRow.setOpaque(false);
        JButton addBtn    = gradientBtn("✚  Add Member");
        JButton searchBtn = outlineBtn("🔍  Search");

        addBtn.addActionListener(e -> {
            String name    = getPlain(nameF,  "Full Name");
            String email   = getPlain(emailF, "Email");
            String pass    = new String(passF.getPassword()).trim();
            // إزالة placeholder لو موجود
            if (pass.equals("Password (numbers only)")) pass = "";
            String phone   = getPlain(phoneF, "Phone (numbers only)");
            String subText = getPlain(subF,   "Sub End (yyyy-MM-dd)");
            String cid     = getPlain(cidF,   "Coach ID (0=none)");
            String age     = getPlain(ageF,   "Age (numbers only)");

            // Validate name
            if (!isValidName(name)) { showErr("Name must contain letters only (no numbers)."); return; }
            // Validate email
            if (email.isEmpty()) { showErr("All fields are required."); return; }
            if (!email.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) { showErr("Invalid email format."); return; }
            if (fm.emailExists("members.txt", email) || fm.emailExists("coaches.txt", email)) { showErr("Email already exists."); return; }
            // Validate password (numbers only)
            if (!isValidPassword(pass)) { showErr("Password must contain numbers only."); return; }
            // Validate phone (numbers only)
            if (!isValidPhone(phone)) { showErr("Phone must contain numbers only."); return; }
            // Validate age (numbers only)
            if (!isValidAge(age)) { showErr("Age must be a number between 1 and 120."); return; }
            // Validate date
            LocalDate subDate;
            try { subDate = LocalDate.parse(subText, DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
            catch (DateTimeParseException ex) { showErr("Invalid date! Use yyyy-MM-dd"); return; }
            // Validate coach id
            int coachId;
            try { coachId = cid.isEmpty() ? 0 : Integer.parseInt(cid); }
            catch (NumberFormatException ex) { showErr("Coach ID must be a number."); return; }

            int newId = fm.generateMemberId();
            fm.addMember(new Member(newId, name, email, pass, phone,
                    Integer.parseInt(age), subDate.toString(), coachId));
            idField.setText("AUTO #" + fm.generateMemberId());
            clearDash(nameF,"Full Name"); clearDash(phoneF,"Phone (numbers only)");
            clearDash(ageF,"Age (numbers only)"); clearDash(emailF,"Email");
            passF.setText(""); clearDash(subF,"Sub End (yyyy-MM-dd)");
            clearDash(cidF,"Coach ID (0=none)");
            showMsg("Member added! ID: " + newId);
        });

        searchBtn.addActionListener(e -> showSearchDialog("member"));
        btnRow.add(addBtn); btnRow.add(searchBtn);

        formCard.add(formHeader, BorderLayout.NORTH);
        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(btnRow, BorderLayout.SOUTH);

        outer.add(header, BorderLayout.NORTH);
        outer.add(statsRow, BorderLayout.CENTER);
        outer.add(formCard, BorderLayout.SOUTH);
        return outer;
    }

    // ── Search Dialog ─────────────────────────────────────────
    private void showSearchDialog(String type) {
        JDialog dialog = new JDialog(this, "Search " + (type.equals("member") ? "Members" : "Coaches"), true);
        dialog.setSize(640, 400); dialog.setLocationRelativeTo(this); dialog.setLayout(new BorderLayout(0,10));
        JPanel top = new JPanel(new BorderLayout(10,0)); top.setBackground(CARD_BG); top.setBorder(new EmptyBorder(14,16,14,16));
        JTextField searchF = new JTextField(); searchF.setBackground(FIELD_BG); searchF.setForeground(WHITE); searchF.setCaretColor(WHITE); searchF.setFont(new Font("Trebuchet MS",Font.PLAIN,14)); searchF.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FIELD_BORD), BorderFactory.createEmptyBorder(8,12,8,12)));
        JButton goBtn = gradientBtn("Search"); top.add(searchF, BorderLayout.CENTER); top.add(goBtn, BorderLayout.EAST);
        String[] cols = type.equals("member") ? new String[]{"ID","Name","Email","Phone","Sub End","CoachID"} : new String[]{"ID","Name","Email","Phone","Plan","Schedule"};
        DefaultTableModel model = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r, int c) { return false; } }; JTable table = makeTable(model);
        goBtn.addActionListener(e -> { model.setRowCount(0); String kw = searchF.getText().trim(); if (kw.isEmpty()) return; if (type.equals("member")) { for (Member m : fm.searchMembers(kw)) model.addRow(new Object[]{m.getId(), m.getName(), m.getEmail(), m.getPhone(), m.getSubscriptionEndDate(), m.getCoachId()}); } else { for (Coach c : fm.searchCoaches(kw)) model.addRow(new Object[]{c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getPlan(), c.getSchedule()}); } if (model.getRowCount() == 0) JOptionPane.showMessageDialog(dialog, "No results found.", "Search", JOptionPane.INFORMATION_MESSAGE); });
        searchF.addActionListener(e -> goBtn.doClick());
        dialog.add(top, BorderLayout.NORTH); dialog.add(makeScroll(table), BorderLayout.CENTER); dialog.getContentPane().setBackground(new Color(10,22,40)); dialog.setVisible(true);
    }

    // ── CRUD Panel ───────────────────────────────────────────
    private JPanel buildCrudPanel(String title, String file, String[] cols, String type) {
        JPanel outer = new JPanel(new BorderLayout(0,14)); outer.setOpaque(false); outer.setBorder(new EmptyBorder(24,28,24,28));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel lbl = new JLabel(title); lbl.setFont(new Font("Trebuchet MS",Font.BOLD,26)); lbl.setForeground(WHITE);
        JButton searchBtn = outlineBtn("🔍  Search"); searchBtn.addActionListener(e -> showSearchDialog(type));
        header.add(lbl, BorderLayout.WEST); header.add(searchBtn, BorderLayout.EAST);

        DefaultTableModel model = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = makeTable(model);
        loadTable(file, model);

        JTextField[] fields = new JTextField[cols.length];
        JPasswordField finalPF = new JPasswordField();
        stylePassField(finalPF);

        JPanel formCard = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.setColor(CARD_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.dispose();
            }
        };
        formCard.setOpaque(false); formCard.setBorder(new EmptyBorder(16,20,16,20));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5,8,5,8); gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < cols.length; i++) {
            gbc.gridx = (i % 4) * 2; gbc.gridy = i / 4; gbc.weightx = 0;
            JLabel fl = new JLabel(cols[i]); fl.setForeground(ICE); fl.setFont(new Font("Trebuchet MS",Font.BOLD,10)); fl.setPreferredSize(new Dimension(60,20));
            formCard.add(fl, gbc); gbc.gridx++; gbc.weightx = 1;
            if (i == 3) {
                formCard.add(finalPF, gbc); fields[i] = null;
            } else {
                fields[i] = makeField();
                if (i == 0) { fields[i].setEditable(false); fields[i].setForeground(GREEN); }
                formCard.add(fields[i], gbc);
            }
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                for (int c = 0; c < cols.length; c++) {
                    Object v = model.getValueAt(row, c);
                    String val = v != null ? v.toString() : "";
                    if (c == 3) finalPF.setText(val);
                    else if (fields[c] != null) fields[c].setText(val);
                }
            }
        });

        JButton addBtn = gradientBtn("✚  Add");
        JButton updBtn = outlineBtn("✎  Update");
        JButton delBtn = dangerBtn("✕  Delete");
        JButton clrBtn = outlineBtn("↺  Clear");

        addBtn.addActionListener(e -> {
            try {
                String name  = fields[1].getText().trim();
                String email = fields[2].getText().trim();
                String pass  = new String(finalPF.getPassword()).trim();
                String phone = fields[4].getText().trim();
                String age   = fields[5].getText().trim();
                String subOrPlan       = fields[6].getText().trim();
                String coachOrSchedule = fields[7].getText().trim();

                // ── Validate Name: حروف فقط بدون أرقام ──
                if (!isValidName(name)) {
                    showErr("Name must contain letters only (no numbers)."); return;
                }
                // ── Validate Password: أرقام فقط ──
                if (!isValidPassword(pass)) {
                    showErr("Password must contain numbers only."); return;
                }
                // ── Validate Phone: أرقام فقط ──
                if (!isValidPhone(phone)) {
                    showErr("Phone must contain numbers only."); return;
                }
                // ── Validate Age: أرقام فقط بين 1 و 120 ──
                if (!isValidAge(age)) {
                    showErr("Age must be a number between 1 and 120."); return;
                }
                // ── Validate Email ──
                if (!email.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
                    showErr("Invalid email format."); return;
                }
                if (fm.emailExists("members.txt", email) || fm.emailExists("coaches.txt", email)) {
                    showErr("Email already exists."); return;
                }

                int ageVal = Integer.parseInt(age);
                int newId;
               if (type.equals("member")) {

    newId = fm.generateMemberId();

    fields[0].setText(String.valueOf(newId));

    int coachId =
            coachOrSchedule.isEmpty()
                    ? 0
                    : Integer.parseInt(coachOrSchedule);

    // ── check if coach exists ──

    if (coachId != 0 && fm.getCoachById(coachId) == null) {

        showErr("Coach ID does not exist.");

        return;
    }

    fm.addMember(
            new Member(
                    newId,
                    name,
                    email,
                    pass,
                    phone,
                    ageVal,
                    subOrPlan,
                    coachId
            )
    );

}
                else {
                    newId = fm.generateCoachId();
                    fields[0].setText(String.valueOf(newId));
                    fm.addCoach(new Coach(newId, name, email, pass, phone,
                            ageVal, subOrPlan, coachOrSchedule));
                }
                loadTable(file, model);
                clearCrudFields(fields, finalPF);
                showMsg("Added! ID: " + newId);
            } catch (Exception ex) {
                showErr("Error: " + ex.getMessage());
            }
        });

        updBtn.addActionListener(e -> {
            try {
                int id    = Integer.parseInt(fields[0].getText().trim());
                String name  = fields[1].getText().trim();
                String email = fields[2].getText().trim();
                String pass  = new String(finalPF.getPassword()).trim();
                String phone = fields[4].getText().trim();
                String age   = fields[5].getText().trim();

                // ── Validate Name: حروف فقط بدون أرقام ──
                if (!isValidName(name)) {
                    showErr("Name must contain numbers only."); return;
                }
                // ── Validate Password: أرقام فقط ──
                if (!isValidPassword(pass)) {
                    showErr("Password must contain numbers only."); return;
                }
                // ── Validate Phone: أرقام فقط ──
                if (!isValidPhone(phone)) {
                    showErr("Phone must contain numbers only."); return;
                }
                // ── Validate Age: أرقام فقط بين 1 و 120 ──
                if (!isValidAge(age)) {
                    showErr("Age must be a number between 1 and 120."); return;
                }
int ageVal = Integer.parseInt(age);

if (type.equals("member")) {

    int coachId =
            Integer.parseInt(
                    fields[7].getText().trim()
            );

    if (
        coachId != 0
        &&
        fm.getCoachById(coachId) == null
    ) {

        showErr("Coach ID does not exist.");

        return;
    }

    fm.updateMember(
            id,
            name,
            email,
            pass,
            phone,
            ageVal,
            fields[6].getText().trim(),
            coachId
    );

} else {

    fm.updateCoach(
            id,
            name,
            email,
            pass,
            phone,
            ageVal,
            fields[6].getText().trim(),
            fields[7].getText().trim()
    );
}
                
                loadTable(file, model);
                showMsg("Updated successfully!");
            } catch (Exception ex) {
                showErr("Error: " + ex.getMessage());
            }
        });

        delBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c != JOptionPane.YES_OPTION) return;
            try {
                int id = Integer.parseInt(fields[0].getText().trim());
                if (type.equals("member")) fm.deleteMember(id);
                else fm.deleteCoach(id);
                loadTable(file, model);
                clearCrudFields(fields, finalPF);
                showMsg("Deleted successfully!");
            } catch (Exception ex) {
                showErr("Error: " + ex.getMessage());
            }
        });

        clrBtn.addActionListener(e -> clearCrudFields(fields, finalPF));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); btnRow.setOpaque(false);
        btnRow.add(addBtn); btnRow.add(updBtn); btnRow.add(delBtn); btnRow.add(clrBtn);

        JPanel bottom = new JPanel(new BorderLayout(0,10)); bottom.setOpaque(false);
        bottom.add(formCard, BorderLayout.CENTER); bottom.add(btnRow, BorderLayout.SOUTH);

        outer.add(header, BorderLayout.NORTH);
        outer.add(makeScroll(table), BorderLayout.CENTER);
        outer.add(bottom, BorderLayout.SOUTH);
        return outer;
    }

    // ── Assign Panel ─────────────────────────────────────────
    private JPanel buildAssignPanel() {
        JPanel outer = new JPanel(new BorderLayout()); outer.setOpaque(false); outer.setBorder(new EmptyBorder(28,30,28,30));
        JLabel lbl = new JLabel("Assign Coach to Member"); lbl.setFont(new Font("Trebuchet MS",Font.BOLD,26)); lbl.setForeground(WHITE); lbl.setBorder(new EmptyBorder(0,0,20,0));
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        JPanel card = new JPanel(new GridBagLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(CARD_BG); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,16,16); g2.setColor(CARD_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16); g2.setColor(ACCENT); g2.fillRoundRect(20,0,80,4,2,2); g2.dispose(); } };
        card.setOpaque(false); card.setBorder(new EmptyBorder(36,44,36,44));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(12,12,12,12); gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel iconLbl = new JLabel("🔗", SwingConstants.CENTER); iconLbl.setFont(new Font("SansSerif",Font.PLAIN,36)); gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; card.add(iconLbl, gbc);
        JLabel cardTitle = new JLabel("Link Member ↔ Coach", SwingConstants.CENTER); cardTitle.setFont(new Font("Trebuchet MS",Font.BOLD,16)); cardTitle.setForeground(WHITE); gbc.gridy=1; card.add(cardTitle, gbc); gbc.gridwidth=1;
        JTextField midF = makeField(14); JTextField cidF = makeField(14);
        gbc.gridx=0; gbc.gridy=2; gbc.weightx=0; card.add(makeLabel("Member ID"), gbc); gbc.gridx=1; gbc.weightx=1; card.add(midF, gbc); gbc.gridx=0; gbc.gridy=3; gbc.weightx=0; card.add(makeLabel("Coach ID"), gbc); gbc.gridx=1; gbc.weightx=1; card.add(cidF, gbc);
        JButton btn = gradientBtn("🔗  Assign Coach"); btn.setBorder(new EmptyBorder(12,32,12,32)); gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2; card.add(btn, gbc);
        btn.addActionListener(e -> { try { int mid = Integer.parseInt(midF.getText().trim()); int cid = Integer.parseInt(cidF.getText().trim()); if (fm.getMemberById(mid) == null) { showErr("Member ID not found."); return; } if (fm.getCoachById(cid) == null) { showErr("Coach ID not found."); return; } fm.assignCoachToMember(mid, cid); showMsg("Coach assigned successfully!"); midF.setText(""); cidF.setText(""); } catch (NumberFormatException ex) { showErr("IDs must be numbers."); } });
        center.add(card); outer.add(lbl, BorderLayout.NORTH); outer.add(center, BorderLayout.CENTER); return outer;
    }

    // ── Billing Panel ────────────────────────────────────────
    private JPanel buildBillingPanel() {
        JPanel outer = new JPanel(new BorderLayout(0,14)); outer.setOpaque(false); outer.setBorder(new EmptyBorder(24,28,24,28));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); header.setBorder(new EmptyBorder(0,0,8,0)); JLabel lbl = new JLabel("Billing"); lbl.setFont(new Font("Trebuchet MS",Font.BOLD,26)); lbl.setForeground(WHITE); header.add(lbl, BorderLayout.WEST);
        JPanel formCard = new JPanel(new GridBagLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(CARD_BG); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.setColor(CARD_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.dispose(); } };
        formCard.setOpaque(false); formCard.setBorder(new EmptyBorder(14,18,14,18));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(6,8,6,8); gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField midF = makeField(); JTextField nameF = makeField(); JTextField amtF = makeField();
        gbc.gridx=0; gbc.gridy=0; formCard.add(makeLabel("Member ID"), gbc); gbc.gridx=1; formCard.add(midF, gbc); gbc.gridx=2; formCard.add(makeLabel("Name"), gbc); gbc.gridx=3; formCard.add(nameF, gbc); gbc.gridx=0; gbc.gridy=1; formCard.add(makeLabel("Amount (EGP)"), gbc); gbc.gridx=1; formCard.add(amtF, gbc); JButton addBtn = gradientBtn("✚  Save Bill"); gbc.gridx=2; gbc.gridwidth=2; formCard.add(addBtn, gbc);
        DefaultTableModel billModel = new DefaultTableModel(new String[]{"Member ID","Name","Amount","Date"},0) { public boolean isCellEditable(int r, int c) { return false; } }; loadTable("bills.txt", billModel);
        addBtn.addActionListener(e -> { try { String mid = midF.getText().trim(); String name = nameF.getText().trim(); String amt = amtF.getText().trim(); if (mid.isEmpty() || name.isEmpty() || amt.isEmpty()) { showErr("All fields required."); return; } double amount = Double.parseDouble(amt); if (amount <= 0) { showErr("Amount must be positive."); return; } bill.addBill(mid, name, amount); loadTable("bills.txt", billModel); midF.setText(""); nameF.setText(""); amtF.setText(""); showMsg("Bill saved!"); } catch (NumberFormatException ex) { showErr("Amount must be a number."); } });
        JPanel top = new JPanel(new BorderLayout(0,12)); top.setOpaque(false); top.add(header, BorderLayout.NORTH); top.add(formCard, BorderLayout.CENTER);
        outer.add(top, BorderLayout.NORTH); outer.add(makeScroll(makeTable(billModel)), BorderLayout.CENTER); return outer;
    }

    // ── Reports Panel ────────────────────────────────────────
    private JPanel buildReportPanel() {
        JPanel outer = new JPanel(new BorderLayout(0,14)); outer.setOpaque(false); outer.setBorder(new EmptyBorder(24,28,24,28));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); header.setBorder(new EmptyBorder(0,0,8,0)); JLabel lbl = new JLabel("Reports"); lbl.setFont(new Font("Trebuchet MS",Font.BOLD,26)); lbl.setForeground(WHITE); JButton refresh = outlineBtn("↻  Refresh"); header.add(lbl, BorderLayout.WEST); header.add(refresh, BorderLayout.EAST);
        JTextArea ta = new JTextArea(fm.generateReport()); ta.setEditable(false); ta.setBackground(new Color(12,26,46)); ta.setForeground(WHITE); ta.setFont(new Font("Consolas",Font.PLAIN,13)); ta.setBorder(new EmptyBorder(16,20,16,20));
        refresh.addActionListener(e -> ta.setText(fm.generateReport()));
        outer.add(header, BorderLayout.NORTH); outer.add(makeScroll(ta), BorderLayout.CENTER); return outer;
    }

    // ── Profile Panel ────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(28,30,28,30));

        JLabel lbl = new JLabel("My Profile");
        lbl.setFont(new Font("Trebuchet MS",Font.BOLD,26));
        lbl.setForeground(WHITE);
        lbl.setBorder(new EmptyBorder(0,0,20,0));

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints cWrapper = new GridBagConstraints();
        cWrapper.anchor = GridBagConstraints.NORTH;
        cWrapper.weighty = 1.0;
        cWrapper.fill = GridBagConstraints.HORIZONTAL;

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.setColor(CARD_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.setColor(ACCENT); g2.fillRoundRect(20,0,80,4,2,2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(24,30,24,30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,10,8,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Avatar
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int sz = 64, cx = getWidth()/2, cy = getHeight()/2;
                g2.setPaint(new GradientPaint(cx-sz/2,cy-sz/2, ACCENT, cx+sz/2,cy+sz/2, PURPLE));
                g2.fillOval(cx-sz/2,cy-sz/2,sz,sz);
                g2.setColor(WHITE);
                g2.setFont(new Font("Trebuchet MS",Font.BOLD,26));
                FontMetrics fmx = g2.getFontMetrics();
                g2.drawString("A", cx - fmx.stringWidth("A")/2, cy + fmx.getAscent()/2 - 2);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(80,80));
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; gbc.anchor = GridBagConstraints.CENTER;
        card.add(avatar, gbc);

        JLabel avatarLbl = new JLabel("Administrator", SwingConstants.CENTER);
        avatarLbl.setFont(new Font("Trebuchet MS",Font.BOLD,14));
        avatarLbl.setForeground(WHITE);
        gbc.gridy=1; card.add(avatarLbl, gbc);
        gbc.gridwidth=1; gbc.anchor = GridBagConstraints.WEST;

        JTextField nameF  = makeField(20);
        JTextField emailF = makeField(20);
        JPasswordField passF = new JPasswordField(20); stylePassField(passF);
        JTextField phoneF = makeField(20);
        JTextField ageF   = makeField(20);
        JTextField uF     = makeField(20);

        // Pre-fill from admins.txt
        try (BufferedReader br = new BufferedReader(new FileReader("admins.txt"))) {
            String line = br.readLine();
            if (line != null && !line.trim().isEmpty()) {
                String[] p = line.split(",");
                if (p.length > 5) {
                    nameF.setText(p[1]);
                    emailF.setText(p[2]);
                    passF.setText(p[3]);
                    phoneF.setText(p[4]);
                    ageF.setText(p[5]);
                    if (p.length > 6) uF.setText(p[6]);
                }
            }
        } catch (Exception ignored) {}

        Object[][] rows = {
            {"👤  Name",   nameF},
            {"📧  Email",  emailF},
            {"🔒  Password", passF},
            {"📱  Phone",  phoneF},
            {"🎂  Age",    ageF},
            {"🏷  Username", uF}
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx=0; gbc.gridy=i+2; gbc.weightx=0;
            JLabel fl = new JLabel((String)rows[i][0]);
            fl.setForeground(MUTED);
            fl.setFont(new Font("Trebuchet MS",Font.PLAIN,12));
            fl.setPreferredSize(new Dimension(120,20));
            card.add(fl, gbc);
            gbc.gridx=1; gbc.weightx=1;
            card.add((JComponent)rows[i][1], gbc);
        }

        JButton save = gradientBtn("✚  Save Changes");
        save.setBorder(new EmptyBorder(10,24,10,24));
        card.setPreferredSize(new Dimension(560, 540));
        gbc.gridx=0; gbc.gridy=rows.length+2; gbc.gridwidth=2; gbc.weightx=0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        card.add(save, gbc);

        save.addActionListener(e -> {
            String name   = nameF.getText().trim();
            String email  = emailF.getText().trim();
            String pass   = new String(passF.getPassword()).trim();
            String phone  = phoneF.getText().trim();
            String ageStr = ageF.getText().trim();
            String uname  = uF.getText().trim();

            // ── Validate Name: حروف فقط بدون أرقام ──
            if (!isValidName(name)) {
                showErr("Name must contain letters only (no numbers)."); return;
            }
            // ── Validate Email ──
            if (!email.matches("^[\\w.+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
                showErr("Invalid email format."); return;
            }
            // ── Validate Password: أرقام فقط ──
            if (!pass.isEmpty() && !isValidPassword(pass)) {
                showErr("Password must contain numbers only."); return;
            }
            // ── Validate Phone: أرقام فقط ──
            if (!phone.isEmpty() && !isValidPhone(phone)) {
                showErr("Phone must contain numbers only."); return;
            }
            // ── Validate Age: أرقام فقط بين 1 و 120 ──
            if (!ageStr.isEmpty() && !isValidAge(ageStr)) {
                showErr("Age must be a number between 1 and 120."); return;
            }

            auth.saveAdminCredentials("0", name, email, pass, phone, ageStr, uname);
            showMsg("Profile updated successfully!");
        });

        centerWrapper.add(card, cWrapper);
        outer.add(lbl, BorderLayout.NORTH);
        outer.add(centerWrapper, BorderLayout.CENTER);
        return outer;
    }

    // ── Runner cartoon ────────────────────────────────────────
    private JPanel buildRunnerPanel() {
        JPanel runner = new JPanel() { @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int cx = getWidth()/2, cy = getHeight()/2; g2.setColor(new Color(0xF5C5A3)); g2.fillOval(cx-14,cy-32,28,28); g2.setColor(new Color(0x2A1A0A)); g2.fillArc(cx-14,cy-32,28,20,0,180); g2.setColor(new Color(0x2C1810)); g2.fillOval(cx-7,cy-22,4,5); g2.fillOval(cx+3,cy-22,4,5); g2.setColor(new Color(0xC07060)); g2.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.drawArc(cx-6,cy-13,12,8,10,-160); g2.setPaint(new GradientPaint(cx-14,cy, ACCENT, cx+14,cy+22, ACCENT_DARK)); g2.fillRoundRect(cx-14,cy,28,24,6,6); g2.setColor(new Color(0xF5C5A3)); g2.setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.drawLine(cx-14,cy+6,cx-28,cy+16); g2.drawLine(cx+14,cy+6,cx+28,cy-8); g2.setColor(ICE); g2.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.drawLine(cx+22,cy-12,cx+42,cy-12); g2.setColor(ACCENT); g2.fillRoundRect(cx+18,cy-18,7,13,3,3); g2.fillRoundRect(cx+39,cy-18,7,13,3,3); g2.setColor(new Color(0x1A3A6A)); g2.setStroke(new BasicStroke(7,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g2.drawLine(cx-5,cy+24,cx-16,cy+50); g2.drawLine(cx+5,cy+24,cx+12,cy+50); g2.setColor(ACCENT_DARK); g2.fillRoundRect(cx-22,cy+46,14,7,4,4); g2.fillRoundRect(cx+6, cy+46,14,7,4,4); } };
        runner.setOpaque(false); runner.setPreferredSize(new Dimension(100,100)); return runner;
    }

    private JPanel makeStatCard(String icon, String num, String label, Color color, String sub) {
        JPanel card = new JPanel(new BorderLayout()) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(CARD_BG); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.setColor(new Color(color.getRed(),color.getGreen(), color.getBlue(),80)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12); g2.setColor(color); g2.fillRoundRect(14,0,50,3,2,2); g2.dispose(); } };
        card.setOpaque(false); card.setBorder(new EmptyBorder(16,16,14,16));
        JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel iconLbl = new JLabel(icon); iconLbl.setFont(new Font("SansSerif",Font.PLAIN,22)); iconLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel numLbl = new JLabel(num); numLbl.setFont(new Font("Trebuchet MS",Font.BOLD,32)); numLbl.setForeground(color); numLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblLbl = new JLabel(label); lblLbl.setFont(new Font("Trebuchet MS",Font.PLAIN,12)); lblLbl.setForeground(MUTED); lblLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subLbl = new JLabel(sub); subLbl.setFont(new Font("Trebuchet MS",Font.ITALIC,10)); subLbl.setForeground(new Color(color.getRed(),color.getGreen(), color.getBlue(),150)); subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(iconLbl); left.add(Box.createVerticalStrut(4)); left.add(numLbl); left.add(Box.createVerticalStrut(2)); left.add(lblLbl); left.add(Box.createVerticalStrut(2)); left.add(subLbl);
        card.add(left, BorderLayout.CENTER); return card;
    }

    // ── Helpers ──────────────────────────────────────────────
    private Component makeSep() { JPanel s = new JPanel() { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setPaint(new GradientPaint(0,0, new Color(0,0,0,0), getWidth()/2,0, new Color(50,80,130,120), true)); g2.fillRect(0,0,getWidth(),1); g2.dispose(); } }; s.setOpaque(false); s.setMaximumSize(new Dimension(190,1)); s.setAlignmentX(CENTER_ALIGNMENT); return s; }
    private void stylePassField(JPasswordField pf) { pf.setBackground(FIELD_BG); pf.setForeground(WHITE); pf.setCaretColor(ICE); pf.setFont(new Font("Trebuchet MS",Font.PLAIN,13)); pf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FIELD_BORD,1), BorderFactory.createEmptyBorder(8,12,8,12))); pf.addFocusListener(new java.awt.event.FocusAdapter() { public void focusGained(java.awt.event.FocusEvent e) { pf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ICE,1), BorderFactory.createEmptyBorder(8,12,8,12))); } public void focusLost(java.awt.event.FocusEvent e) { pf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FIELD_BORD,1), BorderFactory.createEmptyBorder(8,12,8,12))); } }); }
    private JButton gradientBtn(String text) { JButton btn = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setPaint(new GradientPaint(0,0,ACCENT, getWidth(),getHeight(),PURPLE)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.setColor(new Color(255,255,255,30)); g2.fillRoundRect(2,1,getWidth()-4,getHeight()/2-1,8,8); g2.dispose(); super.paintComponent(g); } }; btn.setForeground(WHITE); btn.setFont(new Font("Trebuchet MS",Font.BOLD,12)); btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setBorder(new EmptyBorder(10,22,10,22)); return btn; }
    private JButton outlineBtn(String text) { JButton btn = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(ICE.getRed(),ICE.getGreen(), ICE.getBlue(),20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.setColor(new Color(ICE.getRed(),ICE.getGreen(), ICE.getBlue(),70)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose(); super.paintComponent(g); } }; btn.setForeground(ICE); btn.setFont(new Font("Trebuchet MS",Font.PLAIN,12)); btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setBorder(new EmptyBorder(10,18,10,18)); btn.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseEntered(java.awt.event.MouseEvent e) { btn.setForeground(WHITE); } public void mouseExited(java.awt.event.MouseEvent e) { btn.setForeground(ICE); } }); return btn; }
    private JButton dangerBtn(String text) { JButton btn = new JButton(text) { @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(255,60,60,20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.setColor(new Color(255,60,60,70)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose(); super.paintComponent(g); } }; btn.setForeground(new Color(0xFF6B6B)); btn.setFont(new Font("Trebuchet MS",Font.PLAIN,12)); btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setBorder(new EmptyBorder(10,18,10,18)); btn.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseEntered(java.awt.event.MouseEvent e) { btn.setForeground(new Color(255,100,100)); } public void mouseExited(java.awt.event.MouseEvent e) { btn.setForeground(new Color(0xFF6B6B)); } }); return btn; }

    private JTable makeTable(DefaultTableModel model) {
        JTable t = new JTable(model) { @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) { Component c = super.prepareRenderer(renderer, row, col); if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? ROW_NORM : ROW_ALT); if (c instanceof JLabel) ((JLabel)c).setBorder(new EmptyBorder(0,8,0,8)); return c; } };
        t.setBackground(ROW_NORM); t.setForeground(WHITE); t.setGridColor(new Color(25,50,85)); t.setFont(new Font("Trebuchet MS",Font.PLAIN,13)); t.setRowHeight(34); t.setIntercellSpacing(new Dimension(0,1)); t.setSelectionBackground(new Color(ACCENT.getRed(),ACCENT.getGreen(),ACCENT.getBlue(),100)); t.setSelectionForeground(WHITE); t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        JTableHeader h = t.getTableHeader(); h.setBackground(HEADER_BG); h.setForeground(ICE); h.setFont(new Font("Trebuchet MS",Font.BOLD,11)); h.setBorder(new MatteBorder(0,0,2,0,new Color(40,70,120))); h.setPreferredSize(new Dimension(0,36)); h.setReorderingAllowed(false); return t;
    }

    private JScrollPane makeScroll(Component c) { JScrollPane s = new JScrollPane(c); s.setOpaque(false); s.getViewport().setOpaque(false); s.setBorder(new LineBorder(CARD_BORDER,1,true)); s.getVerticalScrollBar().setUnitIncrement(12); s.getVerticalScrollBar().setBackground(FIELD_BG); return s; }
    private JTextField makeField() { return makeField(10); }
    private JTextField makeField(int cols) { JTextField f = new JTextField(cols); f.setBackground(FIELD_BG); f.setForeground(WHITE); f.setCaretColor(ICE); f.setFont(new Font("Trebuchet MS",Font.PLAIN,13)); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FIELD_BORD,1), BorderFactory.createEmptyBorder(8,12,8,12))); f.addFocusListener(new java.awt.event.FocusAdapter() { public void focusGained(java.awt.event.FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ICE,1), BorderFactory.createEmptyBorder(8,12,8,12))); } public void focusLost(java.awt.event.FocusEvent e) { f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(FIELD_BORD,1), BorderFactory.createEmptyBorder(8,12,8,12))); } }); return f; }
    private JTextField makeDashField(String ph) { JTextField f = makeField(); f.putClientProperty("ph", ph); f.setText(ph); f.setForeground(MUTED); f.addFocusListener(new java.awt.event.FocusAdapter() { public void focusGained(java.awt.event.FocusEvent e) { if (f.getText().equals(ph)) { f.setText(""); f.setForeground(WHITE); } } public void focusLost(java.awt.event.FocusEvent e) { if (f.getText().isEmpty()) { f.setText(ph); f.setForeground(MUTED); } } }); return f; }
    private JPasswordField makeDashPass(String ph) { JPasswordField pf = new JPasswordField(); stylePassField(pf); pf.setEchoChar((char)0); pf.setText(ph); pf.setForeground(MUTED); pf.addFocusListener(new java.awt.event.FocusAdapter() { public void focusGained(java.awt.event.FocusEvent e) { if (String.valueOf(pf.getPassword()).equals(ph)) { pf.setText(""); pf.setEchoChar('●'); pf.setForeground(WHITE); } } public void focusLost(java.awt.event.FocusEvent e) { if (pf.getPassword().length == 0) { pf.setEchoChar((char)0); pf.setText(ph); pf.setForeground(MUTED); } } }); return pf; }
    private JLabel makeLabel(String text) { JLabel l = new JLabel(text); l.setForeground(MUTED); l.setFont(new Font("Trebuchet MS",Font.BOLD,11)); return l; }
    private String getPlain(JTextField f, String ph) { String t = f.getText().trim(); return t.equals(ph) ? "" : t; }
    private void clearDash(JTextField f, String ph) { f.setText(ph); f.setForeground(MUTED); }
    private void clearCrudFields(JTextField[] fields, JPasswordField pf) { for (JTextField f : fields) if (f != null) f.setText(""); if (pf != null) pf.setText(""); }
    private void loadTable(String file, DefaultTableModel model) { model.setRowCount(0); try (BufferedReader r = new BufferedReader(new FileReader(file))) { String line; while ((line = r.readLine()) != null) if (!line.trim().isEmpty()) model.addRow(line.split(",")); } catch (IOException ignored) {} }
    private void showMsg(String msg) { JOptionPane.showMessageDialog(this, msg, "✓ Success", JOptionPane.INFORMATION_MESSAGE); }
    private void showErr(String msg) { JOptionPane.showMessageDialog(this, msg, "✕ Error", JOptionPane.ERROR_MESSAGE); }
}