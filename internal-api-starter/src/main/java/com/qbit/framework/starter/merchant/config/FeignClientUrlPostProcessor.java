package com.qbit.framework.starter.merchant.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;

/**
 * Feign Client URL 后置处理器。
 * <p>
 * 为未显式配置 URL 的 FeignClient 填充统一基础地址。
 * <p>
 * 注意：作为 BeanFactoryPostProcessor，此类在 Spring 容器启动早期执行，
 * 此时 @ConfigurationProperties 还未绑定，因此必须使用 Environment 直接读取配置。
 *
 * @author framework
 */
public class FeignClientUrlPostProcessor implements BeanFactoryPostProcessor {

    private static final String FEIGN_CLIENT_FACTORY_BEAN = "org.springframework.cloud.openfeign.FeignClientFactoryBean";
    private static final String BASE_URL_PROPERTY = "framework.feign.api.base-url";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // 从 Environment 中直接读取配置值
        Environment environment = beanFactory.getBean(Environment.class);
        String baseUrl = environment.getProperty(BASE_URL_PROPERTY);

        if (StringUtils.isEmpty(baseUrl)) {
            return;
        }

        // 遍历所有 Bean 定义，找到 FeignClient 并设置 URL
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            var beanDefinition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();

            if (!FEIGN_CLIENT_FACTORY_BEAN.equals(beanClassName)) {
                continue;
            }

            MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
            PropertyValue urlProperty = propertyValues.getPropertyValue("url");
            String existingUrl = urlProperty != null ? String.valueOf(urlProperty.getValue()) : null;

            // 只为未配置 URL 的 FeignClient 设置基础地址
            if (StringUtils.isEmpty(existingUrl)) {
                propertyValues.add("url", baseUrl);
            }
        }
    }
}
