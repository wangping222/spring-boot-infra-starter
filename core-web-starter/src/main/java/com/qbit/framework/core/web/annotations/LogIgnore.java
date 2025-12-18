package com.qbit.framework.core.web.annotations;

import com.qbit.framework.core.web.filter.ApiLoggingFilter;

import java.lang.annotation.*;

/**
 * 通过filter 过滤请求日志
 * @author Qbit Framework
 * @see ApiLoggingFilter
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogIgnore {
}