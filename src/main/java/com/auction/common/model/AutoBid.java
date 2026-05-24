package com.auction.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AutoBid implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Bidder bidder;
    private final String itemId;
    private final double maxBid;
    private final double increment;
    private final LocalDateTime registeredTime;

    public AutoBid(Bidder bidder, String itemId, double maxBid, double increment) {
        if (bidder == null || itemId == null || itemId.isEmpty()) {
            throw new IllegalArgumentException("Bidder và ItemId không được bỏ trống");
        }
        if (maxBid <= 0 || increment <= 0) {
            throw new IllegalArgumentException("Giá tối đa và bước giá tự động phải lớn hơn 0");
        }
        this.bidder = bidder;
        this.itemId = itemId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredTime = LocalDateTime.now();
    }

    public Bidder getBidder() { return bidder; }
    public String getItemId() { return itemId; }
    public double getMaxBid() { return maxBid; }
    public double getIncrement() { return increment; }
    public LocalDateTime getRegisteredTime() { return registeredTime; }
}
