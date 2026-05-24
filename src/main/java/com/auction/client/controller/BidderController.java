
package com.auction.client.controller;

import com.auction.client.MainApp;
import com.auction.client.NetworkClient;
import com.auction.common.model.*;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class BidderController {

    @FXML private TableView<Item> table;
    @FXML private TableColumn<Item, String> idCol, typeCol, nameCol, statusCol;
    @FXML private TableColumn<Item, Double> priceCol;
    @FXML private TableColumn<Item, Double> minIncCol;
    @FXML private Label lblUsername, lblBalance;

    private ObservableList<Item> data;
    private Bidder currentBidder;
    private NetworkClient.BroadcastListener broadcastListener;

    @FXML
    public void initialize() {
        // Setup các cột TableView
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        minIncCol.setCellValueFactory(new PropertyValueFactory<>("minIncrement"));
        typeCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));
        statusCol.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            Auction auction = MainApp.getAuctionForItem(item);
            return auction != null
                ? new SimpleStringProperty(auction.getStatusDisplay())
                : new SimpleStringProperty("NO AUCTION");
        });

        data = FXCollections.observableArrayList();
        table.setItems(data);

        loadDataFromServer();

        // Lấy Bidder từ session MainApp
        User user = MainApp.getCurrentUser();
        if (user instanceof Bidder bidder) {
            setBidder(bidder);
        }

        // Đăng ký nhận Broadcast từ Server
        broadcastListener = (type, payload) -> {
            javafx.application.Platform.runLater(() -> {
                handleServerBroadcast(type, payload);
            });
        };
        NetworkClient.getInstance().addBroadcastListener(broadcastListener);

        table.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showAuctionWindow(row.getItem());
                }
            });
            return row;
        });
    }

    @SuppressWarnings("unchecked")
    private void loadDataFromServer() {
        try {
            Response response = NetworkClient.getInstance().sendRequestAndWait(
                new Request("GET_ITEMS", null)
            );
            if ("SUCCESS".equals(response.getStatus())) {
                List<Auction> auctions = (List<Auction>) response.getData();
                data.clear();
                for (Auction auction : auctions) {
                    MainApp.registerAuction(auction.getItem().getId(), auction);
                    data.add(auction.getItem());
                }
                table.refresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy danh sách đấu giá: " + response.getMessage());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    public void setBidder(Bidder bidder) {
        this.currentBidder = bidder;
        lblUsername.setText("Chào, " + bidder.getUsername());
        lblBalance.setText(String.format("%.2f $", bidder.getAvailableBalance()));
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            if (broadcastListener != null) {
                NetworkClient.getInstance().removeBroadcastListener(broadcastListener);
            }
            MainApp.setCurrentUser(null); // Xóa session khi logout
            MainApp.switchScene("/com/auction/client/view/LoginView.fxml");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi",
                "Không thể quay về màn hình đăng nhập!");
        }
    }

    @FXML
    private void handleSearchItem(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Tìm kiếm sản phẩm");
        dialog.setHeaderText("Nhập từ khóa tìm kiếm (để trống để tải lại toàn bộ):");
        dialog.setContentText("Từ khóa:");
        dialog.showAndWait().ifPresent(keyword -> {
            try {
                Response res = NetworkClient.getInstance().sendRequestAndWait(
                    new Request("SEARCH_ITEMS", keyword)
                );
                if ("SUCCESS".equals(res.getStatus())) {
                    List<Auction> results = (List<Auction>) res.getData();
                    data.clear();
                    for (Auction auction : results) {
                        MainApp.registerAuction(auction.getItem().getId(), auction);
                        data.add(auction.getItem());
                    }
                    table.refresh();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi tìm kiếm", res.getMessage());
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", e.getMessage());
            }
        });
    }

    @FXML
    private void handleMyAuctions(ActionEvent event) {
        if (currentBidder == null) return;
        try {
            Response res = NetworkClient.getInstance().sendRequestAndWait(
                new Request("GET_MY_BIDS", currentBidder.getUsername())
            );
            if ("SUCCESS".equals(res.getStatus())) {
                List<Auction> results = (List<Auction>) res.getData();
                data.clear();
                for (Auction auction : results) {
                    MainApp.registerAuction(auction.getItem().getId(), auction);
                    data.add(auction.getItem());
                }
                table.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Phiên đang tham gia", "Đã hiển thị các phiên đấu giá bạn từng tham gia. \nBấm 'Tìm kiếm' và để trống từ khóa để quay lại danh sách đầy đủ.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", res.getMessage());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    private void handleTopUp(ActionEvent event) {
        if (currentBidder == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Chưa xác định người dùng!");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Nạp tiền vào tài khoản");
        dialog.setHeaderText("Số dư hiện tại: "
            + String.format("%.2f $", currentBidder.getAvailableBalance()));
        dialog.setContentText("Vui lòng nhập số tiền muốn nạp:");
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                currentBidder.deposit(amount);
                
                // Đồng bộ lên Server
                Response res = NetworkClient.getInstance().sendRequestAndWait(
                    new Request("UPDATE_USER", currentBidder)
                );
                if ("SUCCESS".equals(res.getStatus())) {
                    updateBalanceUI();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        String.format("Đã nạp thành công %.2f $ vào tài khoản.", amount));
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi Server", res.getMessage());
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                    "Vui lòng nhập một con số hợp lệ!");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
            }
        });
    }

    @FXML
    private void handlePlaceBid() {
        Item selectedItem = table.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chú ý",
                "Vui lòng chọn một sản phẩm để đặt giá!");
            return;
        }
        showAuctionWindow(selectedItem);
    }

    private void showAuctionWindow(Item item) {
        Auction auction = MainApp.getAuctionForItem(item);
        if (auction == null) return;

        Stage stage = new Stage();
        stage.setTitle("Đấu giá trực tuyến: " + item.getName());

        VBox layout = new VBox(15);
        layout.setPadding(new javafx.geometry.Insets(20));

        Label lblTitle = new Label("CHI TIẾT PHIÊN ĐẤU GIÁ");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        TextArea txtAuctionDetails = new TextArea();
        txtAuctionDetails.setEditable(false);
        txtAuctionDetails.setPrefHeight(250);
        txtAuctionDetails.setText(auction.getInfo());

        Label lblCurrentPrice = new Label(
            "Giá hiện tại: " + auction.getCurrentPrice() + " $");
        lblCurrentPrice.setStyle(
            "-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");

        TextField txtInputBid = new TextField();
        double minPrice = auction.getCurrentPrice() + item.getMinIncrement();
        txtInputBid.setPromptText(String.format("Nhập giá mới (Min: %.1f)", minPrice));

        Button btnBid = new Button("XÁC NHẬN ĐẶT GIÁ");
        btnBid.setMaxWidth(Double.MAX_VALUE);
        btnBid.setStyle(
            "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");

        Timeline detailTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            Auction latestAuction = MainApp.getAuctionForItem(item);
            if (latestAuction != null) {
                txtAuctionDetails.setText(latestAuction.getInfo());
                lblCurrentPrice.setText(
                    String.format("Giá hiện tại: %.1f $", latestAuction.getCurrentPrice()));
                double nextMin = latestAuction.getCurrentPrice() + item.getMinIncrement();
                txtInputBid.setPromptText(String.format("Nhập giá mới (Min: %.1f)", nextMin));
            }
        }));
        detailTimer.setCycleCount(Animation.INDEFINITE);
        detailTimer.play();
        stage.setOnCloseRequest(e -> detailTimer.stop());

        btnBid.setOnAction(e -> {
            processBidLogic(item, txtInputBid.getText());
            txtInputBid.clear();
        });

        // --- AUTO BIDDING UI ---
        Label lblAutoBid = new Label("--- CẤU HÌNH ĐẤU GIÁ TỰ ĐỘNG ---");
        lblAutoBid.setStyle("-fx-font-weight: bold;");
        
        TextField txtMaxBid = new TextField();
        txtMaxBid.setPromptText("Nhập mức giá tối đa ($)");
        
        TextField txtIncrement = new TextField();
        txtIncrement.setPromptText("Nhập bước giá mong muốn ($)");

        Button btnAutoBid = new Button("ĐĂNG KÝ ĐẤU GIÁ TỰ ĐỘNG");
        btnAutoBid.setMaxWidth(Double.MAX_VALUE);
        btnAutoBid.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        
        btnAutoBid.setOnAction(e -> {
            try {
                if (txtMaxBid.getText().isEmpty() || txtIncrement.getText().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Chú ý", "Vui lòng nhập đầy đủ giá tối đa và bước giá!");
                    return;
                }
                double maxBid = Double.parseDouble(txtMaxBid.getText());
                double increment = Double.parseDouble(txtIncrement.getText());
                
                AutoBid autoBid = new AutoBid(currentBidder, item.getId(), maxBid, increment);
                Response response = NetworkClient.getInstance().sendRequestAndWait(
                    new Request("REGISTER_AUTO_BID", autoBid)
                );
                if ("SUCCESS".equals(response.getStatus())) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã đăng ký đấu giá tự động!");
                    txtMaxBid.clear();
                    txtIncrement.clear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", response.getMessage());
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập số hợp lệ!");
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Thông báo", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", ex.getMessage());
            }
        });

        layout.getChildren().addAll(lblTitle, txtAuctionDetails,
            lblCurrentPrice, txtInputBid, btnBid,
            new Separator(), lblAutoBid, txtMaxBid, txtIncrement, btnAutoBid);
        stage.setScene(new Scene(layout, 450, 750));
        stage.show();
    }

    private void processBidLogic(Item item, String amountStr) {
        try {
            if (amountStr == null || amountStr.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Chú ý", "Vui lòng nhập số tiền!");
                return;
            }
            double amount = Double.parseDouble(amountStr);
            BidTransaction newBid = new BidTransaction(currentBidder, amount);
            Auction auction = MainApp.getAuctionForItem(item);
            if (auction == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                    "Không tìm thấy phiên đấu giá cho sản phẩm này!");
                return;
            }

            // Gửi qua Server thay vì đặt trực tiếp cục bộ
            Response response = NetworkClient.getInstance().sendRequestAndWait(
                new Request("PLACE_BID", new Object[]{item.getId(), newBid})
            );

            if ("SUCCESS".equals(response.getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                    "Bạn đã đặt giá thành công!");
                refreshUserBalance();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", response.getMessage());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                "Vui lòng nhập một con số hợp lệ!");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Thông báo đấu giá", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống",
                "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private void handleServerBroadcast(String type, Object payload) {
        switch (type) {
            case "NEW_ITEM" -> {
                Item newItem = (Item) payload;
                Auction newAuction = new Auction(newItem.getSeller(), newItem);
                MainApp.registerAuction(newItem.getId(), newAuction);

                boolean exists = false;
                for (Item current : data) {
                    if (current.getId().equals(newItem.getId())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    data.add(newItem);
                }
                table.refresh();
            }
            case "BID_UPDATE" -> {
                Auction updatedAuction = (Auction) payload;
                MainApp.registerAuction(updatedAuction.getItem().getId(), updatedAuction);

                for (Item current : data) {
                    if (current.getId().equals(updatedAuction.getItem().getId())) {
                        current.setStartingPrice(updatedAuction.getCurrentPrice());
                        current.setBidList(updatedAuction.getBidList());
                        break;
                    }
                }
                table.refresh();
                refreshUserBalance();
            }
            case "AUCTION_FINISHED" -> {
                Auction finishedAuction = (Auction) payload;
                MainApp.registerAuction(finishedAuction.getItem().getId(), finishedAuction);
                table.refresh();
                refreshUserBalance();
            }
        }
    }

    private void refreshUserBalance() {
        if (currentBidder != null) {
            new Thread(() -> {
                try {
                    Response res = NetworkClient.getInstance().sendRequestAndWait(
                        new Request("GET_USER", currentBidder.getUsername())
                    );
                    if ("SUCCESS".equals(res.getStatus())) {
                        User updatedUser = (User) res.getData();
                        if (updatedUser instanceof Bidder b) {
                            javafx.application.Platform.runLater(() -> {
                                setBidder(b);
                            });
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[BidderController] Lỗi cập nhật số dư: " + e.getMessage());
                }
            }).start();
        }
    }

    private void updateBalanceUI() {
        if (currentBidder != null && lblBalance != null) {
            lblBalance.setText(String.format("%.2f $", currentBidder.getAvailableBalance()));
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
