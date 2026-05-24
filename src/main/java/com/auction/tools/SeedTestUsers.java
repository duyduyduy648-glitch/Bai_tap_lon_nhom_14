package com.auction.tools;

import com.auction.common.model.Bidder;
import com.auction.common.model.Seller;
import com.auction.dao.UserDAO;

/**
 * Tool tạo / RESET tài khoản test trong users.dat.
 * Chạy tool này để reset toàn bộ tài khoản test về trạng thái ban đầu.
 *
 * Tài khoản được tạo / reset:
 *  - bidder1 / 123  (Người mua, số dư 5000$)
 *  - bidder2 / 123  (Người mua, số dư 5000$)
 *  - seller1 / 123  (Người bán)
 */
public class SeedTestUsers {

    public static void main(String[] args) {
        System.out.println("=== RESET / SEED DỮ LIỆU TÀI KHOẢN TEST ===");

        // --- Reset / Tạo Bidder 1 (luôn ghi đè) ---
        Bidder b1 = new Bidder("bidder1", "123");
        b1.deposit(5000.0);
        UserDAO.saveUser(b1);
        System.out.println("[OK] bidder1 / 123  đã được tạo/reset  (Số dư: 5000$)");

        // --- Reset / Tạo Bidder 2 (luôn ghi đè) ---
        Bidder b2 = new Bidder("bidder2", "123");
        b2.deposit(5000.0);
        UserDAO.saveUser(b2);
        System.out.println("[OK] bidder2 / 123  đã được tạo/reset  (Số dư: 5000$)");

        // --- Reset / Tạo Seller 1 (luôn ghi đè) ---
        Seller s1 = new Seller("seller1", "123");
        UserDAO.saveUser(s1);
        System.out.println("[OK] seller1 / 123  đã được tạo/reset  (Người bán)");

        System.out.println("\n=== HOÀN TẤT! Các tài khoản test đã sẵn sàng. ===");
        System.out.println("Khởi động AuctionServer rồi chạy MainApp để đăng nhập.");
    }
}
