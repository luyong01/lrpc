package com.ranze.literpc.exception;

public class RpcException extends RuntimeException {
    private int code = -1;
    private String message = "unset";

    public RpcException(ErrorEnum errorEnum) {
        this(errorEnum.code, errorEnum.message);
    }

    public RpcException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;

    }

    @Override
    public String toString() {
        return "RpcException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
