package com.qbit.framework.core.toolkits.exception.code;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Locale;

/**
 * 异常码接口
 * 定义业务异常码的标准规范，所有业务异常码枚举都应实现此接口
 *
 * <p>设计原则：
 * <ul>
 *   <li>单一职责：只定义异常码的基本属性，不处理消息格式化</li>
 *   <li>易用性：提供清晰的属性访问方式</li>
 *   <li>扩展性：支持自定义异常码的扩展实现</li>
 *   <li>国际化：通过不同的异常码枚举支持多语言</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * public enum UserExceptionCode implements ExceptionCode {
 *     USER_NOT_FOUND("USER_001", "用户不存在", HttpStatus.NOT_FOUND),
 *     USER_DISABLED("USER_002", "用户已被禁用", HttpStatus.FORBIDDEN),
 *     INVALID_PASSWORD("USER_003", "密码不正确", HttpStatus.UNAUTHORIZED);
 *
 *     private final String code;
 *     private final String desc;
 *     private final HttpStatus httpStatus;
 *
 *     UserExceptionCode(String code, String desc, HttpStatus httpStatus) {
 *         this.code = code;
 *         this.desc = desc;
 *         this.httpStatus = httpStatus;
 *     }
 *
 *     &#64;Override
 *     public String getCode() {
 *         return code;
 *     }
 *
 *     &#64;Override
 *     public String getDesc() {
 *         return desc;
 *     }
 *
 *     &#64;Override
 *     public HttpStatus getHttpStatus() {
 *         return httpStatus;
 *     }
 * }
 * </pre>
 *
 * @author Qbit Framework
 */
public interface ExceptionCode extends Serializable {

    /**
     * 获取业务错误码
     * 错误码应该具有唯一性和可读性，建议格式：模块前缀_数字编号
     *
     * <p>示例：
     * <ul>
     *   <li>USER_001 - 用户模块错误码</li>
     *   <li>ORDER_001 - 订单模块错误码</li>
     *   <li>PAY_001 - 支付模块错误码</li>
     * </ul>
     *
     * @return 业务错误码
     */
    String getCode();

    /**
     * 获取错误描述
     * 描述应该清晰、简洁，便于用户理解错误原因
     *
     * @return 错误描述
     */
    String getDesc();

    /**
     * 获取英文描述（可选）
     * 用于国际化场景
     *
     * @return 英文描述，默认返回空字符串
     */
    default String getEnDesc() {
        return "";
    }

    /**
     * 获取语言标识
     * 用于国际化支持，标识当前错误消息所属的语言
     *
     * @return 语言代码，如 "zh", "en" 等，默认中文
     */
    default String getLanguage() {
        return Locale.ENGLISH.getLanguage();
    }


    /**
     * 格式化错误消息
     * 将消息模板中的占位符替换为实际参数值
     *
     * @param args 消息参数
     * @return 格式化后的错误消息
     */
    default String getFormatedMessage(Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        String template;
        if (Locale.ENGLISH == locale) {
            template = getEnDesc();
        } else {
            template = getDesc();
        }
        try {
            return String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }

    /**
     * 获取建议的HTTP状态码
     * 默认返回 500 Internal Server Error
     * 子类可以重写此方法返回更合适的HTTP状态码
     *
     * <p>常用状态码映射：
     * <ul>
     *   <li>400 - 请求参数错误</li>
     *   <li>401 - 未认证</li>
     *   <li>403 - 无权限</li>
     *   <li>404 - 资源不存在</li>
     *   <li>409 - 资源冲突</li>
     *   <li>429 - 请求过于频繁</li>
     *   <li>500 - 服务器内部错误</li>
     * </ul>
     *
     * @return HTTP状态码
     */
    default HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 判断是否为客户端错误（4xx）
     *
     * @return true表示客户端错误
     */
    default boolean isClientError() {
        return getHttpStatus().is4xxClientError();
    }

    /**
     * 判断是否为服务器错误（5xx）
     *
     * @return true表示服务器错误
     */
    default boolean isServerError() {
        return getHttpStatus().is5xxServerError();
    }
}
