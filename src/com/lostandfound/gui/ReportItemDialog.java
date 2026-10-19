package com.lostandfound.gui;

import com.lostandfound.dao.CategoryDAO;
import com.lostandfound.dao.ItemDAO;
import com.lostandfound.dao.UserDAO;
import com.lostandfound.model.Category;
import com.lostandfound.model.Item;
import com.lostandfound.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;


//  Modal dialog for reporting a new Lost or Found item.

public class ReportItemDialog extends JDialog {

    private JTextField tfName, tfEmail, tfPhone;
    private JTextField tfTitle, tfLocation;
    private JTextArea taDescription;
    private JComboBox<Category> cbCategory;
    private JComboBox<String> cbStatus;
    private JLabel lblImagePath;
    private File selectedImageFile;
    private boolean submitted = false;

    // For edit mode
    private Item editingItem;

//     Create mode - brand-new item.

    public ReportItemDialog(Frame owner) {
        this(owner, null, null);
    }

    // Edit mode: pre-fills every field.
    public ReportItemDialog(Frame owner, Item item, String reporterEmail) {
        super(owner, item == null ? "Report Lost / Found Item" : "Edit Item", true);
        this.editingItem = item;
        initComponents(reporterEmail);
        setSize(520, 620);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents(String reporterEmail) {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(14, 18, 14, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        // ---- Section- User Info ----
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel sectionUser = new JLabel("Your Information");
        sectionUser.setFont(sectionUser.getFont().deriveFont(Font.BOLD, 14f));
        content.add(sectionUser, gbc);
        row++;

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = row;
        content.add(new JLabel("Name *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        tfName = new JTextField(20);
        content.add(tfName, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        content.add(new JLabel("Email *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        tfEmail = new JTextField(20);
        content.add(tfEmail, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        content.add(new JLabel("Phone"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        tfPhone = new JTextField(20);
        content.add(tfPhone, gbc);
        row++;

        // ---- Section: Item Details ----
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        JLabel sectionItem = new JLabel("Item Details");
        sectionItem.setFont(sectionItem.getFont().deriveFont(Font.BOLD, 14f));
        content.add(sectionItem, gbc);
        row++;
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = row;
        content.add(new JLabel("Title *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        tfTitle = new JTextField(20);
        content.add(tfTitle, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        content.add(new JLabel("Description"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        taDescription = new JTextArea(3, 20);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        taDescription.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(taDescription);
        content.add(sp, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        content.add(new JLabel("Location *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        tfLocation = new JTextField(20);
        content.add(tfLocation, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        content.add(new JLabel("Category *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        cbCategory = new JComboBox<>();
        loadCategories();
        content.add(cbCategory, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        content.add(new JLabel("Status *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        cbStatus = new JComboBox<>(new String[]{"Lost", "Found"});
        content.add(cbStatus, gbc);
        row++;

        // ---- Image Upload ----
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        content.add(new JLabel("Image"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        JPanel imgPanel = new JPanel(new BorderLayout(6, 0));
        lblImagePath = new JLabel("No file selected");
        lblImagePath.setForeground(Color.GRAY);
        JButton btnBrowse = new JButton("Browse…");
        btnBrowse.addActionListener(e -> browseImage());
        imgPanel.add(lblImagePath, BorderLayout.CENTER);
        imgPanel.add(btnBrowse, BorderLayout.EAST);
        content.add(imgPanel, gbc);
        row++;

        // ---- Buttons ----
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnSubmit = new JButton(editingItem == null ? "Submit" : "Save Changes");
        JButton btnCancel = new JButton("Cancel");
        btnSubmit.addActionListener(e -> onSubmit());
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnSubmit);
        btnPanel.add(btnCancel);
        content.add(btnPanel, gbc);

        setContentPane(new JScrollPane(content));

        // Pre-fill in edit mode
        if (editingItem != null) {
            tfTitle.setText(editingItem.getTitle());
            taDescription.setText(editingItem.getDescription());
            tfLocation.setText(editingItem.getLocation());
            cbStatus.setSelectedItem(editingItem.getStatus());
            selectCategoryById(editingItem.getCategoryId());

            if (reporterEmail != null) {
                UserDAO userDAO = new UserDAO();
                User reporter = userDAO.findByEmail(reporterEmail);
                if (reporter != null) {
                    tfName.setText(reporter.getName());
                    tfEmail.setText(reporter.getEmail());
                    tfPhone.setText(reporter.getPhoneNumber());
                }
            }
            tfEmail.setEditable(false);
        }
    }

    //--- Helpers ---
    private void loadCategories() {
        Category[] cats = new CategoryDAO().getAllCategories();
        for (Category c : cats) cbCategory.addItem(c);
    }

    private void selectCategoryById(int catId) {
        for (int i = 0; i < cbCategory.getItemCount(); i++) {
            if (cbCategory.getItemAt(i).getCategoryId() == catId) {
                cbCategory.setSelectedIndex(i);
                return;
            }
        }
    }

    private void browseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "bmp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fc.getSelectedFile();
            lblImagePath.setText(selectedImageFile.getName());
            lblImagePath.setForeground(Color.BLACK);
        }
    }

    private void onSubmit() {
        // Validate required fields
        String name = tfName.getText().trim();
        String email = tfEmail.getText().trim();
        String title = tfTitle.getText().trim();
        String location = tfLocation.getText().trim();

        if (name.isEmpty() || email.isEmpty() || title.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Email, Title, and Location are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getOrCreateUser(name, email, tfPhone.getText().trim());
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Failed to create / retrieve user.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Category selectedCat = (Category) cbCategory.getSelectedItem();
            ItemDAO itemDAO = new ItemDAO();

            if (editingItem == null) {
                // CREATE mode
                Item item = new Item();
                item.setTitle(title);
                item.setDescription(taDescription.getText().trim());
                item.setLocation(tfLocation.getText().trim());
                item.setStatus((String) cbStatus.getSelectedItem());
                item.setCategoryId(selectedCat.getCategoryId());
                item.setReportedByUserId(user.getUserId());

                int newId = itemDAO.insertItem(item, selectedImageFile);
                if (newId > 0) {
                    submitted = true;
                    JOptionPane.showMessageDialog(this, "Item reported successfully!  (ID: " + newId + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // EDIT mode
                editingItem.setTitle(title);
                editingItem.setDescription(taDescription.getText().trim());
                editingItem.setLocation(tfLocation.getText().trim());
                editingItem.setStatus((String) cbStatus.getSelectedItem());
                editingItem.setCategoryId(selectedCat.getCategoryId());

                boolean ok = itemDAO.updateItem(editingItem, selectedImageFile);
                if (ok) {
                    submitted = true;
                    JOptionPane.showMessageDialog(this, "Item updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean isSubmitted() {
        return submitted;
    }
}
