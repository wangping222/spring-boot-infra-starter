package com.qbit.framework.business.service.starter;


import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum AppType {
    /**
     * 运营端
     */
    ADMIN(1, "运营", "admin"),

    /**
     * 会员端
     */
    MEMBER(2, "会员", "member"),

    ;
    @EnumValue
    private final Integer code;

    private final String desc;

    private final String value;

    public static AppType of(String value) {
        return Arrays.stream(AppType.values())
                .filter(appType -> appType.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }

}
