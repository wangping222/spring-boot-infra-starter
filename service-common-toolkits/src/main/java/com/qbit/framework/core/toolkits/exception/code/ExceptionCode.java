package com.qbit.framework.core.toolkits.exception.code;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 异常码接口
 * 定义业务异常码的标准规范，所有业务异常码枚举都应实现此接口
 * 
 * <p>设计目的：
 * <ul>
 *   <li>统一异常码的定义规范</li>
 *   <li>支持类型安全的异常码管理</li>
 *   <li>便于集中管理和维护异常码</li>
 *   <li>提供异常码到HTTP状态码的映射</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>
 * public enum UserExceptionCode implements ExceptionCode {
 *     USER_NOT_FOUND("USER_001", "用户不存在"),
 *     USER_DISABLED("USER_002", "用户已被禁用"),
 *     INVALID_PASSWORD("USER_003", "密码不正确");
 *     
 *     private final String code;
 *     private final String desc;
 *     
 *     UserExceptionCode(String code, String desc) {
 *         this.code = code;
 *         this.desc = desc;
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
     * 获取错误描述信息
     * 描述应该清晰、简洁，便于开发人员理解和排查问题
     * 注意：这是默认描述，实际展示给用户的消息可能需要通过国际化处理
     *
     * @return 错误描述
     */
    String getDesc();

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
     * 获取英文描述
     * 用于国际化场景，默认返回空字符串
     * 如需支持英文，子类可重写此方法
     *
     * @return 英文描述
     */
    default String getEnDesc() {
        return "";
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
