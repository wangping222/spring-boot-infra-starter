package com.qbit.framework.business.merchant.auth.starter.model;

import lombok.Data;

/**
 * @author fengkewei
 * @date 2024/3/21 12:08
 */
@Data
public class AccountDTO {

    private String id;

    private String uuId;

    private String parentAccountId;

    private String verifiedName;

    private String verifiedNameEn;

    private String accountType;

    private String displayId;

    private String status;

    private String type;

}
