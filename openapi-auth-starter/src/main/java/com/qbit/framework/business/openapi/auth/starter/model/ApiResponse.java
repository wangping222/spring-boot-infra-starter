package com.qbit.framework.business.openapi.auth.starter.model;

import lombok.Data;

@Data
public class ApiResponse<T> {
    // 顶层返回码，例如 "000000"，错误时可为 "-1"
    private String code;
    // 顶层返回消息，例如 "success"
    private String message;
    // 数据载荷，包含授权码和时间戳
    private T data;

}