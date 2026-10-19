package com.lostandfound.gui;

import com.lostandfound.dao.ClaimDAO;
import com.lostandfound.dao.ClaimDAO.ClaimRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

//Window showing the full claims history — every item that has been claimed, who reported it, who claimed it, and when
public class ClaimsLogFrame extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    public ClaimsLogFrame() {
        super("Claims Log");
        initComponents();
        loadClaims();
        setSize(950, 500);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ---- Header ----
        JPanel header = new JPanel();
        header.setBackground(new Color(27, 94, 32));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("📋  Claims Log");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);

        JLabel subtitle = new JLabel("History of all claimed items");
        subtitle.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        subtitle.setForeground(new Color(165, 214, 167));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        // ---- Table ----
        String[] columns = {"Claim ID", "Item ID", "Item Title", "Category", "Location", "Reported By", "Claimed By", "Claim Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        table.setFillsViewportHeight(true);

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color even = new Color(241, 248, 233);
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
        th.setBackground(new Color(27, 94, 32));
        th.setForeground(Color.WHITE);
        th.setOpaque(true);
        th.setPreferredSize(new Dimension(th.getWidth(), 34));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBackground(new Color(27, 94, 32));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        headerRenderer.setOpaque(true);
        th.setDefaultRenderer(headerRenderer);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);   // Claim ID
        table.getColumnModel().getColumn(1).setPreferredWidth(60);   // Item ID
        table.getColumnModel().getColumn(2).setPreferredWidth(180);  // Title
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Category
        table.getColumnModel().getColumn(4).setPreferredWidth(140);  // Location
        table.getColumnModel().getColumn(5).setPreferredWidth(130);  // Reported By
        table.getColumnModel().getColumn(6).setPreferredWidth(130);  // Claimed By
        table.getColumnModel().getColumn(7).setPreferredWidth(150);  // Claim Date

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- Bottom bar ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnRefresh = new JButton("🔄  Refresh");
        JButton btnClose = new JButton("Close");
        btnRefresh.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btnClose.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btnRefresh.setFocusPainted(false);
        btnClose.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadClaims());
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnRefresh);
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadClaims() {
        tableModel.setRowCount(0);
        ClaimDAO dao = new ClaimDAO();
        List<ClaimRecord> claims = dao.fetchAllClaims();

        if (claims.isEmpty()) {
            // Show a message in the first row
            tableModel.addRow(new Object[]{"—", "—", "No claims recorded yet", "", "", "", "", ""});
            return;
        }

        for (ClaimRecord r : claims) {
            tableModel.addRow(new Object[]{r.claimId, r.itemId, r.itemTitle, r.category, r.location, r.reportedBy + " (" + r.reporterEmail + ")", r.claimedBy + " (" + r.claimerEmail + ")", r.claimDate});
        }
    }
}
