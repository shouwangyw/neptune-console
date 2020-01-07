package com.transsnet.vskit.neptune.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author yangwei
 * @date 2019-12-05 19:35
 */
@Data
@Accessors(chain = true)
public class ExecuteResult {
    private List<Object> data;
    private long duration;
    private String msg;
    private int count;
}
