package com.auction.client.controller;

import com.auction.common.model.Auction;
import com.auction.common.model.BidTransaction;
import javafx.stage.Stage;
import javafx.scene.Scene;
import com.auction.client.MainApp;
import com.auction.common.model.Bidder; // Bồi thêm: Model Bidder
import com.auction.common.model.Item;   // Bồi thêm: Model Item
import com.auction.dao.ItemDAO;         // Bồi thêm: DAO lấy hàng
import com.auction.dao.JsonItemDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;
import javafx.scene.layout.VBox;

public class BidderController {

    // --- BỒI THÊM: Các UI Components cho TableView và thông tin Bidder ---
    @FXML
    private TableView<Item> table;
    @FXML
    private TableColumn<Item, String> idCol, typeCol, nameCol, statusCol;
    @FXML
    private TableColumn<Item, Double> priceCol;
    @FXML
    private Label lblUsername, lblBalance;

    // --- BỒI THÊM: Logic Fields ---
    private final ItemDAO itemDAO = new JsonItemDAO();
    private ObservableList<Item> data;
    private Bidder currentBidder;
    private Auction currentAuction;

    // --- BỒI THÊM: Hàm initialize để thiết lập bảng và đồng bộ thời gian ---
    @FXML
    public void initialize() {
        // Cấu hình các cột
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));

        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatusDisplay()));

        // Nạp dữ liệu
        data = FXCollections.observableArrayList(itemDAO.getAllItems());
        table.setItems(data);
        for (Item item : data) {
            if (MainApp.getAuctionForItem(item) == null) {
                // Kiểm tra an toàn: chỉ tạo nếu item đã có seller
                if (item.getSeller() != null) {
                    // Cập nhật tham số thứ 3 là item.getMinIncrement() thay vì để cứng 10.0
                    Auction auction = new Auction(item.getSeller(), item);
                    MainApp.registerAuction(item.getId(), auction);
                } else {
                    System.out.println("Cảnh báo: Sản phẩm " + item.getId() + " đang thiếu thông tin người bán!");
                }
            }
        }

        // Bồi thêm: Bộ cập nhật thời gian thực để Bidder thấy giây nhảy lùi
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> table.refresh())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        // BỒI THÊM: Click đúp vào sản phẩm để mở cửa sổ đấu giá riêng
        table.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Item selectedItem = row.getItem();
                    showAuctionWindow(selectedItem); // Hàm này sẽ viết ở Bước 2
                }
            });
            return row;
        });
    }

    // --- BỒI THÊM: Nhận dữ liệu Bidder từ LoginController ---
    public void setBidder(Bidder bidder) {
        this.currentBidder = bidder;
        lblUsername.setText("Chào, " + bidder.getUsername());
        lblBalance.setText(String.format("%.2f $", bidder.getAvailableBalance()));
    }

    // 1. Xử lý nút Đăng xuất (GIỮ NGUYÊN 100% CODE CŨ)
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

    // 2. Xử lý nút "Tìm kiếm sản phẩm" (GIỮ NGUYÊN 100% CODE CŨ)
    @FXML
    private void handleSearchItem(ActionEvent event) {
        // Tạm thời hiển thị thông báo, sau này bạn sẽ viết code mở giao diện tìm kiếm ở đây
        showAlert(Alert.AlertType.INFORMATION, "Tính năng đang phát triển", "Mở giao diện Tìm kiếm sản phẩm...");
    }

    // 3. Xử lý nút "Phiên đang tham gia" (GIỮ NGUYÊN 100% CODE CŨ)
    @FXML
    private void handleMyAuctions(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Nạp tiền vào tài khoản");
        dialog.setHeaderText("Số dư hiện tại: " + String.format("%.2f $", currentBidder.getAvailableBalance()));
        dialog.setContentText("Vui lòng nhập số tiền muốn nạp:");

        // 2. Xử lý khi người dùng nhấn OK
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);

                // 3. Gọi hàm deposit từ lớp Bidder
                currentBidder.deposit(amount);

                // 4. Cập nhật lại giao diện hiển thị số dư
                updateBalanceUI();

                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        String.format("Đã nạp thành công %.2f $ vào tài khoản.", amount));

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập một con số hợp lệ!");
            } catch (IllegalArgumentException e) {
                // Bắt lỗi số tiền âm từ hàm deposit của lớp Bidder
                showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
            }
        });
    }

    // 4. Xử lý nút "Nạp tiền / Số dư" (GIỮ NGUYÊN 100% CODE CŨ)
    @FXML
    private void handleTopUp(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Nạp tiền vào tài khoản");
        dialog.setHeaderText("Số dư hiện tại: " + String.format("%.2f $", currentBidder.getAvailableBalance()));
        dialog.setContentText("Vui lòng nhập số tiền muốn nạp:");

        // 2. Xử lý khi người dùng nhấn OK
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);

                // 3. Gọi hàm deposit từ lớp Bidder
                currentBidder.deposit(amount);

                // 4. Cập nhật lại giao diện hiển thị số dư
                updateBalanceUI();

                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        String.format("Đã nạp thành công %.2f $ vào tài khoản.", amount));

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập một con số hợp lệ!");
            } catch (IllegalArgumentException e) {
                // Bắt lỗi số tiền âm từ hàm deposit của lớp Bidder
                showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
            }
        });
    }

    // --- BỒI THÊM: Xử lý Đặt giá (Mới) ---
    @FXML
    private void handlePlaceBid() {
        Item selectedItem = table.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý", "Vui lòng chọn một sản phẩm để đặt giá!");
            return;
        }

        // Ở đây bạn sẽ bồi thêm logic gọi auction.placeBid()
        // và trừ tiền/đóng băng tiền của currentBidder
        showAuctionWindow(selectedItem);
    }

    // Hàm phụ trợ để hiển thị hộp thoại thông báo (GIỮ NGUYÊN 100% CODE CŨ)
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAuctionWindow(Item item) {
        Stage stage = new Stage();
        stage.setTitle("Đấu giá trực tuyến: " + item.getName());

        // Layout chính cho cửa sổ popup
        VBox layout = new VBox(15);
        layout.setPadding(new javafx.geometry.Insets(20));

        // 1. Hiển thị getInfo() từ Model
        Label lblTitle = new Label("CHI TIẾT SẢN PHẨM");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        TextArea txtInfo = new TextArea(item.getDetails()); // Hoặc gọi auction.getInfo()
        txtInfo.setEditable(false);
        txtInfo.setPrefHeight(100);

        // 2. Hiển thị BidList (Lịch sử đặt giá)
        Label lblHistory = new Label("Lịch sử đặt giá (BidList):");
        ListView<String> bidListView = new ListView<>();
        bidListView.setPrefHeight(120);
        // bidListView.getItems().addAll(auction.getBidListAsString()); // Bồi dữ liệu thật ở đây

        // 3. Khu vực thao tác đặt giá và hiển thị số tiền hiện tại
        Label lblCurrentPrice = new Label("Giá hiện tại: " + item.getCurrentHighestBid() + " $");
        lblCurrentPrice.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        TextField txtInputBid = new TextField();
        txtInputBid.setPromptText("Nhập số tiền đấu giá mới...");

        Button btnBid = new Button("XÁC NHẬN ĐẶT GIÁ");
        btnBid.setMaxWidth(Double.MAX_VALUE);
        btnBid.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");

        // Xử lý khi nhấn nút Đặt giá ngay trên cửa sổ mới này
        btnBid.setOnAction(e -> {
            processBidLogic(item, txtInputBid.getText());
            lblCurrentPrice.setText("Giá hiện tại: " + item.getStartingPrice() + " $"); // Cập nhật lại giá hiển thị
        });

        layout.getChildren().addAll(lblTitle, txtInfo, lblHistory, bidListView, lblCurrentPrice, txtInputBid, btnBid);

        stage.setScene(new Scene(layout, 400, 500));
        stage.show();
    }

    private void processBidLogic(Item item, String amountStr) {
        try {
            if (amountStr == null || amountStr.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Chú ý", "Vui lòng nhập số tiền!");
                return;
            }
            // 1. Chuyển đổi dữ liệu nhập vào
            double amount = Double.parseDouble(amountStr);

            // 2. Tạo đối tượng giao dịch đặt giá mới
            // Cần đảm bảo BidTransaction của bạn nhận (Bidder, double) trong Constructor
            BidTransaction newBid = new BidTransaction(currentBidder, amount);

            // 3. Lấy đối tượng Auction đang quản lý Item này
            // Giả sử bạn có một cách để lấy Auction (qua Map hoặc Manager)
            // Nếu bạn đang lưu Auction ngay trong Item thì dùng: item.getAuction()
            Auction auction = MainApp.getAuctionForItem(item);

            if (auction == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy phiên đấu giá cho sản phẩm này!");
                return;
            }

            // 4. GỌI AUCTION XỬ LÝ (QUAN TRỌNG NHẤT)
            // Hàm placeBid(newBid) của bạn sẽ tự động:
            // - Check status (UPCOMING/ACTIVE/FINISHED)
            // - Check giá tối thiểu (minIncrement)
            // - Check nếu người dùng đang giữ giá cao nhất (lastBid.getBidder())
            // - Tự động gọi lastBid.refund() để trả tiền cho người cũ
            // - Tự động gọi newBid.frozen() để trừ tiền của currentBidder
            auction.placeBid(newBid);

            // --- NẾU CHẠY ĐẾN ĐÂY LÀ THÀNH CÔNG ---

            // 5. Cập nhật dữ liệu hiển thị (UI)
            // Cập nhật lại số dư khả dụng (vì đã bị trừ trong auction.placeBid -> newBid.frozen)
            lblBalance.setText(String.format("%.2f $", currentBidder.getAvailableBalance()));

            // Cập nhật lại giá hiển thị trên bảng chính (lấy giá mới nhất từ Auction)
            item.setCurrentHighestBid(auction.getCurrentPrice());
            table.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Bạn đã đặt giá thành công!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập một con số hợp lệ!");
        } catch (IllegalArgumentException e) {
            // Đây là nơi bắt tất cả các lỗi logic bạn đã viết trong Auction.java
            // Ví dụ: "Bạn đang là người giữ giá cao nhất!", "Phiên đấu giá đã kết thúc!", v.v.
            showAlert(Alert.AlertType.ERROR, "Thông báo đấu giá", e.getMessage());
        } catch (Exception e) {
            // Bắt các lỗi hệ thống khác
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Có lỗi xảy ra: " + e.getMessage());
        }
    }
    private void updateBalanceUI() {
        if (currentBidder != null) {
            // Cập nhật số dư lên Label lblBalance
            // Giả sử label của bạn tên là lblBalance, hãy đổi tên nếu khác nhé
            lblBalance.setText(String.format("%.2f $", currentBidder.getAvailableBalance()));
        }
    }
}