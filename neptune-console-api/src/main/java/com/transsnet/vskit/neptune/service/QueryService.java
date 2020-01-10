package com.transsnet.vskit.neptune.service;

import com.transsnet.vskit.neptune.components.GraphResultHandler;
import com.transsnet.vskit.neptune.components.OriginalResultHandler;
import com.transsnet.vskit.neptune.components.parser.TokenParser;
import com.transsnet.vskit.neptune.dao.DefaultDaoHandler;
import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.model.graph.Graph;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

/**
 * @author yangwei
 * @date 2019-12-09 19:10
 */
@Slf4j
@Service
public class QueryService extends DefaultDaoHandler {
    @Resource
    private OriginalResultHandler originalResultHandler;
    @Resource
    private GraphResultHandler graphResultHandler;

    public QueryResult executeQuery(String script) {
        long startTime = System.currentTimeMillis();

        QueryResult queryResult = QueryResult.emptyResult();
        if (StringUtils.isBlank(script)) {
            return queryResult;
        }

        String[] scripts = script.trim().split("\n");

        for (String code : scripts) {
            executeQuery(code, queryResult);
        }

        long endTime = System.currentTimeMillis();

        return queryResult.setDuration((endTime - startTime) + " ms")
                .setId(UUID.randomUUID().toString())
                .setMessage("");
    }

    private void executeQuery(String code, QueryResult queryResult) {

        // 解析类型
        QueryResult.Type resultType = TokenParser.parseType(code);
        queryResult.setType(resultType.toString());

        List<Result> results = execute(code);
        if (isEmpty(results)) {
            return;
        }
        // 1、对查询原始结果处理
        originalResultHandler.handle(queryResult, results, new ArrayList<>());
        List<Object> data = queryResult.getData();
        if (isNotEmpty(data)) {
            // 2、对结果数据进行进一步处理，解析为Graph对象信息
            graphResultHandler.handle(queryResult, data, null);
        }
    }

}
