package com.ranze.literpc.exception;

public class RpcException extends RuntimeException {
    private int code = -1;
    private String reason = "unset";

    public RpcException(ErrorEnum errorEnum) {
        this(errorEnum.code, errorEnum.reason);
    }

    public RpcException(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;

    }

    @Override
    public String toString() {
        return "RpcException{" +
                "code=" + code +
                ", reason='" + reason + '\'' +
                '}';
    }
}
