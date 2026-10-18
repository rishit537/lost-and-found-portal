package com.lostandfound.gui;

import com.lostandfound.dao.*;
import com.lostandfound.model.Category;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/*
 Main dashboard window for the Lost and Found Portal.
 Features:
 - A JTable of all active (non-Claimed) items
 - Search bar + Category/Status combo-boxes + Search button
 - Report, Claim, View Image, and My Items action buttons
 - Email-based identity verification for Claim actions
 */
public class MainDashboardFrame extends JFrame {

    // ---- Filter widgets ----
    private JTextField tfSearch;
    private JComboBox<String> cbCategoryFilter;
    private JComboBox<String> cbStatusFilter;

    // ---- Table ----
    private JTable table;
    private DefaultTableModel tableModel;

    // ---- Data ----
    private List<Item> currentItems;
    private Category[] allCategories;

    // ---- DAOs ----
    private final ItemDAO itemDAO = new ItemDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final UserDAO userDAO = new UserDAO();

    public MainDashboardFrame() {
        super("Lost & Found Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        applyLookAndFeel();
        initComponents();
        setSize(980, 600);
        setMinimumSize(new Dimension(780, 420));
        setLocationRelativeTo(null);
        refreshTable();
    }

    // --- Look and feel
    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    // --- components init ---
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        // ---- Header ----
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ---- Filter Bar ----
        JPanel filterBar = buildFilterBar();

        // ---- Table ----
        String[] columns = {"ID", "Title", "Category", "Location", "Status", "Reported By", "Date Reported"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color even = new Color(245, 247, 250);
            private final Color odd = Color.WHITE;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? even : odd);
                return c;
            }
        });

        // Header styling
        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        th.setBackground(new Color(55, 71, 97));
        th.setForeground(Color.WHITE);
        th.setOpaque(true);
        th.setPreferredSize(new Dimension(th.getWidth(), 34));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBackground(new Color(55, 71, 97));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        headerRenderer.setOpaque(true);
        th.setDefaultRenderer(headerRenderer);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(45);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200);  // Title
        table.getColumnModel().getColumn(2).setPreferredWidth(100);  // Category
        table.getColumnModel().getColumn(3).setPreferredWidth(140);  // Location
        table.getColumnModel().getColumn(4).setPreferredWidth(70);   // Status
        table.getColumnModel().getColumn(5).setPreferredWidth(140);  // Reporter
        table.getColumnModel().getColumn(6).setPreferredWidth(130);  // Date

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());

        JPanel center = new JPanel(new BorderLayout());
        center.add(filterBar, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ---- Bottom action bar ----
        JPanel actionBar = buildActionBar();
        add(actionBar, BorderLayout.SOUTH);
    }

    // ---- sub panels ----

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(55, 71, 97));
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Lost & Found Portal");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        p.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Report, search, and claim lost or found items");
        subtitle.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        subtitle.setForeground(new Color(180, 195, 220));
        p.add(subtitle, BorderLayout.EAST);

        return p;
    }

    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setBackground(new Color(235, 238, 244));
        p.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        p.add(new JLabel("Search:"));
        tfSearch = new JTextField(16);
        p.add(tfSearch);

        p.add(new JLabel("Category:"));
        cbCategoryFilter = new JComboBox<>();
        cbCategoryFilter.addItem("All");
        allCategories = categoryDAO.getAllCategories();
        for (Category c : allCategories) cbCategoryFilter.addItem(c.getCategoryName());
        p.add(cbCategoryFilter);

        p.add(new JLabel("Status:"));
        cbStatusFilter = new JComboBox<>(new String[]{"All", "Lost", "Found"});
        p.add(cbStatusFilter);

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> performSearch());
        p.add(btnSearch);

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> {
            tfSearch.setText("");
            cbCategoryFilter.setSelectedIndex(0);
            cbStatusFilter.setSelectedIndex(0);
            refreshTable();
        });
        p.add(btnReset);

        return p;
    }

    private JPanel buildActionBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        p.setBackground(new Color(245, 247, 250));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 215, 225)));

        JButton btnReport = new JButton("📝  Report Item");
        JButton btnMarkFound = new JButton("📍  Mark as Found");
        JButton btnClaim = new JButton("✋  Claim Item");
        JButton btnViewImg = new JButton("🖼  View Image");
        JButton btnMyItems = new JButton("👤  My Items");
        JButton btnClaimsLog = new JButton("📋  Claims Log");
        JButton btnRefresh = new JButton("🔄  Refresh");

        btnReport.addActionListener(e -> openReportDialog());
        btnMarkFound.addActionListener(e -> markSelectedAsFound());
        btnClaim.addActionListener(e -> claimSelected());
        btnViewImg.addActionListener(e -> viewImage());
        btnMyItems.addActionListener(e -> openMyItems());
        btnClaimsLog.addActionListener(e -> openClaimsLog());
        btnRefresh.addActionListener(e -> refreshTable());

        for (JButton b : new JButton[]{btnReport, btnMarkFound, btnClaim, btnViewImg, btnMyItems, btnClaimsLog, btnRefresh}) {
            b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            b.setFocusPainted(false);
            p.add(b);
        }
        return p;
    }

    // --- Table refresh & search ---
    public void refreshTable() {
        currentItems = itemDAO.fetchActiveItems();
        fillTable();
    }

    private void performSearch() {
        String keyword = tfSearch.getText().trim();
        int catIdx = cbCategoryFilter.getSelectedIndex();
        int categoryId = -1;
        if (catIdx > 0 && allCategories != null) categoryId = allCategories[catIdx - 1].getCategoryId();  // skip "All"

        String status = (String) cbStatusFilter.getSelectedItem();
        if ("All".equals(status)) status = null;

        currentItems = itemDAO.searchItems(keyword.isEmpty() ? null : keyword, categoryId, status);
        fillTable();
    }

    private void fillTable() {
        tableModel.setRowCount(0);
        if (currentItems == null) return;
        for (Item it : currentItems) {
            tableModel.addRow(new Object[]{it.getItemId(), it.getTitle(), it.getCategoryName(), it.getLocation(), it.getStatus(), it.getReporterName(), it.getDateReported()});
        }
    }

    //    --- selected item helper ---
    private Item getSelectedItem() {
        int row = table.getSelectedRow();
        if (row < 0 || currentItems == null) return null;
        return currentItems.get(row);
    }

    // --- Actions ---
    private void openReportDialog() {
        ReportItemDialog dlg = new ReportItemDialog(this);
        dlg.setVisible(true);
        if (dlg.isSubmitted()) refreshTable();
    }

    // claim workflow
    private void claimSelected() {
        Item item = getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to claim.");
            return;
        }

        // Only Found items can be claimed
        if (!"Found".equals(item.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only 'Found' items can be claimed.\nTo recover a lost item, contact the reporter.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Identity verification via email
        String email = JOptionPane.showInputDialog(this, "Enter your email to verify your identity:", "Identity Verification", JOptionPane.QUESTION_MESSAGE);
        if (email == null || email.isBlank()) return;

        email = email.trim();
        User claimer = userDAO.findByEmail(email);
        if (claimer == null) {
            // Offer to create a new user record for the claimer
            String name = JOptionPane.showInputDialog(this, "Email not found. Enter your name to register:", "New User", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.isBlank()) return;
            claimer = userDAO.getOrCreateUser(name.trim(), email, null);
            if (claimer == null) {
                JOptionPane.showMessageDialog(this, "Failed to register user.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Cannot claim your own item
        if (claimer.getUserId() == item.getReportedByUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot claim your own item.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Claim \"" + item.getTitle() + "\"?", "Confirm Claim", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = itemDAO.claimItem(item.getItemId(), claimer.getUserId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Item claimed successfully!");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to claim item (it may already be claimed).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Mark a Lost item as Found. used when someone finds a lost item
    private void markSelectedAsFound() {
        Item item = getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to mark as found.");
            return;
        }

        if (!"Lost".equals(item.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only 'Lost' items can be marked as found.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // The person who found it should not be the same person who reported it lost
        String email = JOptionPane.showInputDialog(this, "Enter your email (the person who found this item):", "Identity Verification", JOptionPane.QUESTION_MESSAGE);
        if (email == null || email.isBlank()) return;

        email = email.trim();
        User finder = userDAO.findByEmail(email);
        if (finder == null) {
            String name = JOptionPane.showInputDialog(this, "Email not found. Enter your name to register:", "New User", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.isBlank()) return;
            finder = userDAO.getOrCreateUser(name.trim(), email, null);
            if (finder == null) {
                JOptionPane.showMessageDialog(this, "Failed to register user.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Mark \"" + item.getTitle() + "\" as Found?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = itemDAO.markAsFound(item.getItemId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Item marked as Found! The owner can now claim it.");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update item status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewImage() {
        Item item = getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        Item full = itemDAO.fetchItemById(item.getItemId());
        if (full == null || full.getImageData() == null) {
            JOptionPane.showMessageDialog(this, "No image stored for this item.");
            return;
        }
        ImageIcon icon = new ImageIcon(full.getImageData());
        Image img = icon.getImage();
        if (icon.getIconWidth() > 600 || icon.getIconHeight() > 600) {
            img = img.getScaledInstance(600, -1, Image.SCALE_SMOOTH);
        }
        JLabel lbl = new JLabel(new ImageIcon(img));
        JScrollPane spImg = new JScrollPane(lbl);
        spImg.setPreferredSize(new Dimension(640, 480));
        JOptionPane.showMessageDialog(this, spImg, "Image – " + full.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }

    private void openMyItems() {
        MyItemsFrame frame = new MyItemsFrame(this::refreshTable);
        frame.setVisible(true);
    }

    private void openClaimsLog() {
        ClaimsLogFrame frame = new ClaimsLogFrame();
        frame.setVisible(true);
    }
}
