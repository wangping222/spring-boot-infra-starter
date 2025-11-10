package com.qbit.framework.business.openapi.auth.starter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiPathEnum {
    GET_CODE("GET","open-api/v3/oauth/authorize"),
    GENERATE_ACCESS_TOKEN("POST","open-api/v3/oauth/access-token"),
    ;
    private final String method;
    private final String path;
}
