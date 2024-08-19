package com.library.client.exception;

public enum ErrorCode {
    AUTH_INFO_01(500, "Fail"),
    AUTH_INFO_02(500, "Fail"),
    AUTH_INFO_03(500, "Fail"),
    ;
    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
