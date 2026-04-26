package com.auction.common.factory;

import com.auction.common.model.Art;
import com.auction.common.model.Electronics;
import com.auction.common.model.Item;
import com.auction.common.model.Vehicle;

import java.time.LocalDateTime;

public class ItemFactory {

    // Phương thức Factory (Factory Method)
    public static Item createItem(String type, String id, String name, String description,
                                  double startingPrice, LocalDateTime startTime, LocalDateTime endTime,
                                  String extraAttribute) {
        switch (type.toLowerCase()) {
            case "art":
                // extraAttribute đóng vai trò là tên họa sĩ (artist)
                return new Art(id, name, description, startingPrice, startTime, endTime, extraAttribute);
            case "electronics":
                // extraAttribute đóng vai trò là số tháng bảo hành
                int warranty = Integer.parseInt(extraAttribute);
                return new Electronics(id, name, description, startingPrice, startTime, endTime, warranty);
            case "vehicle":
                // extraAttribute đóng vai trò là hãng xe
                return new Vehicle(id, name, description, startingPrice, startTime, endTime, extraAttribute);
            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ: " + type);
        }
    }
}