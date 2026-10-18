package com.lostandfound.model;
import java.sql.Timestamp;

// 'claims' table
public class Claim {
    private int claimId;
    private int itemId;
    private int claimedByUserId;
    private Timestamp claimDate;

    public Claim() {
    }

    public Claim(int claimId, int itemId, int claimedByUserId, Timestamp claimDate) {
        this.claimId = claimId;
        this.itemId = itemId;
        this.claimedByUserId = claimedByUserId;
        this.claimDate = claimDate;
    }

    public int getClaimId() {
        return claimId;
    }

    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getClaimedByUserId() {
        return claimedByUserId;
    }

    public void setClaimedByUserId(int claimedByUserId) {
        this.claimedByUserId = claimedByUserId;
    }

    public Timestamp getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(Timestamp claimDate) {
        this.claimDate = claimDate;
    }

    @Override
    public String toString() {
        return "Claim #" + claimId + " for Item #" + itemId;
    }
}
