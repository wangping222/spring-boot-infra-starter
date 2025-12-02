package com.qbit.framework.starter.service.initializer;

import com.qbit.framework.starter.service.exception.code.BusinessCodeService;
import com.qbit.framework.starter.service.exception.factory.CustomerExceptionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
public class ExceptionCodeInitializer implements SystemInitializer {
    @Override
    public boolean requireInitialize() {
        return true;
    }

    @Override
    public void initialize(ApplicationContext context) {
        try {
            BusinessCodeService businessCodeService = context.getBean(BusinessCodeService.class);
            CustomerExceptionFactory.setBusinessCodeService(businessCodeService);
            log.info("Initialized BusinessCodeService for CustomerCodeUtils");
        } catch (Exception e) {
            log.warn("Failed to initialize BusinessCodeService for CustomerCodeUtils", e);
        }
    }
}
