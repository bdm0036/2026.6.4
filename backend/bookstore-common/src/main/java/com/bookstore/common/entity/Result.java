package com.bookstore.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static Result<Void> success(String message) {
        return new Result<>(200, message, null);
    }

    public static Result<Void> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static Result<Void> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static Result<Void> error(String message) {
        return new Result<>(500, message, null);
    }

    public static Result<Void> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    public static Result<Void> forbidden(String message) {
        return new Result<>(403, message, null);
    }

    public static Result<Void> notFound(String message) {
        return new Result<>(404, message, null);
    }
}
