package com.ranze.literpc.exception;

public enum ErrorEnum {
    SERVICE_EXCEPTION(1000, "Service throw exception"),
    TIMEOUT(1001, "time out"),
    NETWORK_ERROR(1002, "network error"),
    SERVICE_BUSY(1003, "service busy");

    int code;
    String reason;

    ErrorEnum(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

}
