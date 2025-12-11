package com.qbit.framework.core.web.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
public class AppContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, BeanDefinitionRegistryPostProcessor {

    /**
     * @key bean name
     * @value bean definition supplier
     */
    private static final Map<String, Supplier<BeanDefinition>> BEAN_DEFINITIONS = new ConcurrentHashMap<>();

    static {
        BEAN_DEFINITIONS.put(ApplicationContextUtils.class.getName(), () -> new RootBeanDefinition(ApplicationContextUtils.class));
    }

    static void registerBean(String bean, Supplier<BeanDefinition> supplier) {
        BEAN_DEFINITIONS.put(bean, supplier);
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        log.info("Add AppContextInitializer");
        context.addBeanFactoryPostProcessor(this);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("Register Wind BeanDefinition");
        register(registry);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    private void register(BeanDefinitionRegistry registry) {
        BEAN_DEFINITIONS.forEach((name, supplier) -> {
            if (!registry.containsBeanDefinition(name)) {
                registry.registerBeanDefinition(name, supplier.get());
            }
        });
    }

}
