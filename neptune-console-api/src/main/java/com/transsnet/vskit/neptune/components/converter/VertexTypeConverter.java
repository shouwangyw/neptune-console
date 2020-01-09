package com.transsnet.vskit.neptune.components.converter;

import com.transsnet.vskit.neptune.components.MyEdgeDao;
import com.transsnet.vskit.neptune.components.MyVertexDao;
import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.model.graph.Graph;
import com.transsnet.vskit.neptune.model.graph.MyEdge;
import com.transsnet.vskit.neptune.model.graph.MyVertex;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.transsnet.vskit.neptune.model.QueryResult.Type.VERTEX;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

/**
 * @author yangwei
 * @date 2020-01-09 09:27
 */
@Slf4j
@Component
public class VertexTypeConverter implements TypeConverter<Result, Object> {
    @Resource
    private MyVertexDao myVertexDao;
    @Resource
    private MyEdgeDao myEdgeDao;

    @Override
    public boolean isType(QueryResult.Type type) {
        return VERTEX == type;
    }

    @Override
    public void convert(List<Result> sources, List<Object> targets) {
        for (Result result : sources) {
            Vertex vertex = result.getVertex();
            targets.add(myVertexDao.getMyVertex(vertex));
        }
    }

    @Override
    public void convert2Graph(List<Object> sources, Graph graph) {
        List<MyVertex> myVertices = new ArrayList<>();
        List<MyEdge> myEdges = new ArrayList<>();

        List<String> vIds = new ArrayList<>();
        for (Object obj : sources) {
            val myVertex = (MyVertex) obj;
            if (!vIds.contains(myVertex.getId())) {
                myVertices.add(myVertex);
                vIds.add(myVertex.getId());
            }
        }
        List<String> eIds = getEdgeIds(vIds);

        myEdges.addAll(myEdgeDao.getMyEdges(eIds));

        graph.setVertices(myVertices)
                .setEdges(myEdges);
    }

    private List<String> getEdgeIds(List<String> ids) {
        if (isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<String> eIds = new ArrayList<>();
        for (String id1 : ids) {
            for (String id2 : ids) {
                if (id1.equals(id2)) {
                    if (!eIds.contains(id1 + id2)) {
                        eIds.add(id1 + id2);
                    }
                    if (!eIds.contains(id2 + id1)) {
                        eIds.add(id2 + id1);
                    }
                }
            }
        }
        return eIds;
    }
}
