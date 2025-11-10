package com.qbit.framework.business.merchant.auth.starter.model;

import lombok.Data;

/**
 * @author chenweigang
 * @datetime 15:57
 */
@Data
public class InnerApiBaseVO<T> {


    private static final String SUCCESS = "success";

    private int code;

    /**
     * 自定义错误信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 错误堆栈信息
     * tips: 生产环境不打印
     */
    private String stackTrace;

    public InnerApiBaseVO() {
        code = 200;
        message = SUCCESS;
    }

    public InnerApiBaseVO(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public static InnerApiBaseVO<?> ok() {
        return new InnerApiBaseVO<>();
    }


    public static InnerApiBaseVO<?> error(String message) {
        return new InnerApiBaseVO<>(500, message);
    }

    public static InnerApiBaseVO<?> error(int code, String message) {
        return new InnerApiBaseVO<>(code, message);
    }

}