package com.auction.server;

import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;
import com.auction.common.model.Auction;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class TestClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8888;

        System.out.println("=================================================");
        System.out.println("     KẾT NỐI KIỂM THỬ THỬ NGHIỆM CLIENT - SOCKET   ");
        System.out.println("=================================================");

        try {
            System.out.println("[Client] Đang kết nối tới Server tại " + host + ":" + port + "...");
            Socket socket = new Socket(host, port);
            System.out.println("[Client] Kết nối thành công!");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // 1. Tạo một luồng phụ để liên tục nghe thông báo phát sóng (Broadcast) từ Server
            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        Response res = (Response) in.readObject();
                        if (res == null) break;

                        // Kiểm tra xem có phải là thông báo broadcast không
                        if ("SUCCESS".equals(res.getStatus()) && res.getData() instanceof Object[]) {
                            Object[] payload = (Object[]) res.getData();
                            if (payload.length == 2 && payload[0] instanceof String) {
                                String broadcastType = (String) payload[0];
                                Object broadcastData = payload[1];
                                
                                System.out.println("\n📣 [BROADCAST NHẬN ĐƯỢC] Loại tin: " + broadcastType);
                                if (broadcastData instanceof Auction) {
                                    Auction auction = (Auction) broadcastData;
                                    System.out.println("   -> Sản phẩm: " + auction.getItem().getName() 
                                            + " | Giá hiện tại: " + auction.getCurrentPrice() + " $");
                                } else {
                                    System.out.println("   -> Nội dung: " + res.getMessage());
                                }
                                continue;
                            }
                        }

                        // Phản hồi thông thường
                        System.out.println("\n📩 [Client Nhận] Phản hồi: " + res.getStatus() + " | " + res.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("[Client] Luồng nghe thông báo đã dừng (Ngắt kết nối).");
                }
            });
            receiveThread.setDaemon(true); // Để luồng phụ tự tắt khi luồng chính tắt
            receiveThread.start();

            // 2. Gửi yêu cầu Đăng Nhập kiểm tra thử
            System.out.println("\n[Client] Gửi yêu cầu LOGIN với tài khoản admin...");
            Request loginReq = new Request("LOGIN", new String[]{"admin", "admin123"});
            out.writeObject(loginReq);
            out.flush();

            // Chờ một chút để xem kết quả
            Thread.sleep(1500);

            // 3. Gửi yêu cầu lấy toàn bộ sản phẩm
            System.out.println("\n[Client] Gửi yêu cầu GET_ITEMS...");
            Request getItemsReq = new Request("GET_ITEMS", null);
            out.writeObject(getItemsReq);
            out.flush();

            // Chờ một chút để xem kết quả và nghe broadcast
            Thread.sleep(3000);

            // Đóng kết nối
            System.out.println("\n[Client] Đang tắt client...");
            socket.close();
            System.out.println("[Client] Đã đóng kết nối an toàn.");

        } catch (Exception e) {
            System.err.println("[Client Lỗi] Không thể giao tiếp với Server: " + e.getMessage());
            System.err.println("👉 VUI LÒNG ĐẢM BẢO RẰNG 'AuctionServer' ĐÃ ĐƯỢC KHỞI CHẠY TRƯỚC!");
        }
    }
}
