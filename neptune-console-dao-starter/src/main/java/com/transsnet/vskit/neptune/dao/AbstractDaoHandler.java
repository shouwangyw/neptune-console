package com.transsnet.vskit.neptune.dao;

import com.transsnet.vskit.neptune.config.AwsNeptuneAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedEdge;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedVertex;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;

/**
 * 抽象的图数据库操作处理器
 * 可以封装一些公共的属性、接口适配等
 *
 * @author yangwei
 * @date 2020-01-10 16:07
 */
@Slf4j
public abstract class AbstractDaoHandler implements DaoHandler {
    /**
     * 最大超时时间，1小时
     */
    protected static final int MAX_TIMEOUT = 60 * 60 * 1000;
    /**
     * 定义将请求结果“批处理”回客户端的大小。换句话说，如果设置为1，那么包含10个项的结果将使每个结果分别返回。
     * 如果设置为2，同样的10个结果将在5个批次中返回，每个批次2个。
     * 最大批量大小，默认是 64
     * 2的64次方 = 65536
     */
    protected static final int MAX_BATCH_SIZE = 64;
    /**
     * 重试时间间隔：1秒
     */
    protected static final int RETRY_TIME_INTERVAL = 1000;
    /**
     * 最大重试次数
     */
    protected static final int MAX_RETRY_COUNT = 20;
    /**
     * 最大错误日志打印长度
     */
    protected static final int MAX_PRINT_LENGTH = 500;
    /**
     * 根据时间戳属性排序分页查询【升序】时，最大的时间区间，用于过滤掉一大部分数据，提升效率
     */
    protected static final int MAX_TIME_STEP = 100000000;
    /**
     * 慢执行时间：1分钟
     */
    protected static final int SLOW_EXECUTE_TIME = 60 * 1000;
    /**
     * 执行失败次数
     */
    protected static AtomicInteger failureCount = new AtomicInteger(0);
    /**
     * 批量添加插入数量
     */
    protected static final int MAX_BATCH_INSERT_NUM = 200;
    /**
     * 顶点
     */
    private static final String TYPE_VERTEX = "V";
    /**
     * 边
     */
    private static final String TYPE_EDGE = "E";

    protected AwsNeptuneAutoConfiguration configuration;
    protected GraphTraversalSource g;
    protected Client client;

    @Override
    public List<Vertex> queryVertexByIds(List<String> ids) {
        return queryByIds(TYPE_VERTEX, ids);
    }

    @Override
    public List<Edge> queryEdgeByIds(List<String> ids) {
        return queryByIds(TYPE_EDGE, ids);
    }

    /**
     * 根据id查询[批量]
     *
     * @param type
     * @param ids
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> queryByIds(String type, List<String> ids) {
        String script = buildQueryScriptByIds(type, ids);

        List<Result> results = execute(script);
        if (ObjectUtils.isEmpty(results)) {
            return emptyList();
        }
        List<T> objects = new ArrayList<>();
        for (Result result : results) {
            Object object = result.getObject();
            if (object instanceof DetachedVertex) {
                objects.add((T) result.getVertex());
            }
            if (object instanceof DetachedEdge) {
                objects.add((T) result.getEdge());
            }
        }

        return objects;
    }

    /**
     * 构建根据id查询的脚本
     *
     * @param type
     * @param ids
     * @return
     */
    protected String buildQueryScriptByIds(String type, List<String> ids) {
        if (ObjectUtils.isEmpty(ids)) {
            return "";
        }
        int length = ids.size();
        if (length == 1) {
            return String.format("g.%s().hasId('%s')", type, ids.get(0));
        }
        StringBuilder stringBuilder = new StringBuilder(
                String.format("g.%s().hasId('%s'", type, ids.get(0))
        );
        for (int i = 1; i < length; i++) {
            stringBuilder.append(String.format(", '%s'", ids.get(i)));
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    /**
     * 打印执行脚本语句日志
     *
     * @param script
     * @return
     */
    protected String printScriptLog(String script) {
        if (script.length() > MAX_PRINT_LENGTH) {
            return script.substring(0, MAX_PRINT_LENGTH / 2) + " ... " + script.substring(script.length() - MAX_PRINT_LENGTH / 2);
        } else {
            return script;
        }
    }
}
