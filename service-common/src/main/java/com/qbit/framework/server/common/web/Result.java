package com.qbit.framework.server.common.web;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result<T> {
    /**
     * 状态码
     */
    private String code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 是否成功
     */
    private boolean success;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<T>()
                .setCode("200")
                .setMessage("success")
                .setData(data)
                .setSuccess(true);
    }

    public static <T> Result<T> fail(String message) {
        return fail("500", message);
    }

    public static <T> Result<T> fail(String code, String message) {
        return new Result<T>()
                .setCode(code)
                .setMessage(message)
                .setSuccess(false);
    }
}
