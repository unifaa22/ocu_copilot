package com.example.diagnoseillusion.common;

public class Result<T> {

    private Integer code;     // 状态码
    private String message;   // 提示信息
    private T data;           // 返回数据

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功（无数据返回）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 成功（有数据返回）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功（自定义提示信息和数据返回）
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 失败（返回默认失败状态码和信息）
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null);
    }

    // 失败（自定义提示信息）
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    // 失败（自定义状态码和提示信息）
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // Getter and Setter
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

