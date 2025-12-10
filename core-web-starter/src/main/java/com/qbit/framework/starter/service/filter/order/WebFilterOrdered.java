package com.qbit.framework.starter.service.filter.order;

import com.qbit.framework.common.toolkits.enums.DescriptiveEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.Ordered;


@AllArgsConstructor
@Getter
public enum WebFilterOrdered implements Ordered, DescriptiveEnum {

    TraceFilter(Ordered.HIGHEST_PRECEDENCE, "TraceFilter"),
    ContentCachingRequestFilter(Ordered.HIGHEST_PRECEDENCE + 5, "ContentCachingRequestFilter"),
    ErrorHandlingFilter(TraceFilter.getOrder() + 10, "ErrorHandlingFilter"),
    SignatureValidationFilter(TraceFilter.getOrder() + 20, "SignatureValidationFilter"),
    ApiLoggingFilter(TraceFilter.getOrder() + 40, "ApiLoggingFilter"),
    ;

    private final int order;

    private final String desc;


    public Integer getCode() {
        return order;
    }
}
