package com.transsnet.vskit.neptune.dao;

import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

/**
 * 图数据库操作接口
 *
 * @author yangwei
 * @date 2020-01-10 16:01
 */
public interface DaoHandler {
    /**
     * 提交脚本执行
     *
     * @param script 需要被执行的脚本
     * @return 返回结果集合
     */
    List<Result> execute(String script);

    /**
     * 根据id查询批量顶点
     *
     * @param ids
     * @return
     */
    List<Vertex> queryVertexByIds(List<String> ids);

    /**
     * 根据id查询批量边
     *
     * @param ids
     * @return
     */
    List<Edge> queryEdgeByIds(List<String> ids);
}
