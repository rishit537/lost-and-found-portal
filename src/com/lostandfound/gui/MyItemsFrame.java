package com.lostandfound.gui;

import com.lostandfound.dao.ItemDAO;
import com.lostandfound.dao.UserDAO;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


// "My Items" window – the user enters their email to view, edit, or delete items they personally reported.
public class MyItemsFrame extends JFrame {

    private JTextField tfEmail;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Item> currentItems;
    private final Runnable onChangeCallback;   // refreshes main dashboard

    public MyItemsFrame(Runnable onChangeCallback) {
        super("My Reported Items");
        this.onChangeCallback = onChangeCallback;
        initComponents();
        setSize(820, 480);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // ---- Top Panel: email lookup ----
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Your Email:"));
        tfEmail = new JTextField(22);
        top.add(tfEmail);
        JButton btnLoad = new JButton("Load My Items");
        btnLoad.addActionListener(e -> loadItems());
        top.add(btnLoad);

        // ---- Table ----
        String[] cols = {"ID", "Title", "Category", "Location", "Status", "Date Reported"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        JScrollPane sp = new JScrollPane(table);

        // ---- Bottom buttons ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnViewImg = new JButton("View Image");
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnViewImg.addActionListener(e -> viewImage());
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnViewImg);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    // --- Actions ---
    private void loadItems() {
        String email = tfEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email.");
            return;
        }
        currentItems = new ItemDAO().fetchItemsByUserEmail(email);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        if (currentItems == null) return;
        for (Item it : currentItems) {
            tableModel.addRow(new Object[]{it.getItemId(), it.getTitle(), it.getCategoryName(), it.getLocation(), it.getStatus(), it.getDateReported()});
        }
    }

    private Item getSelectedItem() {
        int row = table.getSelectedRow();
        if (row < 0 || currentItems == null) return null;
        return currentItems.get(row);
    }


    //      Identity verification: the email entered in the text field must match the item's reporter.
    private boolean verifyOwner(Item item) {
        String email = tfEmail.getText().trim();
        if (item.getReporterEmail() == null || !item.getReporterEmail().equalsIgnoreCase(email)) {
            JOptionPane.showMessageDialog(this, "You can only modify items you reported.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void editSelected() {
        Item item = getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        if (!verifyOwner(item)) return;

        ReportItemDialog dlg = new ReportItemDialog(this, item, item.getReporterEmail());
        dlg.setVisible(true);
        if (dlg.isSubmitted()) {
            loadItems();
            if (onChangeCallback != null) onChangeCallback.run();
        }
    }

    private void deleteSelected() {
        Item item = getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        if (!verifyOwner(item)) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Delete \"" + item.getTitle() + "\" permanently?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = new ItemDAO().deleteItem(item.getItemId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Item deleted.");
                loadItems();
                if (onChangeCallback != null) onChangeCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete item.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewImage() {
        Item item = getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        // Fetch full item to get BLOB
        Item full = new ItemDAO().fetchItemById(item.getItemId());
        if (full == null || full.getImageData() == null) {
            JOptionPane.showMessageDialog(this, "No image stored for this item.");
            return;
        }
        ImageIcon icon = new ImageIcon(full.getImageData());
        // Scale down if too large
        Image img = icon.getImage();
        if (icon.getIconWidth() > 600 || icon.getIconHeight() > 600) {
            img = img.getScaledInstance(600, -1, Image.SCALE_SMOOTH);
        }
        JLabel lbl = new JLabel(new ImageIcon(img));
        JScrollPane sp = new JScrollPane(lbl);
        sp.setPreferredSize(new Dimension(640, 480));
        JOptionPane.showMessageDialog(this, sp, "Image – " + full.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }
}
