package com.transsnet.vskit.neptune.dao;

import com.transsnet.vskit.neptune.config.AwsNeptuneAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.RequestOptions;
import org.apache.tinkerpop.gremlin.driver.Result;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * 默认的图数据库操作处理器
 *
 * @author yangwei
 * @date 2020-01-10 16:01
 */
@Slf4j
public class DefaultDaoHandler extends AbstractDaoHandler {
    private static final String ALREADY_EXISTS = "already exists";

    public DefaultDaoHandler() {
    }

    public DefaultDaoHandler(AwsNeptuneAutoConfiguration neptuneAutoConfiguration) {
        configuration = neptuneAutoConfiguration;
        g = configuration.getGraphTraversalSource();
        client = configuration.getClient();
    }

    @Override
    public List<Result> execute(String script) {
        if (isEmpty(script) || configuration == null) {
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
            if (e.getMessage().contains(ALREADY_EXISTS)) {
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

    private static void init() {
        g = configuration.getGraphTraversalSource();
        client = configuration.getClient();
    }
}
