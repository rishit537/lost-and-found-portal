package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.Item;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {
    // --- Insertion ---
    public int insertItem(Item item, File imgFile) {
        String sql = "INSERT INTO items (title, description, location, status, image_data, " + "category_id, reported_by_user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Read image file into byte[] up front
        byte[] imageBytes = null;
        if (imgFile != null && imgFile.exists()) {
            try (FileInputStream fis = new FileInputStream(imgFile)) {
                imageBytes = fis.readAllBytes();
            } catch (IOException e) {
                System.err.println("Error reading image file: " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        } else if (item.getImageData() != null) {
            imageBytes = item.getImageData();
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getTitle());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getLocation());
            ps.setString(4, item.getStatus());

            if (imageBytes != null) {
                ps.setBytes(5, imageBytes);
            } else {
                ps.setNull(5, Types.BLOB);
            }
            ps.setInt(6, item.getCategoryId());
            ps.setInt(7, item.getReportedByUserId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting item: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // --- Search ---
    // Fetches items where {@code status != 'Claimed'}, with optional filters.
    public List<Item> searchItems(String keyword, int categoryId, String status) {
        List<Item> results = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT i.*, c.category_name, u.name AS reporter_name, u.email AS reporter_email "
                + "FROM items i "
                + "JOIN categories c ON i.category_id = c.category_id "
                + "JOIN users u ON i.reported_by_user_id = u.user_id "
                + "WHERE i.status != 'Claimed' ");

        Object[] params = new Object[4];
        int paramCount = 0;

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (i.title LIKE ? OR i.description LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params[paramCount++] = like;
            params[paramCount++] = like;
        }
        if (categoryId > 0) {
            sql.append("AND i.category_id = ? ");
            params[paramCount++] = categoryId;
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND i.status = ? ");
            params[paramCount++] = status;
        }

        sql.append("ORDER BY i.date_reported DESC");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < paramCount; i++) {
                Object p = params[i];
                if (p instanceof String) ps.setString(i + 1, (String) p);
                else if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching items: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    // --- Fetch all unclaimed ---
    public List<Item> fetchActiveItems() {
        return searchItems(null, -1, null);
    }

    // --- Fetch by email ---
    public List<Item> fetchItemsByUserEmail(String email) {
        List<Item> results = new ArrayList<>();
        String sql = "SELECT i.*, c.category_name, u.name AS reporter_name, u.email AS reporter_email "
                + "FROM items i "
                + "JOIN categories c ON i.category_id = c.category_id "
                + "JOIN users u ON i.reported_by_user_id = u.user_id "
                + "WHERE u.email = ? ORDER BY i.date_reported DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching items by user email: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    // --- Fetch a single item by its PK ---
    public Item fetchItemById(int itemId) {
        String sql = "SELECT i.*, c.category_name, u.name AS reporter_name, u.email AS reporter_email "
                + "FROM items i "
                + "JOIN categories c ON i.category_id = c.category_id "
                + "JOIN users u ON i.reported_by_user_id = u.user_id "
                + "WHERE i.item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching item #" + itemId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // --- Update ---
    public boolean updateItem(Item item, File imgFile) {
        // Read image file into byte[] up front so we don't have stream-lifetime issues
        byte[] imageBytes = null;
        if (imgFile != null && imgFile.exists()) {
            try (FileInputStream fis = new FileInputStream(imgFile)) {
                imageBytes = fis.readAllBytes();
            } catch (IOException e) {
                System.err.println("Error reading image file: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else if (item.getImageData() != null) {
            imageBytes = item.getImageData();
        }

        boolean hasNewImage = (imageBytes != null);
        String sql;
        if (hasNewImage) {
            sql = "UPDATE items SET title=?, description=?, location=?, status=?, "
                    + "image_data=?, category_id=? WHERE item_id=?";
        } else {
            sql = "UPDATE items SET title=?, description=?, location=?, status=?, "
                    + "category_id=? WHERE item_id=?";
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getTitle());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getLocation());
            ps.setString(4, item.getStatus());

            if (hasNewImage) {
                ps.setBytes(5, imageBytes);
                ps.setInt(6, item.getCategoryId());
                ps.setInt(7, item.getItemId());
            } else {
                ps.setInt(5, item.getCategoryId());
                ps.setInt(6, item.getItemId());
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating item: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // --- Delete by PK ---
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE item_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting item #" + itemId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // --- Mark as Found ---
    /**
     * Transitions an item from 'Lost' to 'Found'.
     * @return true if the update succeeded (item existed and was in 'Lost' status)
     */
    public boolean markAsFound(int itemId) {
        String sql = "UPDATE items SET status = 'Found' WHERE item_id = ? AND status = 'Lost'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking item #" + itemId + " as found: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // --- Claims ---
    /*
     Claims an item inside a single transaction:
     - UPDATE items SET status = 'Claimed' WHERE item_id = ?
     - INSERT INTO claims (item_id, claimed_by_user_id) VALUES (?, ?)
     Both statements succeed or both roll back.
     return true if the claim succeeded
     */
    public boolean claimItem(int itemId, int claimedByUserId) {
        String updateSql = "UPDATE items SET status = 'Claimed' WHERE item_id = ? AND status != 'Claimed'";
        String insertSql = "INSERT INTO claims (item_id, claimed_by_user_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Mark item as Claimed
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setInt(1, itemId);
                int rows = psUpdate.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;   // already claimed or doesn't exist
                }
            }

            // Insert claim record
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setInt(1, itemId);
                psInsert.setInt(2, claimedByUserId);
                psInsert.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error claiming item #" + itemId + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
        return false;
    }

    // --- Row Mapper ---
    // Maps the current ResultSet row to an Item, reading the BLOB into a byte[] for in-memory image rendering.
    private Item mapRow(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setLocation(rs.getString("location"));
        item.setStatus(rs.getString("status"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setReportedByUserId(rs.getInt("reported_by_user_id"));
        item.setDateReported(rs.getTimestamp("date_reported"));

        // BLOB retrieval
        Blob blob = rs.getBlob("image_data");
        if (blob != null) {
            item.setImageData(blob.getBytes(1, (int) blob.length()));
        }

        // JOIN helper fields
        item.setCategoryName(rs.getString("category_name"));
        item.setReporterName(rs.getString("reporter_name"));
        item.setReporterEmail(rs.getString("reporter_email"));

        return item;
    }
}
