package com.qbit.framework.starter.service.exception.type;

import com.qbit.framework.starter.service.message.MessageFormatter;
import com.qbit.framework.starter.service.message.MessagePlaceholder;
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

    private Object[] pvParams;

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

    public static CustomerException common(MessagePlaceholder of) {
        String formatted = of == null
                ? "请求不合法"
                : MessageFormatter.java().format(of.getPattern(), of.getArgs());
        return new CustomerException("400", formatted, HttpStatus.BAD_REQUEST);
    }

    public static CustomerException common(String message) {
        String formatted = message == null ? "请求不合法" : message;
        return new CustomerException("400", formatted, HttpStatus.BAD_REQUEST);
    }

}
