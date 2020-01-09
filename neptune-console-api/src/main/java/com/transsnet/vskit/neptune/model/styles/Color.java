package com.transsnet.vskit.neptune.model.styles;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author yangwei
 */
@Data
@Accessors(chain = true)
public class Color {
    private String background;
    private String border;
    private Highlight highlight;
    private Hover hover;
}