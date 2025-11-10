package com.qbit.framework.business.merchant.auth.starter.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author random
 * @date 2024/4/10 17:30
 */
@Data
public class ApiBaseRequestDTO {
    @Schema(description = "签名")
    private String sign;

    @Schema(description = "发起时间")
    private long timestamp;

    @Schema(description = "随机数")
    private String nonceStr;
}