package com.transsnet.vskit.neptune.service;

import com.transsnet.vskit.neptune.components.MyEdgeDao;
import com.transsnet.vskit.neptune.components.MyVertexDao;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.transsnet.vskit.neptune.model.QueryResult.Type.*;

/**
 * @author yangwei
 * @date 2019-12-09 19:10
 */
@Slf4j
@Service
public class QueryService extends BaseDao {
    private static final String VERTEX_TYPE_PREFIX = "g.V";
    private static final String EDGE_TYPE_PREFIX = "g.E";
    private static final String PATH_TYPE_SUFFIX = ".path()";
    /**
     * 限制执行语句
     */
    private static final String LIMIT_CODES = "drop()|max()|min()";

    @Resource
    private StyleConfig styleConfig;
    @Resource
    private MyVertexDao myVertexDao;
    @Resource
    private MyEdgeDao myEdgeDao;

    public QueryResult executeQuery(String script) {
        QueryResult result = QueryResult.emptyResult();
        if (StringUtils.isBlank(script)) {
            return result;
        }

        String[] scripts = script.trim().split("\n");
        for (String code : scripts) {
            executeQuery(code, result);
        }

        return result;
    }

    private void executeQuery(String code, QueryResult result) {
        long startTime = System.currentTimeMillis();

        validate(code);

        List<Result> results = execute(code);
        // 解析类型
        QueryResult.Type type = parseType(code);
        // 解析结果
        List<Object> data = parseResult(results, type);
        // 解析图
        Graph graph = parseGraph(data, type);

        long endTime = System.currentTimeMillis();

        result.setId(UUID.randomUUID().toString())
                .setData(data)
                .setShowNum(data.size())
                .setType(type.toString())
                .setDuration((endTime - startTime) + " ms")
                .setGraph(graph)
                .setMessage("");
    }

    private void validate(String code) {
        String[] limitCodes = LIMIT_CODES.split("\\|");
        for (String limitCode : limitCodes) {
            if (code.contains(limitCode)) {
                log.error("该语句执行被限制");
            }
        }
    }

    private QueryResult.Type parseType(String code) {
        QueryResult.Type type = OTHER;
        if (code.indexOf(VERTEX_TYPE_PREFIX) == 0) {
            type = VERTEX;
        }
        if (code.indexOf(EDGE_TYPE_PREFIX) == 0) {
            type = EDGE;
        }
        if (code.lastIndexOf(PATH_TYPE_SUFFIX) == code.length() - 7) {
            type = PATH;
        }

        return type;
    }

    private List<Object> parseResult(List<Result> results, QueryResult.Type type) {
        if (ObjectUtils.isEmpty(results)) {
            return Collections.emptyList();
        }

        List<Object> data = new ArrayList<>();
        switch (type) {
            case VERTEX:
                parseMyVertexFromResult(results, data);
                break;
            case EDGE:
                parseMyEdgeFromResult(results, data);
                break;
            case PATH:
                parseMyPathFromResult(results, data);
                break;
            default:
                break;
        }

        return data;
    }

    private void parseMyVertexFromResult(List<Result> results, List<Object> data) {
        for (Result result : results) {
            data.add(getMyVertex(result.getVertex()));
        }
    }

    private void parseMyEdgeFromResult(List<Result> results, List<Object> data) {
        for (Result result : results) {
            data.add(getMyEdge(result.getEdge()));
        }
    }

    private void parseMyPathFromResult(List<Result> results, List<Object> data) {
        Map<String, MyVertex> vertexMap = getAllPathVertices(results);
        for (Result result : results) {
            data.add(getMyPath(result.getPath(), vertexMap));
        }
    }

    private Map<String, MyVertex> getAllPathVertices(List<Result> results) {
        List<String> vIds = new ArrayList<>();
        for (Result result : results) {
            Path path = result.getPath();
            List<Object> objects = path.objects();
            for (Object obj : objects) {
                String vId = ((Vertex) obj).id().toString();
                if (!vIds.contains(vId)) {
                    vIds.add(vId);
                }
            }
        }
        List<Vertex> vertices = myVertexDao.queryVertexByIds(vIds);
        Map<String, MyVertex> vertexMap = new HashMap<>(16);
        for (Vertex vertex : vertices) {
            vertexMap.put(vertex.id().toString(), getMyVertex(vertex));
        }
        return vertexMap;
    }

    private MyVertex getMyVertex(Vertex vertex) {
        return new MyVertex()
                .setId(vertex.id().toString())
                .setLabel(vertex.label())
                .setType(VERTEX.toString().toLowerCase())
                .setProperties(getProperties(vertex));
    }

    private MyEdge getMyEdge(Edge edge) {
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

    private MyPath getMyPath(Path path, Map<String, MyVertex> vertexMap) {
        List<Object> objects = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (Object obj : path.objects()) {
            MyVertex myVertex = vertexMap.get(((Vertex) obj).id().toString());
            objects.add(myVertex);
            labels.add(myVertex.getLabel());
        }

        return new MyPath()
                .setLabels(labels)
                .setObjects(objects);
    }

    private Map<String, String> getProperties(Vertex vertex) {
        Map<String, String> properties = new HashMap<>();
        vertex.keys().forEach(key -> {
            properties.put(key, vertex.property(key).value().toString());
        });
        return properties;
    }

    private Map<String, String> getProperties(Edge edge) {
        Map<String, String> properties = new HashMap<>();
        edge.keys().forEach(key -> {
            properties.put(key, edge.property(key).value().toString());
        });
        return properties;
    }

    private Graph parseGraph(List<Object> data, QueryResult.Type type) {
        if (ObjectUtils.isEmpty(data)) {
            return new Graph();
        }
        List<MyVertex> myVertices = new ArrayList<>();
        List<MyEdge> myEdges = new ArrayList<>();

        switch (type) {
            case VERTEX:
                parseMyVertexFromData(data, myVertices, myEdges);
                break;
            case EDGE:
                parseMyEdgeFromData(data, myVertices, myEdges);
                break;
            case PATH:
                parseMyPathFromData(data, myVertices, myEdges);
                break;
            default:
                break;
        }

        val styles = new Styles()
                .setGroups(getGroups(myVertices))
                .setFont(styleConfig.getFont())
                .setEdgeColor(styleConfig.getEdgeColor())
                .setEdgeFont(styleConfig.getEdgeFont());

        return new Graph().setVertices(myVertices)
                .setEdges(myEdges)
                .setStyles(styles);
    }

    private void parseMyVertexFromData(List<Object> data, List<MyVertex> myVertices, List<MyEdge> myEdges) {
        List<String> vIds = new ArrayList<>();
        for (Object obj : data) {
            val myVertex = (MyVertex) obj;
            if (!vIds.contains(myVertex.getId())) {
                myVertices.add(myVertex);
                vIds.add(myVertex.getId());
            }
        }
        List<String> eIds = getEdgeIds(vIds);

        getMyEdgeByIds(eIds, myEdges);
    }

    private void parseMyEdgeFromData(List<Object> data, List<MyVertex> myVertices, List<MyEdge> myEdges) {
        List<String> vIds = new ArrayList<>();
        for (Object obj : data) {
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

    private void parseMyPathFromData(List<Object> data, List<MyVertex> myVertices, List<MyEdge> myEdges) {
        // 用于去重
        List<String> vIds = new ArrayList<>();
        List<String> eIds = new ArrayList<>();
        for (Object obj : data) {
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
        if (ObjectUtils.isEmpty(objects) || objects.size() <= 1) {
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
        if (ObjectUtils.isEmpty(ids)) {
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
        List<Edge> edges = myEdgeDao.queryEdgeByIds(ids);
        if (ObjectUtils.isEmpty(edges)) {
            return;
        }
        for (Edge edge : edges) {
            myEdges.add(getMyEdge(edge));
        }
    }

    private void getMyVertexByIds(List<String> ids, List<MyVertex> myVertices) {
        List<Vertex> vertices = myVertexDao.queryVertexByIds(ids);
        if (ObjectUtils.isEmpty(vertices)) {
            return;
        }
        for (Vertex vertex : vertices) {
            myVertices.add(getMyVertex(vertex));
        }
    }

    private Map<String, Group> getGroups(List<MyVertex> vertices) {
        if (ObjectUtils.isEmpty(vertices)) {
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
