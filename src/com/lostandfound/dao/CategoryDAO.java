package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for categories
public class CategoryDAO {
    // every row
    public Category[] getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM categories ORDER BY category_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            e.printStackTrace();
        }
        return list.toArray(new Category[0]);
    }
}
