package com.transsnet.vskit.neptune.model.styles;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author yangwei
 */
@Data
@Accessors(chain = true)
public class Highlight {
    private String background;
    private String border;
}