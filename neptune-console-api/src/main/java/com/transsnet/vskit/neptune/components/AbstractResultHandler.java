package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.model.graph.MyEdge;
import com.transsnet.vskit.neptune.model.graph.MyPath;
import com.transsnet.vskit.neptune.model.graph.MyVertex;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.transsnet.vskit.neptune.model.QueryResult.Type.EDGE;
import static com.transsnet.vskit.neptune.model.QueryResult.Type.VERTEX;

/**
 * 抽象的结果集处理器
 *
 * @param <S> 需要处理的对象
 * @param <T> 处理之后的对象
 * @author yangwei
 * @date 2020-01-08 22:26
 */
public abstract class AbstractResultHandler<S, T> {
    protected static final String TYPE_VERTEX = "V";
    protected static final String TYPE_EDGE = "E";

    protected QueryResult queryResult;
    protected List<S> sources;
    protected List<T> targets;

    /**
     * 处理方法
     *
     * @param sources 处理前的对象集合
     * @param targets 处理后的对象集合
     */
    public void handle(QueryResult queryResult, List<S> sources, List<T> targets) {
        if (queryResult == null || ObjectUtils.isEmpty(sources)) {
            return;
        }

        this.queryResult = queryResult;
        this.sources = sources;
        this.targets = targets;

        handle();
    }

    /**
     * 子类去实现处理细节
     */
    protected abstract void handle();

    protected MyVertex getMyVertex(Vertex vertex) {
        return new MyVertex()
                .setId(vertex.id().toString())
                .setLabel(vertex.label())
                .setType(VERTEX.toString().toLowerCase())
                .setProperties(getProperties(vertex));
    }

    protected MyEdge getMyEdge(Edge edge) {
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

    protected MyPath getMyPath(Path path, Map<String, MyVertex> vertexMap) {
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
}
