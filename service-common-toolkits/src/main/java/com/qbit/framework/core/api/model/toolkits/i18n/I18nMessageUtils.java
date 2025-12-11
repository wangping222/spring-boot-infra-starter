package com.qbit.framework.core.api.model.toolkits.i18n;

import com.qbit.framework.core.api.model.toolkits.constants.WebConstants;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 国际化消息工具类
 * <p>
 * 提供国际化消息的获取和配置功能，支持：
 * <ul>
 *   <li>根据消息键获取国际化消息</li>
 *   <li>支持消息参数替换</li>
 *   <li>支持默认消息设置</li>
 *   <li>支持自定义 Locale 提供器</li>
 *   <li>支持自定义消息键匹配器</li>
 * </ul>
 * </p>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 设置消息源
 * I18nMessageUtils.setMessageSource(messageSource);
 * 
 * // 设置 Locale 提供器
 * I18nMessageUtils.setLocaleSupplier(() -> LocaleContextHolder.getLocale());
 * 
 * // 获取国际化消息
 * String message = I18nMessageUtils.getMessage("$.user.login.success");
 * 
 * // 获取带参数的国际化消息
 * String message = I18nMessageUtils.getMessage("$.user.welcome", new Object[]{"张三"});
 * }</pre>
 *
 * @author Qbit Framework
 */
public class I18nMessageUtils {

    private I18nMessageUtils() {
        throw new AssertionError();
    }

    /**
     * {@link Locale} Supplier
     */
    private static final AtomicReference<Supplier<Locale>> LOCALE_SUPPLIER = new AtomicReference<>();

    /**
     * i18n 消息源
     */
    private static final AtomicReference<MessageSource> MESSAGE_SOURCE = new AtomicReference<>();

    /**
     * i18n 消息 key 匹配器
     */
    private static final AtomicReference<Predicate<String>> I18N_KEY_MATCHER = new AtomicReference<>(text -> text.startsWith("$."));

    public static String getMessage(String message) {
        return getMessage(message, WebConstants.EMPTY);
    }

    public static String getMessage(String message, String defaultMessage) {
        return getMessage(message, defaultMessage, LOCALE_SUPPLIER.get().get());
    }

    public static String getMessage(String message, String defaultMessage, Locale locale) {
        return getMessage(message, null, defaultMessage, locale);
    }

    public static String getMessage(String message, @Nullable Object[] args) {
        return getMessage(message, args, null, null);
    }

    public static String getMessage(String message, @Nullable Object[] args, Locale locale) {
        return getMessage(message, args, null, locale);
    }

    public static String getMessage(String message, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        if (message != null) {
//            if (MESSAGE_SOURCE.get() == null || !I18N_KEY_MATCHER.get().test(message)) {
//                return message;
//            }
            String result = MESSAGE_SOURCE.get().getMessage(message, args, defaultMessage, getLocale(locale));
            if (StringUtils.hasText(result)) {
                return result;
            }
        }
        // 未获取到消息返回默认消息或原本消息
        return StringUtils.hasText(defaultMessage) ? defaultMessage : message;
    }

    private static Locale getLocale(@Nullable Locale defaultLocale) {
        defaultLocale = defaultLocale == null ? Locale.ENGLISH : defaultLocale;
        if (LOCALE_SUPPLIER.get() == null) {
            return defaultLocale;
        }
        Locale result = LOCALE_SUPPLIER.get().get();
        return result == null ? defaultLocale : result;
    }

    public static void setMessageSource(MessageSource messageSource) {
        MESSAGE_SOURCE.set(messageSource);
    }

    public static void setLocaleSupplier(Supplier<Locale> supplier) {
        LOCALE_SUPPLIER.set(supplier);
    }

    /**
     * @return 获取当前上下文的 locale
     */
    @NotNull
    public static Locale requireLocale() {
        return getLocale(Locale.ENGLISH);
    }

    /**
     * 设置 i18n 消息 key 的匹配器
     *
     * @param matcher 匹配器
     */
    public static void setI18nKeyMatcher(Predicate<String> matcher) {
        I18N_KEY_MATCHER.set(matcher);
    }
}