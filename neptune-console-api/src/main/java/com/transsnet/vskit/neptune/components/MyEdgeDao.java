package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.dao.BaseDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yangwei
 * @date 2019-12-10 13:51
 */
@Slf4j
@Component
public class MyEdgeDao extends BaseDao {
    private static final String TYPE_EDGE = "E";

    /**
     * 根据id查询边
     *
     * @param id
     * @return
     */
    public Edge queryEdgeById(String id) {
        return queryById(TYPE_EDGE, id);
    }

    /**
     * 根据id查询点[批量]
     *
     * @param ids
     * @return
     */
    public List<Edge> queryEdgeByIds(List<String> ids) {
        return queryByIds(TYPE_EDGE, ids);
    }
}
