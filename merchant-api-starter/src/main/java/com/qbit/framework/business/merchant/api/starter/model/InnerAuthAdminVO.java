package com.qbit.framework.business.merchant.api.starter.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chenweigang
 * @datetime 11:26
 */
@Data
public class InnerAuthAdminVO {
    @Schema(description = "当前用户")
    private UserDTO user;

    @Schema(description = "当前账户")
    private AssetAccountDTO account;

    @Schema(description = "当前账户-accountMap")
    private AssetAccountMap accountMap;
}