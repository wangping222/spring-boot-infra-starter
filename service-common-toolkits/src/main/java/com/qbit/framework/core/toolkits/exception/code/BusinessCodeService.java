package com.qbit.framework.core.toolkits.exception.code;

import java.util.List;

/**
 * 业务错误码服务接口
 * 负责查询和管理业务错误码信息，支持多语言错误消息
 *
 * @author Qbit Framework
 */
public interface BusinessCodeService {

    /**
     * 根据错误码查询对应的业务错误信息列表
     * 返回该错误码在不同语言下的错误消息配置
     *
     * @param code 业务错误码
     * @return 业务错误码数据列表，包含不同语言版本的错误消息
     */
    List<ExceptionCode> list(String code);
}
