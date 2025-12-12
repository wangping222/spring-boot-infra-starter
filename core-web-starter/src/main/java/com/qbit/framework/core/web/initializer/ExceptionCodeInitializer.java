package com.qbit.framework.core.web.initializer;

import com.qbit.framework.core.api.model.toolkits.exception.code.BusinessCodeService;
import com.qbit.framework.core.api.model.toolkits.exception.factory.CustomerExceptionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * @author Qbit Framework
 */
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
