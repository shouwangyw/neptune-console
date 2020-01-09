package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.components.converter.TypeConverter;
import com.transsnet.vskit.neptune.config.StyleConfig;
import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.model.graph.Graph;
import com.transsnet.vskit.neptune.model.graph.MyVertex;
import com.transsnet.vskit.neptune.model.styles.Group;
import com.transsnet.vskit.neptune.model.styles.Styles;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

/**
 * @author yangwei
 * @date 2020-01-09 11:55
 */
@Slf4j
@Component
public class GraphResultHandler extends AbstractResultHandler<Object, Object> {
    @Resource
    private StyleConfig styleConfig;

    @SuppressWarnings("unchecked")
    @Override
    protected void handle() {
        Graph graph = new Graph();
        for (TypeConverter typeConverter : TYPE_CONVERTERS) {
            if (typeConverter.isType(QueryResult.Type.valueOf(queryResult.getType()))) {
                typeConverter.convert2Graph(sources, graph);
            }
        }

        val styles = new Styles()
                .setGroups(getGroups(graph.getVertices()))
                .setFont(styleConfig.getFont())
                .setEdgeColor(styleConfig.getEdgeColor())
                .setEdgeFont(styleConfig.getEdgeFont());
        graph.setStyles(styles);

        queryResult.setGraph(graph);
    }

    private Map<String, Group> getGroups(List<MyVertex> vertices) {
        if (isEmpty(vertices)) {
            return Collections.emptyMap();
        }
        Map<String, Long> map = vertices.stream().collect(Collectors.groupingBy(MyVertex::getLabel, Collectors.counting()));

        AtomicInteger count = new AtomicInteger();
        Map<String, Group> groups = new HashMap<>(4);
        map.keySet().forEach(label -> {
            groups.put(label, styleConfig.getGroups().get(count.get() % 2));
            count.getAndIncrement();
        });

        return groups;
    }
}