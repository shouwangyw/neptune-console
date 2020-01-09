package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.components.converter.TypeConverter;
import com.transsnet.vskit.neptune.model.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.springframework.stereotype.Component;

/**
 * 对查询出来的原始数据集合处理
 *
 * @author yangwei
 * @date 2020-01-08 22:29
 */
@Slf4j
@Component
public class OriginalResultHandler extends AbstractResultHandler<Result, Object> {

    @SuppressWarnings("unchecked")
    @Override
    protected void handle() {
        for (TypeConverter typeConverter : TYPE_CONVERTERS) {
            if (typeConverter.isType(QueryResult.Type.valueOf(queryResult.getType()))) {
                typeConverter.convert(sources, targets);
            }
        }

        queryResult.setData(targets)
                .setShowNum(targets.size());
    }
}