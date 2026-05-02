package com.auction.client.controller;

import com.auction.client.MainApp;
import com.auction.common.factory.ItemFactory;
import com.auction.common.model.Item;
import com.auction.dao.ItemDAO;
import com.auction.dao.JsonItemDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;

public class ItemManagementController {

    // --- CÁC THÀNH PHẦN GIAO DIỆN QUẢN LÝ SẢN PHẨM ---
    @FXML private TableView<Item> table;
    @FXML private TableColumn<Item, String> idCol;
    @FXML private TableColumn<Item, String> nameCol;
    @FXML private TableColumn<Item, Double> priceCol;

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<String> cbType;

    // TODO: Tạm thời dùng DAO để test UI. Sau này phần này phải đổi thành Socket gọi lên Server!
    private final ItemDAO itemDAO = new JsonItemDAO();
    private ObservableList<Item> data;

    // --- HÀM KHỞI TẠO (Tự động chạy khi mở màn hình) ---
    @FXML
    public void initialize() {
        // 1. Cấu hình cột
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

        // 2. Load dữ liệu
        data = FXCollections.observableArrayList(itemDAO.getAllItems());
        table.setItems(data);

        // 3. Cấu hình ComboBox
        cbType.getItems().addAll("Art", "Electronics", "Vehicle");
        cbType.setValue("Art");
    }

    // --- HÀM XỬ LÝ NÚT BẤM ---

    // Logic khi bấm nút "Thêm sản phẩm"
    @FXML
    private void handleAddItem() {
        try {
            String type = cbType.getValue();
            String id = txtId.getText();
            String name = txtName.getText();
            double price = Double.parseDouble(txtPrice.getText());

            // Áp dụng Factory Method Pattern
            Item newItem = ItemFactory.createItem(type, id, name, "Mô tả", price, LocalDateTime.now(), LocalDateTime.now().plusDays(7), "Default");

            // Lưu dữ liệu và cập nhật bảng
            itemDAO.saveItem(newItem);
            data.add(newItem);

            // Xóa form sau khi thêm thành công
            txtId.clear(); txtName.clear(); txtPrice.clear();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Vui lòng kiểm tra lại thông tin nhập vào (Giá phải là số)!");
            alert.show();
        }
    }

    // Logic khi bấm nút "Đăng xuất" ở thanh Header
    @FXML
    private void handleLogout() {
        try {
            // Quay trở về màn hình đăng nhập
            MainApp.switchScene("/com/auction/client/view/LoginView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể quay về màn hình đăng nhập!");
            alert.show();
        }
    }
}