package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.config.StyleConfig;
import com.transsnet.vskit.neptune.dao.BaseDao;
import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.model.graph.Graph;
import com.transsnet.vskit.neptune.model.graph.MyEdge;
import com.transsnet.vskit.neptune.model.graph.MyPath;
import com.transsnet.vskit.neptune.model.graph.MyVertex;
import com.transsnet.vskit.neptune.model.styles.Group;
import com.transsnet.vskit.neptune.model.styles.Styles;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

/**
 * @author yangwei
 * @date 2020-01-08 19:23
 */
@Slf4j
@Component
public class ParsedResultHandler extends AbstractResultHandler<Object, Object> {
    @Resource
    private StyleConfig styleConfig;
    @Resource
    private BaseDao baseDao;

    @Override
    protected void handle() {
        List<MyVertex> myVertices = new ArrayList<>();
        List<MyEdge> myEdges = new ArrayList<>();

        switch (QueryResult.Type.valueOf(queryResult.getType())) {
            case VERTEX:
                parseMyVertex(myVertices, myEdges);
                break;
            case EDGE:
                parseMyEdge(myVertices, myEdges);
                break;
            case PATH:
                parseMyPath(myVertices, myEdges);
                break;
            default:
                break;
        }

        val styles = new Styles()
                .setGroups(getGroups(myVertices))
                .setFont(styleConfig.getFont())
                .setEdgeColor(styleConfig.getEdgeColor())
                .setEdgeFont(styleConfig.getEdgeFont());

        val graph = new Graph().setVertices(myVertices)
                .setEdges(myEdges)
                .setStyles(styles);

        queryResult.setGraph(graph);
    }

    private void parseMyVertex(List<MyVertex> myVertices, List<MyEdge> myEdges) {
        List<String> vIds = new ArrayList<>();
        for (Object obj : sources) {
            val myVertex = (MyVertex) obj;
            if (!vIds.contains(myVertex.getId())) {
                myVertices.add(myVertex);
                vIds.add(myVertex.getId());
            }
        }
        List<String> eIds = getEdgeIds(vIds);

        getMyEdgeByIds(eIds, myEdges);
    }

    private void parseMyEdge(List<MyVertex> myVertices, List<MyEdge> myEdges) {
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

        getMyVertexByIds(vIds, myVertices);
    }

    private void parseMyPath(List<MyVertex> myVertices, List<MyEdge> myEdges) {
        // 用于去重
        List<String> vIds = new ArrayList<>();
        List<String> eIds = new ArrayList<>();
        for (Object obj : sources) {
            val myPath = (MyPath) obj;
            List<Object> objects = myPath.getObjects();

            getVerticesForPath(objects, myVertices, vIds);

            getEdgeIdsForPath(objects, eIds);
        }

        getMyEdgeByIds(eIds, myEdges);
    }

    private void getVerticesForPath(List<Object> objects, List<MyVertex> myVertices, List<String> vIds) {
        for (Object obj : objects) {
            MyVertex myVertex = (MyVertex) obj;
            if (!vIds.contains(myVertex.getId())) {
                vIds.add(myVertex.getId());
                myVertices.add(myVertex);
            }
        }
    }

    private void getEdgeIdsForPath(List<Object> objects, List<String> eIds) {
        if (isEmpty(objects) || objects.size() <= 1) {
            return;
        }

        for (int i = 0; i < objects.size() - 1; i++) {
            val fromV = (MyVertex) objects.get(i);
            val toV = (MyVertex) objects.get(i + 1);
            String eId = fromV.getId() + toV.getId();
            if (!eIds.contains(eId)) {
                eIds.add(eId);
            }
        }
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

    private void getMyEdgeByIds(List<String> ids, List<MyEdge> myEdges) {
        List<Edge> edges = baseDao.queryByIds(TYPE_EDGE, ids);
        if (isEmpty(edges)) {
            return;
        }
        for (Edge edge : edges) {
            myEdges.add(getMyEdge(edge));
        }
    }

    private void getMyVertexByIds(List<String> ids, List<MyVertex> myVertices) {
        List<Vertex> vertices = baseDao.queryByIds(TYPE_VERTEX, ids);
        if (isEmpty(vertices)) {
            return;
        }
        for (Vertex vertex : vertices) {
            myVertices.add(getMyVertex(vertex));
        }
    }

    private Map<String, Group> getGroups(List<MyVertex> vertices) {
        if (isEmpty(vertices)) {
            return Collections.emptyMap();
        }
        Map<String, Long> map = vertices.stream().collect(Collectors.groupingBy(MyVertex::getLabel, Collectors.counting()));

        AtomicInteger count = new AtomicInteger();
        Map<String, Group> groups = new HashMap<>();
        map.keySet().forEach(label -> {
            groups.put(label, styleConfig.getGroups().get(count.get() % 2));
            count.getAndIncrement();
        });

        return groups;
    }
}
