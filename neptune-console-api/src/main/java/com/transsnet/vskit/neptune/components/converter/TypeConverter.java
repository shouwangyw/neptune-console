package com.transsnet.vskit.neptune.components.converter;

import com.transsnet.vskit.neptune.model.QueryResult;
import com.transsnet.vskit.neptune.model.graph.Graph;

import java.util.List;

/**
 * @param <S> 源对象
 * @param <T> 目标对象
 * @author yangwei
 * @date 2020-01-09 09:26
 */
public interface TypeConverter<S, T> {
    /**
     * 判断是否是这种类型
     *
     * @param type
     * @return
     */
    boolean isType(QueryResult.Type type);

    /**
     * 将源对象集合转换为目标对象集合
     *
     * @param sources
     * @param targets
     * @return
     */
    void convert(List<S> sources, List<T> targets);

    /**
     * 对目标对象进一步转换
     *
     * @param sources
     * @param graph
     */
    void convert2Graph(List<T> sources, Graph graph);
}
