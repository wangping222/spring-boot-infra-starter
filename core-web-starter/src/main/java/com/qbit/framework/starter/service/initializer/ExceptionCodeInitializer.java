package com.qbit.framework.starter.service.initializer;

import com.qbit.framework.common.toolkits.exception.code.BusinessCodeService;
import com.qbit.framework.common.toolkits.exception.factory.CustomerExceptionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

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
