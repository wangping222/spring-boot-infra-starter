package com.qbit.framework.core.web.filter.order;

import com.qbit.framework.core.toolkits.enums.DescriptiveEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.Ordered;


/**
 * Web过滤器顺序枚举类
 * <p>
 * 定义了Web层各个过滤器的执行顺序，通过实现{@link Ordered}接口来指定优先级。
 * 过滤器按照order值从小到大的顺序执行，值越小优先级越高。
 * </p>
 *
 * @author Qbit Framework
 * @see Ordered
 * @see DescriptiveEnum
 */
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
