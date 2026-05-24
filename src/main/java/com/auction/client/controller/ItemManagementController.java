package com.auction.client.controller;

import com.auction.client.MainApp;
import com.auction.client.NetworkClient;
import com.auction.common.factory.ItemFactory;
import com.auction.common.model.Item;
import com.auction.common.model.Seller;
import com.auction.common.model.Auction;
import com.auction.common.model.User;
import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.animation.Animation;

import java.time.LocalDateTime;
import java.util.List;

public class ItemManagementController {

    @FXML private TableView<Item> table;
    @FXML private TableColumn<Item, String> idCol;
    @FXML private TableColumn<Item, String> typeCol;
    @FXML private TableColumn<Item, String> nameCol;
    @FXML private TableColumn<Item, Double> priceCol;
    @FXML private TableColumn<Item, String> statusCol;
    @FXML private TableColumn<Item, Double> minIncCol;

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<String> cbType;
    @FXML private Label lblExtraParam;
    @FXML private TextField txtExtraParam;
    @FXML private TextField txtMinIncrement;

    private ObservableList<Item> data;
    private Seller currentSeller;
    private NetworkClient.BroadcastListener broadcastListener;

    @FXML
    public void initialize() {
        if (MainApp.getCurrentUser() instanceof Seller) {
            this.currentSeller = (Seller) MainApp.getCurrentUser();
        }
        // --- 1. Liên kết các cột dữ liệu ---
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Hiển thị loại sản phẩm dựa trên tên lớp (Art, Vehicle...)
        typeCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

        statusCol.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            Auction auction = MainApp.getAuctionForItem(item);
            if (auction != null) {
                return new SimpleStringProperty(auction.getStatusDisplay());
            } else {
                return new SimpleStringProperty("NO AUCTION");
            }
        });

        minIncCol.setCellValueFactory(new PropertyValueFactory<>("minIncrement"));

        // --- 2. Nạp dữ liệu vào bảng ---
        data = FXCollections.observableArrayList();
        table.setItems(data);

        loadDataFromServer();

        // --- 3. Đăng ký nhận Broadcast từ Server ---
        broadcastListener = (type, payload) -> {
            Platform.runLater(() -> {
                handleServerBroadcast(type, payload);
            });
        };
        NetworkClient.getInstance().addBroadcastListener(broadcastListener);

        // --- 4. Cấu hình ComboBox và Extra Field ---
        cbType.getItems().addAll("Art", "Electronics", "Vehicle");
        cbType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateExtraField(newValue);
        });
        cbType.setValue("Art");

        // --- 5. Bộ cập nhật thời gian thực (Làm mới bảng mỗi giây) ---
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> table.refresh())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
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
                new Alert(Alert.AlertType.ERROR, "Không thể lấy danh sách sản phẩm từ Server: " + response.getMessage()).show();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Đã xảy ra lỗi khi tải dữ liệu từ Server: " + e.getMessage()).show();
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
            }
            case "AUCTION_FINISHED" -> {
                Auction finishedAuction = (Auction) payload;
                MainApp.registerAuction(finishedAuction.getItem().getId(), finishedAuction);
                table.refresh();
            }
        }
    }

    @FXML
    private void handleAddItem() {
        try {
            String type = cbType.getValue();
            String id = txtId.getText();
            String name = txtName.getText();
            double price = Double.parseDouble(txtPrice.getText());
            double minIncrement = Double.parseDouble(txtMinIncrement.getText());
            String extraParam = txtExtraParam.getText();

            if (extraParam == null || extraParam.trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng nhập: " + lblExtraParam.getText()).show();
                return;
            }

            // Tạo Item từ Factory
            Item newItem = ItemFactory.createItem(
                currentSeller, type, id, name, "Mô tả sản phẩm", price,
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), minIncrement,
                extraParam
            );

            // Gửi yêu cầu đăng ký sản phẩm đấu giá mới lên Server qua Socket
            Response response = NetworkClient.getInstance().sendRequestAndWait(
                new Request("REGISTER_ITEM", newItem)
            );

            if ("SUCCESS".equals(response.getStatus())) {
                new Alert(Alert.AlertType.INFORMATION, "Đã thêm sản phẩm và khởi tạo phiên đấu giá thành công!").show();
                clearForm();
                // Bảng (TableView) sẽ tự động thêm sản phẩm mới khi nhận được broadcast NEW_ITEM từ Server
            } else {
                new Alert(Alert.AlertType.ERROR, "Lỗi đăng ký sản phẩm: " + response.getMessage()).show();
            }

        } catch (NumberFormatException nfe) {
            new Alert(Alert.AlertType.ERROR, "Lỗi: Giá và bước giá phải là con số!").show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Lỗi hệ thống: " + ex.getMessage()).show();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            if (broadcastListener != null) {
                NetworkClient.getInstance().removeBroadcastListener(broadcastListener);
            }
            MainApp.setCurrentUser(null);
            MainApp.switchScene("/com/auction/client/view/LoginView.fxml");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Không thể quay về màn hình đăng nhập!").show();
        }
    }

    public void setSeller(Seller seller) {
        this.currentSeller = seller;
        System.out.println("Sẵn sàng quản lý cho Seller: " + seller.getUsername());
    }

    private void updateExtraField(String type) {
        if (type == null) return;
        switch (type) {
            case "Art":
                lblExtraParam.setText("Tác giả:");
                txtExtraParam.setPromptText("Nhập tên tác giả");
                break;
            case "Vehicle":
                lblExtraParam.setText("Hãng xe:");
                txtExtraParam.setPromptText("Nhập hãng xe");
                break;
            case "Electronics":
                lblExtraParam.setText("Bảo hành (tháng):");
                txtExtraParam.setPromptText("Nhập số tháng");
                break;
        }
    }

    private void clearForm() {
        txtId.clear();
        txtName.clear();
        txtPrice.clear();
        txtMinIncrement.clear();
        txtExtraParam.clear();
    }
}