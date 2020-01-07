package com.transsnet.vskit.neptune.model.graph;

import com.transsnet.vskit.neptune.model.styles.Styles;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author yangwei
 * @date 2019-12-09 17:54
 */
@Data
@Accessors(chain = true)
public class Graph {
    private List<MyVertex> vertices;
    private List<MyEdge> edges;
    private Styles styles;
}
