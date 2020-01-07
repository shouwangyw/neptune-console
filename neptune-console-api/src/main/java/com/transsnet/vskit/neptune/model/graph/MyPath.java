package com.transsnet.vskit.neptune.model.graph;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author yangwei
 * @date 2019-12-10 11:34
 */
@Data
@Accessors(chain = true)
public class MyPath {
    private List<String> labels;
    private List<Object> objects;
}
