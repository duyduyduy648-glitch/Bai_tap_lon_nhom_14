    
    package com.auction.client;
    
    import com.auction.common.model.Auction;
    import com.auction.common.model.Item;
    import com.auction.common.model.User;
    import javafx.application.Application;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.stage.Stage;
    import java.util.HashMap;
    import java.util.Map;
    
    public class MainApp extends Application {
    
        private static Stage primaryStage;
        private static final Map<String, Auction> activeAuctions = new HashMap<>();
        private static User currentUser; // ← THÊM MỚI: lưu user đang đăng nhập
    
        // Lưu user vào session
        public static void setCurrentUser(User user) {
            currentUser = user;
        }
    
        // Lấy user từ session
        public static User getCurrentUser() {
            return currentUser;
        }
    
        public static void registerAuction(String itemId, Auction auction) {
            activeAuctions.put(itemId, auction);
        }

        public static Auction getAuctionForItem(Item item) {
            if (item == null) return null;
            return activeAuctions.get(item.getId());
        }
    
        // === TỰ ĐỘNG LẤY ĐỊA CHỈ NGROK TỪ MẠNG ===
        private String[] fetchServerAddress() {
            String host = "localhost"; // Mặc định nếu mất mạng
            int port = 8888;
            try {
                // BƯỚC 1: Tạo 1 file text trên pastebin.com với nội dung là địa chỉ ngrok (vd: 0.tcp.ap.ngrok.io:12345)
                // BƯỚC 2: Bấm nút 'raw' trên pastebin, copy đường link dán đè lên đoạn link URL này:
                java.net.URL url = new java.net.URL("https://pastebin.com/raw/DÁN_LINK_RAW_CỦA_BẠN_VÀO_ĐÂY"); 
                java.util.Scanner s = new java.util.Scanner(url.openStream());
                if (s.hasNextLine()) {
                    String line = s.nextLine().trim();
                    if (line.contains(":")) {
                        String[] parts = line.split(":");
                        host = parts[0];
                        port = Integer.parseInt(parts[1]);
                        System.out.println("[AutoConfig] Đã tải thành công IP Server: " + host + ":" + port);
                    }
                }
                s.close();
            } catch (Exception e) {
                System.out.println("[AutoConfig] Không thể tải IP từ mạng, dùng localhost mặc định.");
            }
            return new String[]{host, String.valueOf(port)};
        }

        @Override
        public void start(Stage stage) throws Exception {
            try {
                String[] serverConfig = fetchServerAddress();
                NetworkClient.getInstance().connect(serverConfig[0], Integer.parseInt(serverConfig[1]));
            } catch (Exception e) {
                System.err.println("[MainApp Lỗi] Không thể kết nối tới Server: " + e.getMessage());
            }
            primaryStage = stage;
            primaryStage.setTitle("Hệ thống Đấu giá Trực tuyến");
            switchScene("/com/auction/client/view/LoginView.fxml");
            primaryStage.show();
        }

        @Override
        public void stop() throws Exception {
            NetworkClient.getInstance().disconnect();
            super.stop();
        }
    
        public static void switchScene(String fxmlPath) throws Exception {
            Parent root = FXMLLoader.load(MainApp.class.getResource(fxmlPath));
    
            if (fxmlPath.contains("ItemManagement") || fxmlPath.contains("BidderView")) {
                primaryStage.setScene(new Scene(root, 850, 600));
                primaryStage.setTitle("Hệ thống Đấu giá - Nhóm 14");
            } else {
                primaryStage.setScene(new Scene(root, 350, 300));
            }
        }
    
        public static Stage getPrimaryStage() {
            return primaryStage;
        }
    
        public static void main(String[] args) {
            launch(args);
        }
    }
