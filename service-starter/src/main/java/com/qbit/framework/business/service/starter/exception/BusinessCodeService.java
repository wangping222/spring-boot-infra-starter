package com.qbit.framework.business.service.starter.exception;



import java.util.List;

/**
 * 业务状态码
 */
public interface BusinessCodeService {

    List<BusinessCodeDTO> list(String code);
}
