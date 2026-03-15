package com.example.vo.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private int code;
    private String msg;
    private T data;

    public ApiResult(int i, String success) {
        this.code = i;
        this.msg = success;
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "success", data);
    }

    public static <T> ApiResult<T> fail(String msg) {
        return new ApiResult<>(500, msg, null);
    }

    public static ApiResult success() {
        return new ApiResult<>(200, "success");
    }
}