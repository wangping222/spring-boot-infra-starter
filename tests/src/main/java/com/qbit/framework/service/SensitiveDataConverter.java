package com.qbit.framework.service;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveDataConverter extends MessageConverter {

    // 手机号正则
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)(\\d{4})(\\d{4})");

    // 身份证正则
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})(\\d{8})(\\d{3}[0-9Xx])");

    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1,3})\\w*(@\\w+\\.\\w+)");

    // 银行卡正则
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})(\\d+)(\\d{4})");

    // 密码字段正则
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(password|pwd|密码)[\"':\\s=]+([^\\s,\"'\\}\\]]+)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        return desensitize(message);
    }

    private String desensitize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        // 手机号脱敏: 138****1234
        message = PHONE_PATTERN.matcher(message)
                .replaceAll("$1****$3");

        // 身份证脱敏: 110101********1234
        message = ID_CARD_PATTERN.matcher(message)
                .replaceAll("$1********$3");

        // 邮箱脱敏: abc***@qq.com
        message = EMAIL_PATTERN.matcher(message)
                .replaceAll("$1***$2");

        // 银行卡脱敏: 6222 **** **** 1234
        message = BANK_CARD_PATTERN.matcher(message)
                .replaceAll("$1****$3");

        // 密码脱敏
        message = PASSWORD_PATTERN.matcher(message)
                .replaceAll("$1: ******");

        return message;
    }
}