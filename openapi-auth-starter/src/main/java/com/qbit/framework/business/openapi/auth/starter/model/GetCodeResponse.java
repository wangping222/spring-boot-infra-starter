package com.qbit.framework.business.openapi.auth.starter.model;

import lombok.Data;

@Data
public class GetCodeResponse {
    private long timestamp;
    private String code;
}
