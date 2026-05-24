package com.auction.client;

import com.auction.common.protocol.Request;
import com.auction.common.protocol.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkClient {
    private static NetworkClient instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();
    private final List<BroadcastListener> broadcastListeners = new CopyOnWriteArrayList<>();
    private Thread listenerThread;
    private volatile boolean running = false;

    public interface BroadcastListener {
        void onBroadcast(String type, Object data);
    }

    private NetworkClient() {}

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    public synchronized void connect(String host, int port) throws IOException {
        if (running) {
            return;
        }
        socket = new Socket(host, port);
        // Khởi tạo ObjectOutputStream trước ObjectInputStream để tránh deadlock
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        running = true;
        responseQueue.clear();

        listenerThread = new Thread(this::listen, "NetworkClient-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
        System.out.println("[NetworkClient] Đã kết nối tới Server " + host + ":" + port);
    }

    public synchronized void disconnect() {
        if (!running) {
            return;
        }
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("[NetworkClient Lỗi] Ngắt kết nối thất bại: " + e.getMessage());
        }
        System.out.println("[NetworkClient] Đã ngắt kết nối an toàn với Server.");
    }

    public synchronized Response sendRequestAndWait(Request req) {
        if (!running) {
            return new Response("ERROR", "Chưa kết nối tới Server!", null);
        }
        try {
            out.writeObject(req);
            out.flush();
            out.reset();
            // Chờ phản hồi từ hàng đợi
            return responseQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Response("ERROR", "Yêu cầu bị gián đoạn!", null);
        } catch (Exception e) {
            disconnect();
            return new Response("ERROR", "Lỗi kết nối Server: " + e.getMessage(), null);
        }
    }

    public void addBroadcastListener(BroadcastListener listener) {
        broadcastListeners.add(listener);
    }

    public void removeBroadcastListener(BroadcastListener listener) {
        broadcastListeners.remove(listener);
    }

    private void listen() {
        while (running) {
            try {
                Response res = (Response) in.readObject();
                if (res == null) {
                    break;
                }
                if (isBroadcast(res)) {
                    handleBroadcast(res);
                } else {
                    responseQueue.put(res);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("[NetworkClient Lỗi] Sai định dạng dữ liệu: " + e.getMessage());
            } catch (IOException e) {
                if (running) {
                    System.err.println("[NetworkClient Lỗi] Mất kết nối tới Server: " + e.getMessage());
                    disconnect();
                }
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private boolean isBroadcast(Response res) {
        if ("SUCCESS".equals(res.getStatus()) && res.getData() instanceof Object[]) {
            Object[] dataArr = (Object[]) res.getData();
            if (dataArr.length == 2 && dataArr[0] instanceof String) {
                String type = (String) dataArr[0];
                return "NEW_ITEM".equals(type) || "BID_UPDATE".equals(type) || "AUCTION_FINISHED".equals(type);
            }
        }
        return false;
    }

    private void handleBroadcast(Response res) {
        Object[] dataArr = (Object[]) res.getData();
        String type = (String) dataArr[0];
        Object data = dataArr[1];
        for (BroadcastListener listener : broadcastListeners) {
            try {
                listener.onBroadcast(type, data);
            } catch (Exception e) {
                System.err.println("[NetworkClient Lỗi] Lỗi xử lý callback Broadcast: " + e.getMessage());
            }
        }
    }
}
