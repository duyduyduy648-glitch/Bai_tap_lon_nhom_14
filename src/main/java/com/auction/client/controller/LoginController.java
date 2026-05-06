package com.auction.client.controller;

import com.auction.client.MainApp;
import com.auction.common.model.*; // Import thêm Seller, Bidder
import com.auction.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Thêm FXMLLoader
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

                // 1. Giữ nguyên switch-case gán đường dẫn của bạn
                switch (userRole) {
                    case ADMIN:
                        fxmlPath = "/com/auction/client/view/AdminView.fxml";
                        break;
                    case SELLER:
                        fxmlPath = "/com/auction/client/view/ItemManagementView.fxml";
                        break;
                    case BIDDER:
                        fxmlPath = "/com/auction/client/view/BidderView.fxml";
                        break;
                    default:
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không xác định được vai trò!");
                        return;
                }

                System.out.println("Đang cố gắng mở giao diện: " + fxmlPath);

                // 2. BỒI THÊM: Logic chuyển cảnh có truyền dữ liệu (thay cho MainApp.switchScene đơn thuần)
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                // 3. BỒI THÊM: Kiểm tra và truyền User vào Controller tương ứng
                if (userRole == Role.SELLER) {
                    ItemManagementController controller = loader.getController();
                    controller.setSeller((Seller) user); // Truyền Seller vào biến currentSeller
                }
                // Sau này bạn bồi thêm cho BIDDER tại đây:
                // else if (userRole == Role.BIDDER) {
                //    BidderController controller = loader.getController();
                //    controller.setBidder((Bidder) user);
                // }

                // 4. Hiển thị cảnh mới (Giữ đúng chức năng của switchScene)
                MainApp.getPrimaryStage().setScene(new Scene(root));
                MainApp.getPrimaryStage().show();

            } catch (Exception e) {
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