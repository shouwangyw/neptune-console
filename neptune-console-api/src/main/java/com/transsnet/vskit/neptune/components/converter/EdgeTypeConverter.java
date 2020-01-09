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
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.transsnet.vskit.neptune.model.QueryResult.Type.EDGE;

/**
 * @author yangwei
 * @date 2020-01-09 09:27
 */
@Slf4j
@Component
public class EdgeTypeConverter implements TypeConverter<Result, Object> {
    @Resource
    private MyVertexDao myVertexDao;
    @Resource
    private MyEdgeDao myEdgeDao;

    @Override
    public boolean isType(QueryResult.Type type) {
        return EDGE == type;
    }

    @Override
    public void convert(List<Result> sources, List<Object> targets) {
        for (Result result : sources) {
            Edge edge = result.getEdge();
            targets.add(myEdgeDao.getMyEdge(edge));
        }
    }

    @Override
    public void convert2Graph(List<Object> sources, Graph graph) {
        List<MyEdge> myEdges = new ArrayList<>();

        List<String> vIds = new ArrayList<>();
        for (Object obj : sources) {
            val edge = (MyEdge) obj;
            myEdges.add(edge);
            if (!vIds.contains(edge.getInV())) {
                vIds.add(edge.getInV());
            }
            if (!vIds.contains(edge.getOutV())) {
                vIds.add(edge.getOutV());
            }
        }

        List<MyVertex> myVertices = new ArrayList<>(myVertexDao.getMyVertices(vIds));

        graph.setVertices(myVertices)
                .setEdges(myEdges);
    }

}
