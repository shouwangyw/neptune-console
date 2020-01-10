package com.transsnet.vskit.neptune.model;

import com.transsnet.vskit.neptune.model.graph.Graph;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author yangwei
 */
@Data
@Accessors(chain = true)
public class QueryResult {
    private String id;
    private List<Object> data;
    private String type;
    private String duration;
    private int showNum;
    private String message;
    private Graph graph;

    /**
     * 查询结果类型
     */
    public enum Type {
        /**
         * 顶点
         */
        VERTEX,
        /**
         * 边
         */
        EDGE,
        /**
         * 路径
         */
        PATH,
        /**
         * 其它
         */
        OTHER,
        ;
    }

    public static QueryResult emptyResult() {
        return new QueryResult()
                .setGraph(new Graph());
    }
}