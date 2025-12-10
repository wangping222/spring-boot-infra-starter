package com.qbit.framework.common.toolkits.exception.type;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class CustomerException extends RuntimeException {

    public static final String DEFAULT_ERROR_CODE = "99999";

    private HttpStatus httpStatus;

    private String message;

    private String code;

    public CustomerException(String code, String message, HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public CustomerException(String message) {
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = DEFAULT_ERROR_CODE;
        this.message = message;
    }
}
