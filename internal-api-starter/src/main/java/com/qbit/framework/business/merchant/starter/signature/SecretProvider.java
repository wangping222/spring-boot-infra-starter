package com.qbit.framework.business.merchant.starter.signature;

public interface SecretProvider {
    String findSecret(String accountId);
}