package com.transsnet.vskit.neptune.components;

import com.transsnet.vskit.neptune.components.converter.EdgeTypeConverter;
import com.transsnet.vskit.neptune.components.converter.PathTypeConverter;
import com.transsnet.vskit.neptune.components.converter.TypeConverter;
import com.transsnet.vskit.neptune.components.converter.VertexTypeConverter;
import com.transsnet.vskit.neptune.model.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象的结果集处理器
 *
 * @param <S> 需要处理的对象
 * @param <T> 处理之后的对象
 * @author yangwei
 * @date 2020-01-08 22:26
 */
@Slf4j
@Component
public abstract class AbstractResultHandler<S, T> {
    protected QueryResult queryResult;
    protected List<S> sources;
    protected List<T> targets;

    protected static final List<TypeConverter> TYPE_CONVERTERS = new ArrayList<>();
    private static volatile boolean isRegister = false;

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

        // 注册转换器
        registerConverter();

        this.queryResult = queryResult;
        this.sources = sources;
        this.targets = targets == null ? new ArrayList<>() : targets;

        handle();
    }

    private void registerConverter() {
        if (isRegister) {
            return;
        }
        TYPE_CONVERTERS.clear();
        TYPE_CONVERTERS.add(new VertexTypeConverter());
        TYPE_CONVERTERS.add(new EdgeTypeConverter());
        TYPE_CONVERTERS.add(new PathTypeConverter());

        isRegister = true;
    }

    /**
     * 子类去实现处理细节
     */
    protected abstract void handle();
}
