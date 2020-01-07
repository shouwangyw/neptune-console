package com.transsnet.vskit.neptune.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangwei
 * @date 2019-12-31 17:28
 */
@Slf4j
@Configuration
@ConditionalOnClass
@EnableConfigurationProperties(AwsNeptuneProperties.class)
public class AwsNeptuneAutoConfiguration implements AutoCloseable {
    private static final String DEFAULT_REMOTE_SOURCE_NAME = "g";

    private Cluster cluster;
    private Client client;
    private GraphTraversalSource g;

    @Autowired
    private AwsNeptuneProperties properties;

    /**
     * 获取远程连接操作对象 g
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "aws.neptune.enabled", havingValue = "true")
    public GraphTraversalSource getGraphTraversalSource() {
        if (g != null) {
            return g;
        }
        log.info("==>> getGraphTraversalSource ...");
        Cluster.Builder builder = Cluster.build();
        builder.addContactPoint(properties.getClusterNode());
        builder.port(properties.getClusterPort());
        cluster = builder.create();

        g = AnonymousTraversalSource.traversal()
                .withRemote(DriverRemoteConnection.using(cluster, DEFAULT_REMOTE_SOURCE_NAME));
        return g;
    }

    /**
     * 获取客户端操作对象
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "aws.neptune.enabled", havingValue = "true")
    public Client getClient() {
        if (client != null && !client.isClosing()) {
            return client;
        }
        log.info("===>> getClient ...");
        Cluster.Builder builder = Cluster.build();
        builder.addContactPoint(properties.getClusterNode());
        builder.port(properties.getClusterPort());
        cluster = builder.create();
        client = cluster.connect();
        return client;
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
