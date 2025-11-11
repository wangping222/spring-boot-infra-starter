package com.qbit.framework.business.merchant.api.starter.model;


import lombok.Data;

/**
 * @author fengkewei
 * @date 2024/3/21 12:08
 */
@Data
public class UserDTO {

    private String id;

    private String nickname;

    private String email;

    private String phone;

    private AccountDTO accountDTOInfo;

    private Long accountMapId;

    private PlatformTypeEnum type;
}
