package com.qbit.framework.business.merchant.auth.starter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fengkewei
 * @date 2024/3/1 16:31
 */
@AllArgsConstructor
@Getter
public enum PlatformTypeEnum {
    /**
     * admin端
     */
    ADMIN(0),

    /**
     * 商户端
     */
    MERCHANT(1),

    ;
    private final Integer value;
}
