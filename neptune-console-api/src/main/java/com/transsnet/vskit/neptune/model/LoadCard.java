package com.transsnet.vskit.neptune.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author yangwei
 * @date 2019-12-05 10:12
 */
@Data
@Accessors(chain = true)
public class LoadCard {
    private Card card;
    private long startTime;
    private long updateTime;
    private QueryResult result;
}
