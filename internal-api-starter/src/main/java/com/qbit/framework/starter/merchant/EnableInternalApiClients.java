package com.qbit.framework.starter.merchant;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableFeignClients
/**
 * 启用内部 OpenFeign 客户端扫描。
 *
 * <p>该注解封装了 {@link EnableFeignClients}，用于统一开启并配置内部 API 客户端的扫描。</p>
 *
 * <p>默认扫描包为 {@code com.qbit}，可通过 {@code basePackages} 指定自定义包路径，或者通过 {@code clients}
 * 明确指定需要注册的客户端接口。</p>
 *
 * <p>示例：</p>
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableInternalApiClients(basePackages = {"com.example.client"})
 * public class DemoApplication {}
 * }
 * </pre>
 *
 * @see EnableFeignClients
 */
public @interface EnableInternalApiClients {

    @AliasFor(annotation = EnableFeignClients.class, attribute = "basePackages")
    String[] basePackages() default {"com.qbit"};

    @AliasFor(annotation = EnableFeignClients.class, attribute = "clients")
    Class<?>[] clients() default {};
}

