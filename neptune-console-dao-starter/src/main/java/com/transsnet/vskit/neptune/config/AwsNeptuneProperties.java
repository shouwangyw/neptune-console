package com.transsnet.vskit.neptune.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yangwei
 * @date 2019-12-31 17:31
 */
@Data
@ConfigurationProperties("aws.neptune")
public class AwsNeptuneProperties {
    /**
     * 是否开启
     */
    private boolean enabled;
    /**
     * neptune集群节点
     */
    private String clusterNode;
    /**
     * neptune集群端口
     */
    private int clusterPort;
}
