package com.transsnet.vskit.neptune.config;

import com.transsnet.vskit.neptune.dao.DaoHandler;
import com.transsnet.vskit.neptune.dao.DefaultDaoHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author yangwei
 * @date 2019-12-31 17:28
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "aws.neptune.enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(AwsNeptuneProperties.class)
public class AwsNeptuneAutoConfiguration implements AutoCloseable {
    private static final String DEFAULT_REMOTE_SOURCE_NAME = "g";

    private Cluster cluster;
    private Client client;
    private GraphTraversalSource g;

    @Autowired
    private AwsNeptuneProperties properties;

    @PostConstruct
    public void init() {
        initGraphTraversalSource();
        initClient();
    }

    private void initGraphTraversalSource() {
        if (properties == null) {
            return;
        }
        log.info("==>> initGraphTraversalSource start ...");
        Cluster.Builder builder = Cluster.build();
        builder.addContactPoint(properties.getClusterNode());
        builder.port(properties.getClusterPort());
        cluster = builder.create();

        g = AnonymousTraversalSource.traversal()
                .withRemote(DriverRemoteConnection.using(cluster, DEFAULT_REMOTE_SOURCE_NAME));
        log.info("==>> initGraphTraversalSource end !!!");
    }

    private void initClient() {
        if (properties == null) {
            return;
        }
        log.info("===>> initClient start ...");
        Cluster.Builder builder = Cluster.build();
        builder.addContactPoint(properties.getClusterNode());
        builder.port(properties.getClusterPort());
        cluster = builder.create();
        client = cluster.connect();
        log.info("===>> initClient end !!!");
    }

    /**
     * 初始化创建远程连接操作对象 g
     *
     * @return
     */
    @Bean
    public GraphTraversalSource getGraphTraversalSource() {
        if (g != null) {
            return g;
        }
        initGraphTraversalSource();
        return g;
    }

    /**
     * 初始化创建客户端操作对象
     *
     * @return
     */
    @Bean
    public Client getClient() {
        if (client != null && !client.isClosing()) {
            return client;
        }
        initClient();
        return client;
    }

    /**
     * 初始化创建图数据库操作对象
     *
     * @return
     */
    @Bean
    public DaoHandler initBaseDao() {
        return new DefaultDaoHandler(this);
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        if (g != null) {
            g.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
