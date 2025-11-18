package com.qbit.framework.business.merchant.starter.signature;

import com.qbit.framework.business.merchant.starter.properties.FeignApiProperties;

public class PropertiesSecretProvider implements SecretProvider {
    private final FeignApiProperties properties;

    public PropertiesSecretProvider(FeignApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public String findSecret(String accountId) {
        return properties.getSecret();
    }
}