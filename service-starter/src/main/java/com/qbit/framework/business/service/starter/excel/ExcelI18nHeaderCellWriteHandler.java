package com.qbit.framework.business.service.starter.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.qbit.framework.business.service.starter.i18n.I18nMessageUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.List;

/**
 * 对表头进行国际化处理
 *
 * @author litao
 * @date 2023/9/20 10:40
 */
@Component
public class ExcelI18nHeaderCellWriteHandler implements CellWriteHandler {
    /**
     * 国际化翻译
     */
    private final PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver;

    public ExcelI18nHeaderCellWriteHandler() {
        this.placeholderResolver = I18nMessageUtils::getMessage;
    }

    /**
     * 占位符处理
     */
    private final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("{", "}");

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                                 Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead
    ) {
        if (Boolean.TRUE.equals(isHead)) {
            List<String> originHeadNameList = head.getHeadNameList();
            if (CollectionUtils.isNotEmpty(originHeadNameList)) {
                // 国际化处理
                List<String> i18nHeadNames = originHeadNameList.stream()
                        .map(headName -> propertyPlaceholderHelper.replacePlaceholders(headName, placeholderResolver))
                        .toList();
                head.setHeadNameList(i18nHeadNames);
            }
        }
    }

}

