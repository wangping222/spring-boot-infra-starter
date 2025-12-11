package com.qbit.framework.core.api.model.toolkits.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Pattern;


/**
 * 敏感数据转换器
 * <p>
 * 用于在日志输出时自动对敏感信息进行脱敏处理，继承自 Logback 的 MessageConverter。
 * 支持对以下类型的敏感数据进行脱敏：
 * <ul>
 *   <li>手机号：保留前3位和后4位，中间4位使用****代替</li>
 *   <li>身份证号：保留前6位和后4位，中间8位使用********代替</li>
 *   <li>邮箱地址：保留用户名前1-3位和域名部分，中间使用***代替</li>
 *   <li>银行卡号：保留前4位和后4位，中间使用****代替</li>
 *   <li>密码：检测到password/pwd/密码关键字时，将其值替换为******</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用方式：在 logback.xml 配置文件中配置该转换器
 * <pre>
 * &lt;conversionRule conversionWord="msg" converterClass="com.qbit.framework.core.api.model.toolkits.logging.SensitiveDataConverter"/&gt;
 * </pre>
 * </p>
 *
 * @author Qbit Framework
 * @since 1.0
 */
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
