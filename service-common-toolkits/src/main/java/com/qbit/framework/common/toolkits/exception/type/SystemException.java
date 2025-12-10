package com.qbit.framework.common.toolkits.exception.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SystemException extends RuntimeException {

    private String code;
    private String message;

    public SystemException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public SystemException(String message) {
        super(message);
        this.message = message;
    }

    public SystemException(String message, Exception e) {
        super(message);
        this.message = message;
    }
}
