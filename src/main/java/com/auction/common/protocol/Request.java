package com.auction.common.protocol;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String type;   // "LOGIN", "REGISTER", "GET_ITEMS", "PLACE_BID", ...
    private Object data;   // Dữ liệu đi kèm

    public Request(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
