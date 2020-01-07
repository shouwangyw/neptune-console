package com.transsnet.vskit.neptune.model.styles;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Font {
    private String color;
    private int size;
    private String face;
    private boolean multi;
}