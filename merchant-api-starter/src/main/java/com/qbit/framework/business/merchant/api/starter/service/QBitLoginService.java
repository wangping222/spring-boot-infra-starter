package com.qbit.framework.business.merchant.api.starter.service;

/**
 * @author chenweigang
 * @datetime 09:43
 */
public interface QBitLoginService {

    String getAdminLogin(String authToken);


    String getMerchantLogin(String accessToken);
}