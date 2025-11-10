package com.qbit.framework.business.merchant.auth.starter.service.impl;

import com.qbit.framework.business.merchant.auth.starter.service.QBitLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @author chenweigang
 * @datetime 09:44
 */
@Slf4j
@Service
public class QBitLoginServiceImpl implements QBitLoginService {


    @Override
    public String getAdminLogin(String authToken) {

        return "";
    }

    @Override
    public String getMerchantLogin(String accessToken) {
        return "";
    }

}