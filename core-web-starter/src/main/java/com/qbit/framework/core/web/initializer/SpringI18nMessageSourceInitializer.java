package com.qbit.framework.core.web.initializer;

import com.qbit.framework.core.api.model.toolkits.i18n.I18nMessageUtils;
import com.qbit.framework.core.api.model.toolkits.request.HttpServletRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;


/**
 * spring i18n 国际化支持 初始化器
 **/
@Slf4j
public class SpringI18nMessageSourceInitializer implements SystemInitializer {


    /**
     * @return 获取当前请求的 Locale
     */
    private static Locale getWebRequestLocal(LocaleResolver localeResolver) {
        HttpServletRequest request = HttpServletRequestUtils.getContextRequestOfNullable();
        if (request == null) {
            return Locale.ENGLISH;
        }
        return localeResolver.resolveLocale(request);
    }

    @Override
    public boolean requireInitialize() {
        return true;
    }

    @Override
    public void initialize(ApplicationContext applicationContext) {
        if (applicationContext instanceof ConfigurableWebServerApplicationContext) {
            // 仅在 Web 上下文中执行
            try {
                I18nMessageUtils.setMessageSource(applicationContext.getBean(MessageSource.class));
                I18nMessageUtils.setLocaleSupplier(() -> getWebRequestLocal(applicationContext.getBean(LocaleResolver.class)));
                log.info("enabled i18n supported");
            } catch (Exception ignore) {
                log.info("un enabled i18n supported");
            }
        }
    }

}
