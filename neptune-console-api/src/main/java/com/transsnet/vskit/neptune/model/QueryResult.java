package com.transsnet.vskit.neptune.model;

import com.transsnet.vskit.neptune.model.graph.Graph;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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

    public enum Type {
        VERTEX,
        EDGE,
        PATH,
        OTHER,
        ;
    }

    public static QueryResult emptyResult() {
        return new QueryResult();
    }
}