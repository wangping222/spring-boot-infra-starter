package com.qbit.framework.business.merchant.api.starter.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author martinjiang
 */
@Getter
@AllArgsConstructor
public enum AssetAccountTypeEnum {
    /**
     * Qbit 内部
     */
    Qbit,
    /**
     * 商户
     */
    Merchant,
    /**
     * 合伙人
     */
    Channel,
    /**
     * 子账号
     */
    SubAccount,
    /**
     * 母账号
     */
    MasterAccount,
    /**
     * 测试账户
     */
    TestAccount,
    /**
     * 使用《新开公司》业务的账户
     */
    NewOpenAccount,
    /**
     * api 账户
     */
    ApiClient,
    /**
     * 开放api账户的客户
     */
    ApiClientCustomer,
    /**
     * 开放api持有人, parentAccountId类型为ApiClientCustomer
     */
    ApiClientHolder,
    /**
     * 提现账户
     */
    ApiWithdraw,
    /**
     * 服务商（能登录admin 后台）
     */
    Agent,
    /**
     * 渠道方（能登录admin 后台）
     */
    NewChannel,
    /**
     * 个人版渠道商（负责获客个人版客户，能登录个人版合伙人admin 后台）
     */
    IndividualChannel,
    /**
     * CNY结算账户
     */
    CNYSettle,

    AssetBuyer,

    /**
     * 白标admin 可以登录白标admin端
     */
    WhiteLabelAdmin,

    /**
     * 员工账户
     */
    Employee,
    /**
     * 代理商下商户
     */
    AcquiringMerchantForAgent,
    /**
     * 直清机构下商户
     */
    AcquiringMerchantForDirectPartner,
    /**
     * 间清机构下商户
     */
    AcquiringMerchantForIndirectPartner,
    /**
     * 直清机构
     */
    AcquiringDirectPartner,
    /**
     * 间清机构
     */
    AcquiringIndirectPartner,

    ;

    public String getValue() {
        return name();
    }

    public static AssetAccountTypeEnum getByValue(String value) {
        AssetAccountTypeEnum[] accountTypeEnums = AssetAccountTypeEnum.values();
        for (AssetAccountTypeEnum accountTypeEnum : accountTypeEnums) {
            if (accountTypeEnum.name().equals(value)) {
                return accountTypeEnum;
            }
        }
        return null;
    }
}