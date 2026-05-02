package com.auction.client.controller;

import com.auction.client.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class BidderController {

    // 1. Xử lý nút Đăng xuất
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Trở về màn hình đăng nhập
            MainApp.switchScene("/com/auction/client/view/LoginView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể quay về màn hình đăng nhập!");
        }
    }

    // 2. Xử lý nút "Tìm kiếm sản phẩm"
    @FXML
    private void handleSearchItem(ActionEvent event) {
        // Tạm thời hiển thị thông báo, sau này bạn sẽ viết code mở giao diện tìm kiếm ở đây
        showAlert(Alert.AlertType.INFORMATION, "Tính năng đang phát triển", "Mở giao diện Tìm kiếm sản phẩm...");
    }

    // 3. Xử lý nút "Phiên đang tham gia"
    @FXML
    private void handleMyAuctions(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Tính năng đang phát triển", "Mở danh sách các phiên đấu giá bạn đang theo dõi...");
    }

    // 4. Xử lý nút "Nạp tiền / Số dư"
    @FXML
    private void handleTopUp(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Tính năng đang phát triển", "Mở giao diện Nạp tiền vào tài khoản...");
    }

    // Hàm phụ trợ để hiển thị hộp thoại thông báo
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}