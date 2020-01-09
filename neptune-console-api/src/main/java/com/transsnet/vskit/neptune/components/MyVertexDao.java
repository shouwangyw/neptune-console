package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.dao.BaseDao;
import com.transsnet.vskit.neptune.model.graph.MyVertex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

import static com.transsnet.vskit.neptune.model.QueryResult.Type.VERTEX;

/**
 * @author yangwei
 * @date 2020-01-09 11:35
 */
@Slf4j
@Component
public class MyVertexDao {
    private static final String TYPE_VERTEX = "V";

    @Resource
    private BaseDao baseDao;

    public MyVertex getMyVertex(Vertex vertex) {
        return new MyVertex()
                .setId(vertex.id().toString())
                .setLabel(vertex.label())
                .setType(VERTEX.toString().toLowerCase())
                .setProperties(getProperties(vertex));
    }

    private Map<String, String> getProperties(Vertex vertex) {
        Map<String, String> properties = new HashMap<>(4);
        vertex.keys().forEach(key -> {
            properties.put(key, vertex.property(key).value().toString());
        });

        return properties;
    }

    public List<MyVertex> getMyVertices(List<String> vertexIds) {
        List<Vertex> vertices = baseDao.queryByIds(TYPE_VERTEX, vertexIds);
        if (ObjectUtils.isEmpty(vertices)) {
            return Collections.emptyList();
        }

        List<MyVertex> myVertices = new ArrayList<>();
        for (Vertex vertex : vertices) {
            myVertices.add(getMyVertex(vertex));
        }

        return myVertices;
    }
}
