package com.qbit.framework.core.api.model.toolkits.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Pattern;

public class SensitiveDataConverter extends MessageConverter {
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)(\\d{4})(\\d{4})");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})(\\d{8})(\\d{3}[0-9Xx])");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w{1,3})\\w*(@\\w+\\.\\w+)");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})(\\d+)(\\d{4})");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(password|pwd|密码)[\"':\\s=]+([^\\s,\"'\\}\\]]+)", Pattern.CASE_INSENSITIVE);

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        return desensitize(message);
    }

    private String desensitize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        message = PHONE_PATTERN.matcher(message).replaceAll("$1****$3");
        message = ID_CARD_PATTERN.matcher(message).replaceAll("$1********$3");
        message = EMAIL_PATTERN.matcher(message).replaceAll("$1***$2");
        message = BANK_CARD_PATTERN.matcher(message).replaceAll("$1****$3");
        message = PASSWORD_PATTERN.matcher(message).replaceAll("$1: ******");
        return message;
    }
}
