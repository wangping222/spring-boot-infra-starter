package com.qbit.framework.business.service.starter.filter.config;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD})
@Retention(RUNTIME)
@Documented
@Conditional(ServiceFilterEnabledCondition.class)
public @interface ConditionalOnServiceFilter {
    String value();
}