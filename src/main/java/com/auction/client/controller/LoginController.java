package com.auction.client.controller;

import com.auction.client.MainApp;
import com.auction.common.model.Role;
import com.auction.common.model.User;
import com.auction.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        User user = UserDAO.authenticate(username, password);

        if (user != null) {
            String fxmlPath = "";
            try {
                Role userRole = user.getRole();

                // Gán đường dẫn file FXML dựa theo vai trò
                switch (userRole) {
                    case ADMIN:
                        fxmlPath = "/com/auction/client/view/AdminView.fxml";
                        break;
                    case SELLER:
                        fxmlPath = "/com/auction/client/view/ItemManagementView.fxml"; // File này bạn ĐÃ CÓ
                        break;
                    case BIDDER:
                        fxmlPath = "/com/auction/client/view/BidderView.fxml";
                        break;
                    default:
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không xác định được vai trò!");
                        return;
                }

                System.out.println("Đang cố gắng mở giao diện: " + fxmlPath);
                MainApp.switchScene(fxmlPath);

            } catch (Exception e) {
                // In lỗi chi tiết màu đỏ ra màn hình console (Run)
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi thiếu file FXML",
                        "Không tìm thấy file: " + fxmlPath + "\nBạn cần tạo file này trong thư mục resources!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập", "Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    @FXML
    private void handleGoToRegister() {
        String registerPath = "/com/auction/client/view/RegisterView.fxml";
        try {
            System.out.println("Đang cố gắng mở giao diện: " + registerPath);
            MainApp.switchScene(registerPath);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi thiếu file FXML",
                    "Không tìm thấy file: " + registerPath + "\nBạn chưa tạo file giao diện đăng ký!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}