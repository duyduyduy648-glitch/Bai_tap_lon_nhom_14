package com.auction.common.protocol;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status;   // "SUCCESS" hoặc "ERROR"
    private String message;  // Thông báo cụ thể
    private Object data;     // Dữ liệu kèm theo (User, List<Item>,...)

    public Response(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
