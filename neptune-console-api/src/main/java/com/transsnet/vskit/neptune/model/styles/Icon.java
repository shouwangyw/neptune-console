package com.transsnet.vskit.neptune.model.styles;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author yangwei
 */
@Data
@Accessors(chain = true)
public class Icon {
    private String face;
    private String code;
    private int size;
    private String color;
}