package com.transsnet.vskit.neptune.components.converter;

import com.transsnet.vskit.neptune.components.MyEdgeDaoHandler;
import com.transsnet.vskit.neptune.components.MyVertexDaoHandler;
import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.model.graph.Graph;
import com.transsnet.vskit.neptune.model.graph.MyEdge;
import com.transsnet.vskit.neptune.model.graph.MyPath;
import com.transsnet.vskit.neptune.model.graph.MyVertex;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.transsnet.vskit.neptune.model.QueryResult.Type.PATH;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

/**
 * @author yangwei
 * @date 2020-01-09 10:45
 */
@Slf4j
public class PathTypeConverter implements TypeConverter<Result, Object> {
    @Override
    public boolean isType(QueryResult.Type type) {
        return PATH == type;
    }

    @Override
    public void convert(List<Result> sources, List<Object> targets) {
        Map<String, MyVertex> vertexMap = getAllPathVertices(sources);

        for (Result result : sources) {
            Path path = result.getPath();
            targets.add(getMyPath(path, vertexMap));
        }
    }

    @Override
    public void convert2Graph(List<Object> sources, Graph graph) {
        List<MyVertex> myVertices = new ArrayList<>();

        // 用于去重
        List<String> vIds = new ArrayList<>();
        List<String> eIds = new ArrayList<>();
        for (Object obj : sources) {
            val myPath = (MyPath) obj;
            List<Object> objects = myPath.getObjects();

            getVerticesForPath(objects, myVertices, vIds);

            getEdgeIdsForPath(objects, eIds);
        }

        List<MyEdge> myEdges = new ArrayList<>(new MyEdgeDaoHandler().getMyEdges(eIds));

        graph.setVertices(myVertices)
                .setEdges(myEdges);
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

        List<MyVertex> myVertices = new MyVertexDaoHandler().getMyVertices(vIds);

        Map<String, MyVertex> vertexMap = new HashMap<>(16);
        for (MyVertex myVertex : myVertices) {
            vertexMap.put(myVertex.getId(), myVertex);
        }
        return vertexMap;
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
}
