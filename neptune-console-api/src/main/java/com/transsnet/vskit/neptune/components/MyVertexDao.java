package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yangwei
 * @date 2019-12-10 13:50
 */
@Slf4j
@Component
public class MyVertexDao extends BaseDao {
    private static final String TYPE_VERTEX = "V";

    /**
     * 根据id查询点
     *
     * @param id
     * @return
     */
    public Vertex queryVertexById(String id) {
        return queryById(TYPE_VERTEX, id);
    }

    /**
     * 根据id查询点[批量]
     *
     * @param ids
     * @return
     */
    public List<Vertex> queryVertexByIds(List<String> ids) {
        return queryByIds(TYPE_VERTEX, ids);
    }
}
