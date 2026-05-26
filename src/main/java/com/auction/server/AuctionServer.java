package com.auction.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    // Railway tự inject biến môi trường PORT, nếu chạy local thì dùng 8888
    public static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8888"));

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN - SERVER CHẠY   ");
        System.out.println("=================================================");

        // Đảm bảo AuctionManager được khởi tạo sớm để nạp dữ liệu từ file JSON/DAT
        AuctionManager.getInstance();

        ExecutorService threadPool = Executors.newCachedThreadPool();

        // Đăng ký shutdown hook để tắt scheduler ngầm an toàn khi Server dừng
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Đang tắt hệ thống Server...");
            AuctionManager.getInstance().shutdown();
            threadPool.shutdown();
            System.out.println("[Server] Đã tắt Server an toàn.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] Đang lắng nghe kết nối trên cổng: " + PORT + "...");
            System.out.println("[Server] Sẵn sàng phục vụ nhiều Client đồng thời.");
            System.out.println("-------------------------------------------------");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Kết nối mới từ địa chỉ: " + clientSocket.getInetAddress().getHostAddress());
                
                // Giao kết nối cho 1 thread trong pool xử lý
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("[Server Lỗi] Gặp sự cố nghiêm trọng: " + e.getMessage());
        }
    }
}
