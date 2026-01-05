package com.qbit.framework.core.toolkits.exception.code;

/**
 * 业务错误码数据传输对象
 * 用于封装业务异常的错误代码、消息模板和语言信息
 *
 * @author Qbit Framework
 */
public interface BusinessCodeDTO extends ExceptionCode {

    /**
     * 获取业务错误码
     *
     * @return 错误码字符串
     */
    @Override
    String getCode();

    /**
     * 获取错误消息模板
     * 支持占位符格式化，可通过参数动态替换消息内容
     *
     * @return 消息模板字符串
     */
    String getMessageTemplate();

    /**
     * 获取语言标识
     * 用于国际化支持，标识当前错误消息所属的语言
     *
     * @return 语言代码，如 "zh", "en" 等
     */
    String getLanguage();


    /**
     * 格式化错误消息
     * @param args
     * @return
     */
    String getFormatedMessage(Object... args);

}
