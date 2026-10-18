package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 DAO for querying the claims log.
 Each row joins claims → items → users (reporter) → users (claimer) to provide a complete picture of who claimed what and when.
 */
public class ClaimDAO {


    // A lightweight record holding all the display fields for a single claim.
    public static class ClaimRecord {
        public int claimId;
        public int itemId;
        public String itemTitle;
        public String category;
        public String location;
        public String reportedBy;     // name of the person who reported (found) the item
        public String reporterEmail;
        public String claimedBy;      // name of the person who claimed the item
        public String claimerEmail;
        public Timestamp claimDate;
        public Timestamp dateReported;
    }


    // Fetches all claims, most recent first.
    public List<ClaimRecord> fetchAllClaims() {
        List<ClaimRecord> results = new ArrayList<>();
        String sql = "SELECT cl.claim_id, cl.claim_date, "
                + "i.item_id, i.title, i.location, i.date_reported, "
                + "cat.category_name, "
                + "reporter.name AS reporter_name, reporter.email AS reporter_email, "
                + "claimer.name  AS claimer_name,  claimer.email  AS claimer_email "
                + "FROM claims cl "
                + "JOIN items i ON cl.item_id = i.item_id "
                + "JOIN categories cat ON i.category_id = cat.category_id "
                + "JOIN users reporter ON i.reported_by_user_id = reporter.user_id "
                + "JOIN users claimer ON cl.claimed_by_user_id = claimer.user_id "
                + "ORDER BY cl.claim_date DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ClaimRecord r = new ClaimRecord();
                r.claimId = rs.getInt("claim_id");
                r.itemId = rs.getInt("item_id");
                r.itemTitle = rs.getString("title");
                r.category = rs.getString("category_name");
                r.location = rs.getString("location");
                r.reportedBy = rs.getString("reporter_name");
                r.reporterEmail = rs.getString("reporter_email");
                r.claimedBy = rs.getString("claimer_name");
                r.claimerEmail = rs.getString("claimer_email");
                r.claimDate = rs.getTimestamp("claim_date");
                r.dateReported = rs.getTimestamp("date_reported");
                results.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching claims log: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }
}
