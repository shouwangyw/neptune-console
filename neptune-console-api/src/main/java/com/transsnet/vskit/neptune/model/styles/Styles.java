package com.transsnet.vskit.neptune.model.styles;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author yangwei
 * @date 2019-12-09 18:01
 */
@Data
@Accessors(chain = true)
public class Styles {
    private Font font;
    private EdgeColor edgeColor;
    private EdgeFont edgeFont;
    private Map<String, Group> groups;
}
