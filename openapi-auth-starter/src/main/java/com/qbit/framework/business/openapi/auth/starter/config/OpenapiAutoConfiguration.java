package com.qbit.framework.business.openapi.auth.starter.config;

import com.qbit.framework.business.openapi.auth.starter.properties.OpenapiProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@AutoConfiguration
@EnableConfigurationProperties(OpenapiProperties.class)
@ConditionalOnClass({RedisTemplate.class, RedisConnectionFactory.class})
public class OpenapiAutoConfiguration {
}
