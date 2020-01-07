package com.transsnet.vskit.neptune.model.graph;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author yangwei
 * @date 2019-12-09 19:35
 */
@Data
@Accessors(chain = true)
public class MyEdge {
    private String id;
    private String label;
    private String type;
    private Map<String, String> properties;
    private String inV;
    private String outV;
    private String inVLabel;
    private String outVLabel;
}
