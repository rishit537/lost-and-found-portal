package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.User;

import java.sql.*;

// Users DAO
public class UserDAO {

    // return User if found, null otherwise
    public User findByEmail(String email) {
        String sql = "SELECT user_id, name, email, phone_number FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhoneNumber(rs.getString("phone_number"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error looking up user by email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Inserts a new user and returns the user_id, or -1 if error
    public int insertUser(User user) {
        String sql = "INSERT INTO users (name, email, phone_number) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhoneNumber());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    user.setUserId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }


    // Returns an existing user for the given email, or creates one and returns it.
    public User getOrCreateUser(String name, String email, String phone) {
        User existing = findByEmail(email);
        if (existing != null) {
            return existing;
        }
        User u = new User(0, name, email, phone);
        int id = insertUser(u);
        if (id > 0) {
            u.setUserId(id);
            return u;
        }
        return null;
    }
}
