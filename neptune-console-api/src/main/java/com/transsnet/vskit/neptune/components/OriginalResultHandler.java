package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.dao.BaseDao;
import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.model.graph.MyVertex;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对查询出来的原始数据集合处理
 *
 * @author yangwei
 * @date 2020-01-08 22:29
 */
@Slf4j
@Component
public class OriginalResultHandler extends AbstractResultHandler<Result, Object> {
    @Resource
    private BaseDao baseDao;

    @Override
    protected void handle() {
        switch (QueryResult.Type.valueOf(queryResult.getType())) {
            case VERTEX:
                parseMyVertex();
                break;
            case EDGE:
                parseMyEdge();
                break;
            case PATH:
                parseMyPath();
                break;
            default:
                break;
        }
        queryResult.setData(targets)
                .setShowNum(targets.size());
    }

    private void parseMyVertex() {
        for (Result result : sources) {
            targets.add(getMyVertex(result.getVertex()));
        }
    }

    private void parseMyEdge() {
        for (Result result : sources) {
            targets.add(getMyEdge(result.getEdge()));
        }
    }

    private void parseMyPath() {
        Map<String, MyVertex> vertexMap = getAllPathVertices(sources);
        for (Result result : sources) {
            targets.add(getMyPath(result.getPath(), vertexMap));
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
        List<Vertex> vertices = baseDao.queryByIds(TYPE_VERTEX, vIds);
        Map<String, MyVertex> vertexMap = new HashMap<>(16);
        for (Vertex vertex : vertices) {
            vertexMap.put(vertex.id().toString(), getMyVertex(vertex));
        }
        return vertexMap;
    }


}