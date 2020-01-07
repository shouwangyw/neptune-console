package com.transsnet.vskit.neptune.config;

import com.transsnet.vskit.neptune.model.styles.EdgeColor;
import com.transsnet.vskit.neptune.model.styles.EdgeFont;
import com.transsnet.vskit.neptune.model.styles.Font;
import com.transsnet.vskit.neptune.model.styles.Group;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yangwei
 * @date 2019-12-09 22:24
 */
@Accessors(chain = true)
@Component
@PropertySource(value = "classpath:style.properties", encoding = "utf8")
@ConfigurationProperties("style")
@Data
public class StyleConfig {
    private List<Group> groups;
    private Font font;
    private EdgeColor edgeColor;
    private EdgeFont edgeFont;
}