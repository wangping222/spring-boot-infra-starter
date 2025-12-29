package com.qbit.framework.core.toolkits.exception.code;


import com.qbit.framework.core.toolkits.enums.DescriptiveEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Qbit Framework
 */
public interface ExceptionCode extends DescriptiveEnum, Serializable {

    String SUCCESSFUL_CODE = "0";

    ExceptionCode SUCCESSFUL = new ExceptionCode() {
        @Serial
        private static final long serialVersionUID = 5034455936657195532L;

        @Override
        public String getCode() {
            return SUCCESSFUL_CODE;
        }

        @Override
        public String getDesc() {
            return "";
        }
    };

    String getCode();
}
