package com.qbit.framework.business.merchant.api.starter.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author martinjiang
 */

@Data

public class AssetAccountDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 662278731620805343L;
    /**
     * int 类型的Id
     */
    private String id;


    private String parentAccountId;

    private String verifiedName;


    private String verifiedNameEn;


    private String accountType;


    private String displayId;


    private String country;

    private String referralCodeId;


    private String prevUserId;


    private String metaData;


    private Long tenantId;


    private AssetAccountTypeEnum type;


    private Long accountId;
}