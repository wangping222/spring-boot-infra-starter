package com.qbit.framework.core.api.model.toolkits.exception.code;

import java.util.List;

/**
 * @author Qbit Framework
 */
public interface BusinessCodeService {

    List<BusinessCodeDTO> list(String code);
}
