package com.transsnet.vskit.neptune.dao;

import com.transsnet.vskit.neptune.config.AwsNeptuneAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.RequestOptions;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedEdge;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Neptune操作封装
 *
 * @author yangwei
 * @date 2019-06-17 17:33
 */
@Slf4j
@ConditionalOnProperty(name = "aws.neptune.enabled")
@Component
public class BaseDao {
    /**
     * 最大超时时间，1小时
     */
    private static final int MAX_TIMEOUT = 60 * 60 * 1000;
    /**
     * 定义将请求结果“批处理”回客户端的大小。换句话说，如果设置为1，那么包含10个项的结果将使每个结果分别返回。
     * 如果设置为2，同样的10个结果将在5个批次中返回，每个批次2个。
     * 最大批量大小，默认是 64
     * 2的64次方 = 65536
     */
    private static final int MAX_BATCH_SIZE = 64;
    /**
     * 重试时间间隔：1秒
     */
    private static final int RETRY_TIME_INTERVAL = 1000;
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 20;
    /**
     * 最大错误日志打印长度
     */
    private static final int MAX_PRINT_LENGTH = 500;
    /**
     * 根据时间戳属性排序分页查询【升序】时，最大的时间区间，用于过滤掉一大部分数据，提升效率
     */
    private static final int MAX_TIME_STEP = 100000000;
    /**
     * 慢执行时间：1分钟
     */
    private static final int SLOW_EXECUTE_TIME = 60 * 1000;
    /**
     * 执行失败次数
     */
    private static AtomicInteger failureCount = new AtomicInteger(0);
    /**
     * 批量添加插入数量
     */
    protected static final int MAX_BATCH_INSERT_NUM = 200;

    @Autowired
    private AwsNeptuneAutoConfiguration configuration;
    @Autowired
    private GraphTraversalSource g;
    @Autowired
    private Client client;

    /**
     * 根据id查询
     *
     * @param type
     * @param id
     * @return
     */
    protected <T> T queryById(String type, String id) {
        if (isEmpty(id)) {
            return null;
        }

        List<String> ids = new ArrayList<>();
        ids.add(id);

        List<Object> objects = queryByIds(type, ids);
        if (isNotEmpty(objects)) {
            return (T) objects.get(0);
        }
        return null;
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
    private String buildQueryScriptByIds(String type, List<String> ids) {
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
     * 执行gremlin脚本
     *
     * @param script
     * @return
     */
    public List<Result> execute(String script) {
        if (isEmpty(script)) {
            return emptyList();
        }
        RequestOptions options = RequestOptions.build()
                .timeout(MAX_TIMEOUT)
//                .batchSize(MAX_BATCH_SIZE)   // 设置默认就很好了
                .create();
        try {
            if (client != null && !client.isClosing()) {
                long startTime = System.currentTimeMillis();
                List<Result> results = client.submit(script, options).all().get();
                log.debug("<<== results.size() = {} ", results.size());
                long time = System.currentTimeMillis() - startTime;
                if (time > SLOW_EXECUTE_TIME) {
                    log.info("==>> execute 执行脚本: {} 耗时: {} ms", printScriptLog(script), time);
                }
                failureCount.set(0);
                return results;
            } else {
                log.info("==>> 执行脚本: {} 时 Neptune 客户端正在断开连接 ... ", printScriptLog(script));
                retryExecute(script);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                log.info("==>> 重复插入：{}", printScriptLog(script));
            } else {
                log.error("==>> 执行脚本[{}]时出错：{}:{}", printScriptLog(script), e.getMessage(), e.getStackTrace());
            }
        }
        return emptyList();
    }

    /**
     * 重试
     *
     * @param script
     */
    private void retryExecute(String script) {
        log.info("==>> 正在重试 ... ");
        init();
        try {
            TimeUnit.SECONDS.sleep(RETRY_TIME_INTERVAL);
            if (failureCount.get() < MAX_RETRY_COUNT) {
                // 重试
                failureCount.getAndIncrement();
                log.info("==>> 失败重试执行第[{}]次", failureCount.get());
                execute(script);
            }
        } catch (Exception e) {
            log.info("==>> retryExecute() error {}:{}", e.getMessage(), e.getStackTrace());
        }
    }

    private void init() {
        g = configuration.getGraphTraversalSource();
        client = configuration.getClient();
    }

    /**
     * 打印执行脚本语句日志
     *
     * @param script
     * @return
     */
    private String printScriptLog(String script) {
        if (script.length() > MAX_PRINT_LENGTH) {
            return script.substring(0, MAX_PRINT_LENGTH / 2) + " ... " + script.substring(script.length() - MAX_PRINT_LENGTH / 2);
        } else {
            return script;
        }
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (g != null) {
            g.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
