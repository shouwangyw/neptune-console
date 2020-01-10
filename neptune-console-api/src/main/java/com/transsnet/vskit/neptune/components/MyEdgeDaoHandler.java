package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.dao.DefaultDaoHandler;
import com.transsnet.vskit.neptune.model.graph.MyEdge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.tinkerpop.gremlin.structure.Edge;

import java.util.*;

import static com.transsnet.vskit.neptune.model.QueryResult.Type.EDGE;

/**
 * @author yangwei
 * @date 2020-01-09 11:37
 */
@Slf4j
public class MyEdgeDaoHandler extends DefaultDaoHandler {

    public MyEdge getMyEdge(Edge edge) {
        return new MyEdge()
                .setId(edge.id().toString())
                .setLabel(edge.label())
                .setType(EDGE.toString().toLowerCase())
                .setProperties(getProperties(edge))
                .setInV(edge.inVertex().id().toString())
                .setInVLabel(edge.inVertex().label())
                .setOutV(edge.outVertex().id().toString())
                .setOutVLabel(edge.outVertex().label());
    }

    private Map<String, String> getProperties(Edge edge) {
        Map<String, String> properties = new HashMap<>(4);
        edge.keys().forEach(key -> properties.put(key, edge.property(key).value().toString()));

        return properties;
    }

    public List<MyEdge> getMyEdges(List<String> edgeIds) {
        List<Edge> edges = queryEdgeByIds(edgeIds);
        if (ObjectUtils.isEmpty(edges)) {
            return Collections.emptyList();
        }

        List<MyEdge> myEdges = new ArrayList<>();
        for (Edge edge : edges) {
            myEdges.add(getMyEdge(edge));
        }

        return myEdges;
    }
}
