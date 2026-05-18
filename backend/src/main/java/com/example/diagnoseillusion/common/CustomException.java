package com.example.diagnoseillusion.common;

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException {

    private Integer code;

    public CustomException(String message) {
        super(message);
        this.code = 500; // 默认错误码
    }

    public CustomException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}

