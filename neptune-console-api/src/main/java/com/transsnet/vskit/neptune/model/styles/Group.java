package com.transsnet.vskit.neptune.model.styles;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Group {
    private String shape;
    private int size;
    private Color color;
    private Icon icon;
    private Scaling scaling;
}

