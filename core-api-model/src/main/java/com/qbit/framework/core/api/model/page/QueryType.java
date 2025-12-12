package com.qbit.framework.core.api.model.page;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Qbit Framework
 */
@AllArgsConstructor
@Getter
public enum QueryType {

    COUNT_TOTAL("统计总数"),
    QUERY_RECORDS("查询结果集"),
    QUERY_BOTH("查询总数和结果集");

    private final String desc;
}