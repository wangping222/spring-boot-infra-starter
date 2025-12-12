package com.qbit.framework.core.web.enums;

import com.mybatisflex.annotation.EnumValue;
import com.qbit.framework.core.api.model.toolkits.enums.DescriptiveEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Qbit Framework
 */
@Getter
@AllArgsConstructor
public enum PrincipalType implements DescriptiveEnum {

    USERNAME(1, "用户名"),

    MOBILE_PHONE(2, "手机号码"),

    EMAIL(3, "邮箱");

    @EnumValue
    private final Integer code;

    private final String desc;

}
