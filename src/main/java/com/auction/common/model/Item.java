package com.auction.common.model;

import java.time.*;
import java.util.ArrayList;

public abstract class Item {
    protected Seller seller;
    protected String id;
    protected String name;
    protected String description;
    protected double startingPrice;
    protected double currentHighestBid;
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;
    protected final double minIncrement;
    private ArrayList<BidTransaction> BidList = new ArrayList<>();
    public Item(Seller seller, String id, String name, String description, double startingPrice,
                LocalDateTime startTime, LocalDateTime endTime, double minIncrement) {
        this.seller = seller;
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice; // Lúc mới tạo, giá cao nhất chính là giá khởi điểm
        this.startTime = startTime;
        this.endTime = endTime;
        this.minIncrement = minIncrement;
        validDateTime();
    }
    public String getDetails() {
        return String.format("Mã SP: %s\nTên: %s\nMô tả: %s\nGiá khởi điểm: %.2f $\nKết thúc lúc: %s",
                id, name, description, startingPrice, endTime.toString());
    }

    /**
     * Xác định trạng thái hiện tại dựa trên thời gian máy tính.
     * Controller gọi: item.getStatusDisplay()
     */
    public String getStatusDisplay() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) return "UPCOMING";
        if (now.isAfter(endTime)) return "FINISHED";
        return "ACTIVE";
    }

    /**
     * Cho phép cập nhật lại giá (startingPrice) khi có người đặt giá thành công.
     * Controller gọi: item.setStartingPrice(amount)
     */
    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }
    // Phương thức trừu tượng thể hiện tính Đa hình (Polymorphism)
    public abstract void displayItemDetails();

    // Thêm phương thức này: Các lớp con (Electronics, Art) BẮT BUỘC phải override
    // để trả về loại của nó (VD: return "Electronics";)
    public abstract String getType();

    // ================= GETTERS VÀ SETTERS =================
    // JavaFX cần các getter này để hiển thị dữ liệu lên bảng (Encapsulation)
    public String getId() {
        return id;
    }
    public double getMinIncrement() {return minIncrement;}
    public Seller getSeller(){return seller;}
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    // THÊM DÒNG NÀY: Fix lỗi cột giá khởi điểm bị trống
    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    private void validDateTime() {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Không được bỏ trống thời gian!");
        }
        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes();
        long days = duration.toDays();
        if (startTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new IllegalArgumentException("Thời gian không được bắt đầu ở quá khứ!");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu!");
        } else if (minutes < 3) {
            throw new IllegalArgumentException("Phiên đấu giá của bạn quá ngắn (tối thiểu 3 phút)!");
        } else if (days > 30) {
            throw new IllegalArgumentException("Phiên đấu giá của bạn quá dài (Tối đa 30 ngày)!");
        }
    }
    public String getDescription() {
        return description;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Nên override toString để lấy thông tin nhanh cho TextArea
    @Override
    public String toString() {
        return "Mã SP: " + id + "\nTên: " + name + "\nMô tả: " + description;
    }
    public ArrayList<BidTransaction> getBidList() {
        if (BidList == null) return new ArrayList<>();
        return BidList;
    }

    public void setBidList(ArrayList<BidTransaction> bidList) {
        this.BidList = bidList;
    }
}